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
import org.apache.mahout.math.AbstractMatrix;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

/*
 * Helper class for read-only matrix implementations.
 */

public abstract class ReadOnlyMatrix extends AbstractMatrix {

  @Override
  public Matrix clone() {
    return this;
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
  * Can fetch values for bindings.
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

}
