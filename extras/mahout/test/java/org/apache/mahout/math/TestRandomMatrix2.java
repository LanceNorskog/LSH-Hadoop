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

public class TestRandomMatrix2 extends TestFabricatedMatrix {

  protected static final int ROW = AbstractMatrix.ROW;

  protected static final int COL = AbstractMatrix.COL;

  int rows = 4;
  int columns = 5;
  int[] cardinality = {rows, columns};

//  private AbstractMatrix testLinear;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public
   FabricatedMatrix matrixFactory(int rows, int columns) {
    return new RandomMatrix(rows, columns, 0);
  }

  @Override
  public void testZSum() {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void testDeterminant() {
    // TODO Auto-generated method stub
    
  }


}
