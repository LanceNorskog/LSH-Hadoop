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

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.function.BinaryFunction;
import org.apache.mahout.math.function.UnaryFunction;

import com.google.common.collect.Maps;

/** 
 * Matrix of random but consistent doubles. 
 * Double.MIN_Value -> Double.MAX_VALUE 
 * Linear, limited gaussian, and raw Gaussian distributions
 * 
 * Seed for [row][col] is this.seed + (row * #columns) + column.
 * This allows a RandomVector to take seed + (row * #columns) as its seed
 * and be reproducible from this matrix.
 * 
 * Is read-only. Can be given a writable cache.
 * One quirk: Matrix.like() means "give a writable matrix
 * with the same dense/sparsity profile. The cache also supplies that.
 * */
public class RandomMatrix extends AbstractMatrix {
  // TODO: use enums for this? don't know how to use them
  public static final int LINEAR = 0;
  public static final int GAUSSIAN = 1;
  public static final int GAUSSIAN01 = 2;

  final private Random rnd = new Random();
  final private long seed;
  final private int distribution;
  
  /**
   * Constructs a zero-size matrix.
   * Some serialization thing?
   */

  public RandomMatrix() {
    cardinality[ROW] = 0;
    cardinality[COL] = 0;
    seed = 0;
    distribution = LINEAR;
  }

  /**
   * Constructs an empty matrix of the given size.
   * Linear distribution.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   */
  public RandomMatrix(int rows, int columns) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    seed = 0;
    distribution = LINEAR;
  }

  /*
   * Constructs an empty matrix of the given size.
   * Linear distribution.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   * @param seed Random seed.
   * @param distribution Random distribution: LINEAR, GAUSSIAN, GAUSSIAN01.
   * @param cache Vector to use as cache. Doubles as 'like' source.
  */
  public RandomMatrix(int rows, int columns, long seed, int distribution) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    this.seed = seed;
    this.distribution = distribution;
  }
  
  @Override
  public Matrix clone() {
    // it would thread-safe to pass the cache object itself.
    RandomMatrix clone = new RandomMatrix(rowSize(), columnSize(), seed, distribution);
    if (rowLabelBindings != null) {
      clone.rowLabelBindings = Maps.newHashMap(rowLabelBindings);
    }
    if (columnLabelBindings != null) {
      clone.columnLabelBindings = Maps.newHashMap(columnLabelBindings);
    }
    return clone;
  }

  @Override
  public double getQuick(int row, int column) {
    if (row < 0 || row >= rowSize())
      throw new CardinalityException(row, rowSize());
    if (column < 0 || column >= columnSize())
      throw new CardinalityException(column, columnSize());
    rnd.setSeed(getSeed(row, column));
    double value = getRandom();
    if (!(value > Double.MIN_VALUE && value < Double.MAX_VALUE))
      throw new Error("RandomVector: getQuick created NaN");
    return value;
  }

  private long getSeed(int row, int column) {
    return seed + (row * columnSize()) + column;
  }

  double getRandom() {
    switch (distribution) {
    case LINEAR: return rnd.nextDouble();
    case GAUSSIAN: return rnd.nextGaussian();
    case GAUSSIAN01: return gaussian01();
    default: throw new Error("RandomMatrix: not a random distribution: " + distribution);
    }
  }

  // normal distribution between zero and one
  private double gaussian01() {
    double d = rnd.nextGaussian()/6;
    while(d > 0.5 || d < -0.5) {
      d = rnd.nextGaussian()/6;
    }
    return d;
  }

  @Override
  public Matrix like() {
    return new DenseMatrix(rowSize(), columnSize());
  }

  @Override
 public Matrix like(int rows, int columns) {
    return new DenseMatrix(rows, columns);
  }

  @Override
  public void setQuick(int row, int column, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getNumNondefaultElements() {
    return size();
  }

  @Override
  public Matrix viewPart(int[] offset, int[] size) {
    int rowOffset = offset[ROW];
    int rowsRequested = size[ROW];
    int columnOffset = offset[COL];
    int columnsRequested = size[COL];

    return viewPart(rowOffset, rowsRequested, columnOffset, columnsRequested);
  }

  @Override
  public Matrix viewPart(int rowOffset, int rowsRequested, int columnOffset, int columnsRequested) {
    if (rowOffset < 0) {
      throw new IndexException(rowOffset, rowSize());
    }
    if (rowOffset + rowsRequested > rowSize()) {
      throw new IndexException(rowOffset + rowsRequested, rowSize());
    }
    if (columnOffset < 0) {
      throw new IndexException(columnOffset, columnSize());
    }
    if (columnOffset + columnsRequested > columnSize()) {
      throw new IndexException(columnOffset + columnsRequested, columnSize());
    }
    return new MatrixView(this, new int[]{rowOffset, columnOffset}, new int[]{rowsRequested, columnsRequested});
  }

  @Override
  public Matrix assign(double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assignColumn(int column, Vector other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assignRow(int row, Vector other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assign(double[][] values) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(UnaryFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(Matrix other, BinaryFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector getColumn(int column) {
    if (column < 0 || column >= columnSize()) {
      throw new IndexException(column, columnSize());
    }
    return new RandomVectorOld(cardinality[ROW], seed + cardinality[COL] * column, cardinality[COL], distribution);

  }

  // TODO: make matching cache vector for RandomVector
  @Override
  public Vector getRow(int row) {
    if (row < 0 || row >= rowSize()) {
      throw new IndexException(row, rowSize());
    }
    return new RandomVectorOld(columnSize(), seed + row * columnSize(), 1, distribution);
  }

  /*
   * Can set bindings for all rows and columns.
   * Can fetch values for bindings.
   * Cannot set values with bindings.
   */
  
  @Override
  public
  void set(String rowLabel, String columnLabel, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public
  void set(String rowLabel, String columnLabel, int row, int column, double value) {
    throw new UnsupportedOperationException();

  }

  @Override
  public
  void set(String rowLabel, double[] rowData) {
    throw new UnsupportedOperationException();

  }

  @Override
  public
  void set(String rowLabel, int row, double[] rowData) {		
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o.getClass() == RandomMatrix.class) {
      RandomMatrix r = (RandomMatrix) o;
      return rowSize() == r.rowSize() && columnSize() == r.columnSize() && this.seed == r.seed;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return RandomUtils.hashLong(seed) ^ RandomUtils.hashLong(rowSize()) ^ RandomUtils.hashLong(columnSize());
  }



}
