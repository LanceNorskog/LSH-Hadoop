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

package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;

import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommender;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.iterator.FileLineIterable;
import org.apache.mahout.common.iterator.FileLineIterator;

public final class GroupLensDataModel extends FileDataModel {
  
  private static final String COLON_DELIMTER = "::";
  private static final Pattern COLON_DELIMITER_PATTERN = Pattern.compile(COLON_DELIMTER);
  
  public GroupLensDataModel() throws IOException {
    this(readResourceToTempFile("/org/apache/mahout/cf/taste/example/grouplens/ratings.dat"));
  }
  
  /**
   * @param ratingsFile GroupLens ratings.dat file in its native format
   * @throws IOException if an error occurs while reading or writing files
   */
  public GroupLensDataModel(File ratingsFile) throws IOException {
    super(convertGLFile(ratingsFile));
  }
  
  public GroupLensDataModel(File ratingsFile, MetadataModel<String> itemNames, MetadataModel<Integer> itemYears, MetadataModel<String[]> itemGenres) throws IOException {
    super(convertGLFile(ratingsFile));
    File[] files = ratingsFile.getParentFile().listFiles();
    for(File metadata: files) {
      if (metadata.getName().equals("movies.dat")) {
        if (null != itemNames) {
          readMovies(metadata, itemNames, itemYears, itemGenres);
        }
      }
    }
  }
  
   void readMovies(File moviesFile, MetadataModel<String> itemNames, MetadataModel<Integer> itemYears, MetadataModel<String[]> itemGenres) throws IOException {
    FileLineIterable lines = new FileLineIterable(moviesFile);
    for(String line: lines) {
      String[] parts = line.split(COLON_DELIMTER);
      Long itemId = Long.parseLong(parts[0]);
      String movieName = parts[1];
      String[] genres = parts[2].split("\\|");
      System.out.println(itemId + " -- " + movieName + " -- " + Arrays.toString(genres));
      int lparen = movieName.lastIndexOf('(');
      int year = Integer.parseInt(movieName.substring(lparen + 1, lparen + 5));
      if (null != itemNames)
        itemNames.put(itemId, movieName.substring(0, lparen - 1));
      if (null != itemYears)
        itemYears.put(itemId, year);
      if (null != itemGenres)
        itemGenres.put(itemId, genres);
    }
  }
  
  private static File convertGLFile(File originalFile) throws IOException {
    // Now translate the file; remove commas, then convert "::" delimiter to comma
    File resultFile = new File(new File(System.getProperty("java.io.tmpdir")), "ratings.txt");
    if (resultFile.exists()) {
      resultFile.delete();
    }
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile), Charsets.UTF_8));
      for (String line : new FileLineIterable(originalFile, false)) {
        int lastDelimiterStart = line.lastIndexOf(COLON_DELIMTER);
        if (lastDelimiterStart < 0) {
          throw new IOException("Unexpected input format on line: " + line);
        }
        String subLine = line.substring(0, lastDelimiterStart);
        String convertedLine = COLON_DELIMITER_PATTERN.matcher(subLine).replaceAll(",");
        writer.println(convertedLine);
      }
    } catch (IOException ioe) {
      resultFile.delete();
      throw ioe;
    } finally {
      Closeables.closeQuietly(writer);
    }
    return resultFile;
  }
  
  public static File readResourceToTempFile(String resourceName) throws IOException {
    InputSupplier<? extends InputStream> inSupplier;
    try {
      URL resourceURL = Resources.getResource(GroupLensRecommender.class, resourceName);
      inSupplier = Resources.newInputStreamSupplier(resourceURL);
    } catch (IllegalArgumentException iae) {
      File resourceFile = new File("src/main/java" + resourceName);
      inSupplier = Files.newInputStreamSupplier(resourceFile);
    }
    File tempFile = File.createTempFile("taste", null);
    tempFile.deleteOnExit();
    Files.copy(inSupplier, tempFile);
    return tempFile;
  }
  
  @Override
  public String toString() {
    return "GroupLensDataModel";
  }
  
  public static void main(String[] args) throws IOException {
    Map<Long,String> itemNames = new HashMap<Long,String>();
    MetadataModel<String> metaNames = new MetadataModel<String>(itemNames, "movie");
    MetadataModel<Integer> metaYears = new MetadataModel<Integer>(new HashMap<Long,Integer>(), "year");
    MetadataModel<String[]> metaGenres = new MetadataModel<String[]>(new HashMap<Long,String[]>(), "genres");
    DataModel model = new GroupLensDataModel(new File(args[0]), metaNames, metaYears, metaGenres);
  }
  
}
