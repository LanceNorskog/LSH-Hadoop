/*
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

package semvec.mahout.matrix;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.join.TupleWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import lsh.hadoop.LSHDriver;

/*
 * Read User/Item/Pref format, for all dimensions
 * 	map dimension, user/item/userrandom/itemrandom/pref
 * 
 * prefs are mapped to 0 -> 1 via BIAS and SCALE
 */

public class UserItemPrefMapper extends
Mapper<LongWritable,Text, LongWritable,TupleWritable> {
  private static final Pattern DELIMITER = Pattern.compile("::");

  public Float bias = 0.0f;
  public Float scale = 1.0f;

  @Override
  protected void setup(Context context) {
    Configuration conf = context.getConfiguration();
    String scaleString = conf.get(LSHDriver.SCALE);
    String biasString = conf.get(LSHDriver.BIAS);
    if (null != scaleString)
      scale = Float.parseFloat(scaleString);
    if (null != biasString)
      bias = Float.parseFloat(biasString);    
  }

  @Override
  public void map(LongWritable key,
      Text value,
      Context context) throws IOException, InterruptedException {
    String[] tokens = UserItemPrefMapper.DELIMITER.split(value.toString());
    long userID = Long.parseLong(tokens[0]);
    long itemID = Long.parseLong(tokens[1]);
    float prefValue = tokens.length > 2 ? Float.parseFloat(tokens[2]) : scale;
    prefValue = (prefValue - bias) / scale;
    Writable[] tuple = new Writable[3];
    tuple[0] = new LongWritable(userID);
    tuple[1] = new LongWritable(itemID);
    tuple[0] = new FloatWritable(prefValue);
    TupleWritable valueout = new TupleWritable(tuple);	
    context.write(new LongWritable(itemID), valueout);
  }

  /*
   * Random distributions:
   * Since distance calculations drive the outputs to a normal distribution,
   * it is better to start with a normal distribution in the first place.
   * 
   * Both do 0->1
   */
 
}
