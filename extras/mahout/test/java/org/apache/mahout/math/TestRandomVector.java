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

import java.util.Random;

import org.apache.mahout.math.RandomVector;
import org.apache.mahout.math.ReadOnlyVector;
import org.apache.mahout.math.TestReadOnlyVectorBase;
import org.junit.Test;

public class TestRandomVector extends TestReadOnlyVectorBase {

//  static private RandomVector testLinear = new RandomVector(2);
//  static private RandomVector testBig = new RandomVector(500);
//  static private RandomVector testG = new RandomVector(4, 0, new Date().getTime(), RandomMatrix.GAUSSIAN);

  @Override
  public ReadOnlyVector generateTestVector(int cardinality) {
    return new RandomVector(cardinality, new Random());
  }

  @Test
  @Override
  public void testZSum() {
    // assumes random generator is 0 -> 1
    assertTrue("linear zSum < length", four.zSum() < 4);
  }

}
