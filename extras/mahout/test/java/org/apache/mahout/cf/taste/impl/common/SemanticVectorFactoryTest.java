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

package org.apache.mahout.cf.taste.impl.common;

import java.io.File;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.junit.Before;
import org.junit.Test;

public final class SemanticVectorFactoryTest extends TasteTestCase {
  private static final String[] MINI = {
    "0,10,0.51",
    "0,11,0.2",
    "0,12,0.3",
    "1,10,0.6",
    "1,12,0.8",
    "2,10,0.1"
  };
  
  private static final String[] MIDI = {
    "123,456,0.1",
    "123,789,0.6",
    "123,654,0.7",
    "234,123,0.5",
    "234,234,1.0",
    "234,999,0.9",
    "345,789,0.6",
    "345,654,0.7",
    "345,123,1.0",
    "345,234,0.5",
    "345,999,0.5",
    "456,456,0.1",
    "456,789,0.5",
    "456,654,0.0",
    "456,999,0.2",};
  
  private DataModel miniModel;
  private DataModel midiModel;
  private File miniFile;
  private File midiFile;
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    miniFile = getTestTempFile("mini.txt");
    writeLines(miniFile, MINI);
    miniModel = new FileDataModel(miniFile);
    midiFile = getTestTempFile("midi.txt");
    writeLines(midiFile, MIDI);
    midiModel = new FileDataModel(midiFile);
  }
  
  @Test
  public void testNearness() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(miniModel, 2);
    testEuclideanUser(svf);
    testEuclideanItem(svf);
    svf = new SemanticVectorFactory(miniModel, 200);
    testCosineUser(svf);
    testCosineItem(svf);
  }

  private void testEuclideanUser(SemanticVectorFactory svf) throws TasteException {
    Vector red = svf.projectUserDense(0);
    Vector blue = svf.projectUserDense(1);
    Vector green = svf.projectUserDense(2);
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double rb = measure.distance(red, blue);
    double rg = measure.distance(red, green);
    double bg = measure.distance(blue, green);
    System.out.println("red:   " + red.toString());
    System.out.println("blue:  " + blue.toString());
    System.out.println("green: " + green.toString());
    System.out.println("red-blue:   " + rb);
    System.out.println("red-green:  " + rg);
    System.out.println("blue-green: " + bg);
    assertEquals(0.16458991148732435, rb, 0.00005);
    assertEquals(0.10752920706215117, rg, 0.00005);
    assertEquals(0.2721185153196426, bg, 0.05);
  }
  
  private void testCosineUser(SemanticVectorFactory svf) throws TasteException {
    Vector red = svf.projectUserDense(0);
    Vector blue = svf.projectUserDense(1);
    Vector green = svf.projectUserDense(2);
    DistanceMeasure measure = new CosineDistanceMeasure();
    double rb = measure.distance(red, blue);
    double rg = measure.distance(red, green);
    double bg = measure.distance(blue, green);
    System.out.println("red:   " + red.toString());
    System.out.println("blue:  " + blue.toString());
    System.out.println("green: " + green.toString());
    System.out.println("red-blue:   " + rb);
    System.out.println("red-green:  " + rg);
    System.out.println("blue-green: " + bg);    
    assertEquals(0.016021707007274122, rb, 0.00005);
    assertEquals(0.03301051970229918, rg, 0.00005);
    assertEquals(0.093861176912028, bg, 0.00005);    
  }
  
  private void testEuclideanItem(SemanticVectorFactory svf) throws TasteException {
    Vector red = svf.projectItemDense(10);
    Vector blue = svf.projectItemDense(11);
    Vector green = svf.projectItemDense(12);
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double rb = measure.distance(red, blue);
    double rg = measure.distance(red, green);
    double bg = measure.distance(blue, green);
    System.out.println("red:   " + red.toString());
    System.out.println("blue:  " + blue.toString());
    System.out.println("green: " + green.toString());
    System.out.println("red-blue:   " + rb);
    System.out.println("red-green:  " + rg);
    System.out.println("blue-green: " + bg);
    assertEquals(0.09207475777244815, rb, 0.00005);
    assertEquals(0.06667715551055312, rg, 0.00005);
    assertEquals(0.1587513971025851, bg, 0.05);
  }
  
  private void testCosineItem(SemanticVectorFactory svf) throws TasteException {
    Vector red = svf.projectItemDense(10);
    Vector blue = svf.projectItemDense(11);
    Vector green = svf.projectItemDense(12);
    DistanceMeasure measure = new CosineDistanceMeasure();
    double rb = measure.distance(red, blue);
    double rg = measure.distance(red, green);
    double bg = measure.distance(blue, green);
    System.out.println("red:   " + red.toString());
    System.out.println("blue:  " + blue.toString());
    System.out.println("green: " + green.toString());
    System.out.println("red-blue:   " + rb);
    System.out.println("red-green:  " + rg);
    System.out.println("blue-green: " + bg);    
    assertEquals(3.285536949558798E-4, rb, 0.00005);
    assertEquals(2.938679694960422E-4, rg, 0.00005);
    assertEquals(0.00121582145646415, bg, 0.00005);    
  }

  @Test
  public void testGoodUser() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(midiModel, 0);
    Vector vUser = svf.projectItemDense(123);
    assertNotNull(vUser);
    Vector vItem = svf.projectItemDense(123);
    assertNotNull(vItem);
  }
  
  public void testBadUser() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(midiModel, 0);
    Vector vUser = svf.projectUserDense(-1);
    assertNull(vUser);
  }
  
  public void testBadItem() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(midiModel, 0);
    Vector vItem = svf.projectItemDense(-1);
    assertNull(vItem);
  }
     
}
