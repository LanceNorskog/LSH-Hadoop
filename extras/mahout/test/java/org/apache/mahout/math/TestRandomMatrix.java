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

package org.apache.mahout.math;

import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.function.VectorFunction;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class TestRandomMatrix extends TestReadOnlyMatrixBase {

  protected static final int ROW = AbstractMatrix.ROW;

  protected static final int COL = AbstractMatrix.COL;

  int rows = 4;
  int columns = 5;
  int[] cardinality = {rows, columns};

  private AbstractMatrix testLinear;
  
  

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    testLinear = matrixFactory(rows, columns);
  }
  
  @Override
  public
   ReadOnlyMatrix matrixFactory(int rows, int columns) {
    return new RandomMatrix(rows, columns, 0);
  }

  @Test
  public void testDeterminacy() {
    double d = testLinear.getQuick(1,1);
    testLinear.getQuick(2,2);
    assertTrue("Determinacy", d == testLinear.getQuick(1,1));
  }

  @Test
  public void testZSum() {
    double sum = testLinear.zSum();
    int[] c = testLinear.size();
    assertTrue("zsum", sum > 0);
    assertTrue("zsum", sum < c[0] * c[1]);
    sum = testLinear.zSum();
    c = testLinear.size();
    assertTrue("zsum", sum > 0);
    assertTrue("zsum", sum < c[0] * c[1]);
    // TODO: what is a good assertion about zSum of proper Gaussians?
  }

  @Override
  public void testDeterminant() {
    Matrix m = matrixFactory(5, 5);
    double d = m.determinant();
    assertEquals("determinant", -1.4178246803400959E-16, m.determinant(), EPSILON);
  }

}
