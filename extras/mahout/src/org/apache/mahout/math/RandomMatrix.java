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
import org.apache.mahout.math.AbstractMatrix.TransposeViewVector;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

import com.google.common.collect.Maps;

/** 
 * Matrix of random but consistent doubles. 
 * -Double.MAX_Value -> Double.MAX_VALUE 
 * Whatever the given generator provides
 * 
 * Seed for [row][col] is this.seed + (row * #columns) + column.
 * This allows a RandomVector to take seed + (row * #columns) as its seed
 * and be reproducible from this matrix.
 **/
public class RandomMatrix extends FabricatedMatrix {

  final private Random rnd;
  final private long seed;
  
  /**
   * Constructs a zero-size matrix.
   * Some serialization thing?
   */

  public RandomMatrix() {
    cardinality[ROW] = 0;
    cardinality[COL] = 0;
    seed = 0;
    rnd = null;
  }

  /**
   * Constructs random matrix of the given size.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   * @param rnd Random number generator.
   */
  public RandomMatrix(int rows, int columns, Random rnd) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    seed = rnd.nextLong();
    this.rnd = rnd;
  }

  @Override
  public Matrix clone() {
    // it would thread-safe to pass the cache object itself.
    RandomMatrix clone = new RandomMatrix(rowSize(), columnSize(), rnd);
    super.cloneBindings(clone);
    return clone;
  }

  @Override
  public double getQuick(int row, int column) {
    if (row < 0 || row >= rowSize())
      throw new CardinalityException(row, rowSize());
    if (column < 0 || column >= columnSize())
      throw new CardinalityException(column, columnSize());
    rnd.setSeed(getSeed(row, column));
    double value = rnd.nextDouble();
    if (!(value > Double.MIN_VALUE && value < Double.MAX_VALUE))
      throw new Error("RandomVector: getQuick created NaN");
    return value;
  }

  private long getSeed(int row, int column) {
    return seed + (row * columnSize()) + column;
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
  public Matrix assign(DoubleFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(Matrix other, DoubleDoubleFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void set(int row, double[] data) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void set(int row, int column, double value) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(Matrix other) {
    throw new UnsupportedOperationException();    
  }
  
  public Vector getColumn(int column) {
    if (column < 0 || column >= columnSize()) {
      throw new IndexException(column, columnSize());
    }
    return new TransposeViewVector(this, column);
  }
  
  public Vector getRow(int row) {
    if (row < 0 || row >= rowSize()) {
      throw new IndexException(row, rowSize());
    }
    Vector wrap = new MatrixVectorView(this, row, 0, 0, 1);
    return wrap;
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
