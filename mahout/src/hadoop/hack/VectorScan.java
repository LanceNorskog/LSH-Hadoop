/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;
import lsh.core.VertexTransitiveHasher;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.cf.taste.neighborhood.DenseHash;
import org.apache.mahout.cf.taste.neighborhood.Hash;
import org.apache.mahout.cf.taste.neighborhood.SimplexSpace;
import org.apache.mahout.cf.taste.neighborhood.SparseHash;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VectorScan {
  SimplexSpace<String>  space = null;
  static int DIMS = 45000;
  
  private static final Logger log = LoggerFactory.getLogger(VectorScan.class);
  
  private VectorScan() {
  }
  
  public static void main(String[] args) throws Exception {
    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();
    
    Option seqOpt = obuilder.withLongName("seqFile").withRequired(false).withArgument(
      abuilder.withName("seqFile").withMinimum(1).withMaximum(1).create()).
      withDescription("The Sequence File containing the Clusters").withShortName("s").create();
    Option outputOpt = obuilder.withLongName("output").withRequired(false).withArgument(
      abuilder.withName("output").withMinimum(1).withMaximum(1).create()).
      withDescription("The output file.  If not specified, dumps to the console").withShortName("o").create();
    Option substringOpt = obuilder.withLongName("substring").withRequired(false).withArgument(
      abuilder.withName("substring").withMinimum(1).withMaximum(1).create()).
      withDescription("The number of chars of the asFormatString() to print").withShortName("b").create();
    Option countOpt = obuilder.withLongName("count").withRequired(false).
    withDescription("Report the count only").withShortName("c").create();
    Option helpOpt = obuilder.withLongName("help").withDescription("Print out help").withShortName("h").create();
    
    Group group = gbuilder.withName("Options").withOption(seqOpt).withOption(outputOpt)
      .withOption(substringOpt).withOption(countOpt).withOption(helpOpt).create();
    
    try {
      Parser parser = new Parser();
      parser.setGroup(group);
      CommandLine cmdLine = parser.parse(args);
      
      if (cmdLine.hasOption(helpOpt)) {
        
        printHelp(group);
        return;
      }
      
      boolean doCount = false;
      if (cmdLine.hasOption(countOpt))
        doCount = true;
      
      if (cmdLine.hasOption(seqOpt)) {
        Path path = new Path(cmdLine.getValue(seqOpt).toString());
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(path.toUri(), conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        int start = 0;
        int end = 18;
        SimplexSpace<String>[] spaces = makeSpaces(start, end, doCount);
        try {
          int sub = Integer.MAX_VALUE;
          if (cmdLine.hasOption(substringOpt)) {
            sub = Integer.parseInt(cmdLine.getValue(substringOpt).toString());
          }
          Text key = (Text) reader.getKeyClass().asSubclass(Writable.class).newInstance();
          VectorWritable value = (VectorWritable) reader.getValueClass().asSubclass(Writable.class).newInstance();
          int count = 0;
          while (reader.next(key, value)) {
            String text = key.toString();
            Vector v = value.get();
//            int size = v.size();
//            int density = v.getNumNondefaultElements();
//            System.out.println(size + "," + density);
            addSpaces(spaces, start, text, v);
            count++;
            if (count % 1000 == 0)
              System.out.print(".");
            if (count == 100000)
              break;
          }
          printSpaces(spaces, start);
        } finally {
        }
      }
      
    } catch (OptionException e) {
      log.error("Exception", e);
      printHelp(group);
    }
    
  }
  
  private static void printSpaces(SimplexSpace<String>[] spaces, int start) {
    for(int i = start; i < spaces.length; i++) {
      System.out.println("#" + i);
      System.out.println("non-single: " + spaces[i].getNonSingleHashes());
      System.out.println("max:        " + spaces[i].getMaxHashes());
      System.out.println("count:      " + spaces[i].getCount());
      System.out.println("range:      (" + spaces[i].getMinHash() + "," + spaces[i].getMaxHash() + ")");
//      System.out.println("space:      " + spaces[i].toString());
    }   
  }

  private static void addSpaces(SimplexSpace<String>[] spaces, int start, String key, Vector v) {
   SparseHash sh = (SparseHash) spaces[start].getHashLOD(v);
    for(int lod = start; lod < spaces.length; lod++) {
      Hash spot = new SparseHash(sh, lod);
      if (null != spaces[lod]) {
        spaces[lod].addHash(v, spot, key);
        // set key as payload
      }
    }
  }

  private static SimplexSpace<String>[] makeSpaces(int start, int n, boolean doCount) {
//    Hasher hasher = new OrthonormalHasher(DIMS, 0.001d);
    Hasher hasher = new VertexTransitiveHasher(DIMS, 0.001d);
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    SimplexSpace<String>[] spaces = new SimplexSpace[n];
    for(int i = start; i < n; i++) {
      SimplexSpace<String> space = new SimplexSpace<String>(hasher, DIMS, measure, false, doCount);
      spaces[i] = space;
      space.setLOD(i);
    }
    return spaces;
  }

  private static void printHelp(Group group) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setGroup(group);
    formatter.print();
  }
}
