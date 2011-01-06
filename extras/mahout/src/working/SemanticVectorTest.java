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

package working;

import java.io.File;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.MahoutTestCase;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.IndexException;
import org.apache.mahout.math.Vector;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.annotations.Expose;

public final class SemanticVectorTest extends TasteTestCase {
  private static final String[] DATA = {
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

private DataModel model;
private File testFile;

@Override
@Before
public void setUp() throws Exception {
  super.setUp();
  testFile = getTestTempFile("test.txt");
  writeLines(testFile, DATA);
  model = new FileDataModel(testFile);
}


  @Test
  public void testVectors() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(model, 0);
    // use all items
    Vector vu = svf.getUserVector(123, 0, 0);
    assertNotNull(vu);
    // use two randomly sampled items
    vu = svf.getUserVector(123, 0, 2);
    assertNotNull(vu);
    // use all users
    Vector vi = svf.getItemVector(789, 0, 0);
    assertNotNull(vi);
    // use two randomly sampled users
    vi = svf.getItemVector(789, 0, 2);
    assertNotNull(vi);
  }

  @Test(expected = NoSuchUserException.class)
  public void testBadUser() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(model, 0);
    Vector vu = svf.getUserVector(-1, 0, 0);
   }

  @Test(expected = NoSuchItemException.class)
  public void testBadItem() throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(model, 0);
    Vector vu = svf.getItemVector(-1, 0, 0);
  }

}
