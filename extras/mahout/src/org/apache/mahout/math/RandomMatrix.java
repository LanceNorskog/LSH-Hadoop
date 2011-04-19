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

import java.util.Map;
import java.util.Random;

import com.google.common.primitives.Longs;

/** 
 * Matrix of random but repeatable doubles. 
 * -Double.MAX_Value -> Double.MAX_VALUE 
 * 
 * Can only use java.util.Random as generator from RandomUtils does not honor setSeed()
 */
public class RandomMatrix extends ReadOnlyMatrix {

  private long baseSeed;
  final private Random rnd = new Random(0);
  final boolean gaussian;
  
  /**
   * Constructs a zero-size matrix.
   * Some serialization thing?
   */

  public RandomMatrix() {
    cardinality[ROW] = 0;
    cardinality[COL] = 0;
    baseSeed = 0;
    gaussian = false;
  }

  /**
   * Constructs random matrix of the given size.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   * @param baseSeed The starting seed
   * @param .
   * @throws Exception 
   */
  public RandomMatrix(int rows, int columns, long seed, boolean gaussian, Map<String,Integer> rowBindings, Map<String,Integer> columnBindings) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    this.baseSeed = seed;
    this.gaussian = gaussian;
    this.rowLabelBindings = rowBindings;
    this.columnLabelBindings = columnBindings;
  }


  @Override
  public double getQuick(int row, int column) {
    if (row < 0 || row >= rowSize())
      throw new CardinalityException(row, rowSize());
    if (column < 0 || column >= columnSize())
      throw new CardinalityException(column, columnSize());
    rnd.setSeed(getSeed(row, column));
    double value = gaussian ? rnd.nextGaussian() : rnd.nextDouble();
    return value;
  }

  private long getSeed(int row, int column) {
    return baseSeed + (row * columnSize()) + column;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o.getClass() == RandomMatrix.class) {
      RandomMatrix r = (RandomMatrix) o;
      return rowSize() == r.rowSize() && columnSize() == r.columnSize() &&
        baseSeed == r.baseSeed && gaussian == r.gaussian;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Longs.hashCode(baseSeed) ^ Longs.hashCode((rowSize()) ^ Longs.hashCode(columnSize() ^ (gaussian ? 7 : 11)));
  }

}
