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

public class TestRandomMatrix extends MahoutTestCase {

  protected static final int ROW = AbstractMatrix.ROW;

  protected static final int COL = AbstractMatrix.COL;

  protected RandomMatrix testLinear;
  protected RandomMatrix testGaussian;
  protected RandomMatrix testGaussian01;
  protected RandomMatrix testCached;

  int rows = 4;
  int columns = 5;
  int[] cardinality = {rows, columns};

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    testLinear = new RandomMatrix(rows, columns);
    testGaussian = new RandomMatrix(rows, columns);
    testGaussian01 = new RandomMatrix(rows, columns);
    testCached = new RandomMatrix(rows, columns, 500, RandomMatrix.GAUSSIAN);
  }

  @Test
  public void testCardinality() {
    int[] c = testLinear.size();
    assertEquals("row cardinality", rows, c[ROW]);
    assertEquals("col cardinality", columns, c[COL]);
  }

  @Test
  public void testCopy() {

  }

  @Test
  public void testRepeatable() {
    double d = testLinear.getQuick(1,1);
    testLinear.getQuick(2,2);
    assertTrue("repeatable", d == testLinear.getQuick(1,1));
  }

   @Test
  public void testIterate() {
    Iterator<MatrixSlice> it = testLinear.iterator();
    MatrixSlice m;
    while(it.hasNext() && (m = it.next()) != null) {
      Vector v = m.vector();
      Vector w = testLinear.getRow(m.index());
      assertEquals("iterator: " + v.asFormatString() + ", randomAccess: " + w, v, w);
    }
  }

  @Test
  public void testLike() {
    Matrix like = testLinear.like();
    assertTrue("like", like instanceof DenseMatrix);
  }

  @Test
  public void testLikeIntInt() {
    Matrix like = testLinear.like(4, 4);
    assertTrue("likeIntInt", like instanceof DenseMatrix);
  }

  @Test
  public void testSize() {
    int[] c = testLinear.getNumNondefaultElements();
    assertEquals("row size", rows, c[ROW]);
    assertEquals("col size", columns, c[COL]);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDouble() {
    testLinear.assign(4.53);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDoubleArrayArray() {
    testLinear.assign(new double[3][2]);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrixBinaryFunction() {
    testLinear.assign(testLinear, Functions.PLUS);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrixBinaryFunctionCardinality() {
    testLinear.assign(testLinear.transpose(), Functions.PLUS);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrix() {
    testLinear.assign(testLinear);
  }


  @Test (expected = UnsupportedOperationException.class)
  public void testAssignUnaryFunction() {
    testLinear.assign(Functions.mult(-1));
  }

  @Test
  public void testViewPart() {
    int[] offset = {1, 1};
    int[] size = {2, 1};
    Matrix view = testLinear.viewPart(offset, size);
    assertEquals(2, view.rowSize());
    assertEquals(1, view.columnSize());
    int[] c = view.size();
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row + 1, col + 1), view.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testViewPartCardinality() {
    int[] offset = {1, 1};
    int[] size = {rows, columns};
    testLinear.viewPart(offset, size);
  }

  @Test(expected = IndexException.class)
  public void testViewPartIndexOver() {
    int[] offset = {1, 1};
    int[] size = {rows, columns};
    testLinear.viewPart(offset, size);
  }

  @Test(expected = IndexException.class)
  public void testViewPartIndexUnder() {
    int[] offset = {-1, -1};
    int[] size = {rows+10, columns+10};
    testLinear.viewPart(offset, size);
  }

  @Test
  public void testRowView() {
    int[] c = testLinear.size();
    for (int row = 0; row < c[ROW]; row++) {
      Vector v = new DenseVector(columns);
      for(int i = 0; i < columns; i++)
        v.setQuick(i, testLinear.getQuick(row, i));
      assertEquals(0.0, v.minus(testLinear.viewRow(row)).norm(1), EPSILON);
    }

    assertEquals(c[COL], testLinear.viewRow(3).size());
    assertEquals(c[COL], testLinear.viewRow(5).size());

  }

  @Test
  public void testColumnView() {
    int[] c = testLinear.size();
    Matrix result = copyMatrix(c);

    for (int col = 0; col < c[COL]; col++) {
      assertEquals(0.0, result.getColumn(col).minus(testLinear.viewColumn(col)).norm(1), EPSILON);
    }

    assertEquals(c[ROW], testLinear.viewColumn(3).size());
    assertEquals(c[ROW], testLinear.viewColumn(5).size());

  }

  private Matrix copyMatrix(int[] c) {
    Matrix result = new DenseMatrix(testLinear.rowSize(), testLinear.columnSize());
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        result.setQuick(row, col, testLinear.getQuick(row, col));
      }
    }
    return result;
  }

  @Test
  public void testAggregateRows() {
    Vector v = testLinear.aggregateRows(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    });

    for (int i = 0; i < testLinear.numRows(); i++) {
      assertEquals(testLinear.getRow(i).zSum(), v.get(i), EPSILON);
    }
  }

  @Test
  public void testAggregateCols() {
    Vector v = testLinear.aggregateColumns(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    });

    for (int i = 0; i < testLinear.numCols(); i++) {
      assertEquals(testLinear.getColumn(i).zSum(), v.get(i), EPSILON * 100000);
    }
  }

  @Test
  public void testAggregate() {
    double total = testLinear.aggregate(Functions.PLUS, Functions.IDENTITY);
    assertEquals(testLinear.aggregateRows(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    }).zSum(), total, EPSILON);
  }

  @Test
  public void testDivide() {
    int[] c = testLinear.size();
    Matrix value = testLinear.divide(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row, col) / 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testGetIndexUnder() {
    int[] c = testLinear.size();
    for (int row = -1; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        testLinear.get(row, col);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testGetIndexOver() {
    int[] c = testLinear.size();
    for (int row = 0; row < c[ROW] + 1; row++) {
      for (int col = 0; col < c[COL]; col++) {
        testLinear.get(row, col);
      }
    }
  }

  @Test
  public void testMinus() {
    int[] c = testLinear.size();
    Matrix result = copy();
    Matrix value = result.minus(testLinear);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']', 0.0, value.getQuick(
            row, col), EPSILON);
      }
    }
  }

  private Matrix copy() {
    Matrix result = new DenseMatrix(testLinear.rowSize(), testLinear.columnSize());
    for (int i = 0; i < testLinear.rowSize(); i++) {
      for(int j = 0; j < testLinear.columnSize(); j++) {
        result.setQuick(i, j, testLinear.getQuick(i, j));
      }
    }
    return result;
  }

  @Test(expected = CardinalityException.class)
  public void testMinusCardinality() {
    testLinear.minus(testLinear.transpose());
  }

  @Test
  public void testPlusDouble() {
    int[] c = testLinear.size();
    Matrix dense = copy();

    Matrix value = dense.plus(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row, col) + 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test
  public void testPlusMatrix() {
    Matrix result = copy();
    int[] c = testLinear.size();
    Matrix dense = copy();

    Matrix value = dense.plus(result);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']', testLinear.getQuick(row, col) * 2,
            value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = CardinalityException.class)
  public void testPlusMatrixCardinality() {
    testLinear.plus(testLinear.transpose());
  }

  @Test(expected = IndexException.class)
  public void testSetUnder() {
    int[] c = testLinear.size();
    for (int row = -1; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        testLinear.set(row, col, 1.23);
      }
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetOver() {
    int[] c = testLinear.size();
    for (int row = 0; row < c[ROW] + 1; row++) {
      for (int col = 0; col < c[COL]; col++) {
        testLinear.set(row, col, 1.23);
      }
    }
  }

  @Test
  public void testTimesDouble() {
    int[] c = testLinear.size();
    Matrix value = testLinear.times(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row, col) * 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  public void testTimesMatrix() {
    int[] c = testLinear.size();
    Matrix multiplier = new DenseMatrix(testLinear.rowSize(), testLinear.columnSize());
    for(int row = 0; row < testLinear.rowSize(); row++)
      for(int column = 0; column < testLinear.columnSize(); column++)
        multiplier.setQuick(row, column, 4.53);
        
    Matrix value = testLinear.times(multiplier);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row, col) * 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = CardinalityException.class)
  public void testTimesMatrixCardinality() {
    Matrix toobig = testLinear.like(testLinear.columnSize() + 1, 1);
    testLinear.times(toobig);
  }

  @Test
  public void testTranspose() {
    int[] c = testLinear.size();
    Matrix transpose = testLinear.transpose();
    int[] t = transpose.size();
    assertEquals("rows", c[COL], t[ROW]);
    assertEquals("cols", c[ROW], t[COL]);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            testLinear.getQuick(row, col), transpose.getQuick(col, row), EPSILON);
      }
    }
  }

  @Test
  public void testZSum() {
    double sum = testLinear.zSum();
    int[] c = testLinear.size();
    assertTrue("zsum", sum > 0);
    assertTrue("zsum", sum < c[0] * c[1]);
    sum = testGaussian01.zSum();
    c = testGaussian01.size();
    assertTrue("zsum", sum > 0);
    assertTrue("zsum", sum < c[0] * c[1]);
    // TODO: what is a good assertion about zSum of proper Gaussians?
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignRow() {
    testLinear.assignRow(1, new DenseVector());
  }

  @Test
  public void testGetRow() {
    Vector row = testLinear.getRow(1);
    assertEquals("row size", columns, row.getNumNondefaultElements());
  }

  @Test(expected = IndexException.class)
  public void testGetRowIndexUnder() {
    testLinear.getRow(-1);
  }

  @Test(expected = IndexException.class)
  public void testGetRowIndexOver() {
    testLinear.getRow(5);
  }

  @Test
  public void testGetColumn() {
    Vector column = testLinear.getColumn(1);
    assertEquals("row size", rows, column.getNumNondefaultElements());
  }

  @Test(expected = IndexException.class)
  public void testGetColumnIndexUnder() {
    testLinear.getColumn(-1);
  }

  @Test(expected = IndexException.class)
  public void testGetColumnIndexOver() {
    testLinear.getColumn(5);
  }

  @Test
  public void testAsFormatString() {
    String string = testLinear.asFormatString();
    int[] cardinality = {rows, columns};
    Matrix m = AbstractMatrix.decodeMatrix(string);
    for (int row = 0; row < cardinality[ROW]; row++) {
      for (int col = 0; col < cardinality[COL]; col++) {
        assertEquals("m[" + row + ',' + col + ']', testLinear.get(row, col), m.get(
            row, col), EPSILON);
      }
    }
  }

  @Test
  public void testGettingLabelBindings() {
    Matrix m = new RandomMatrix(3,3);
    Map<String, Integer> rowBindings = new HashMap<String, Integer>();
    rowBindings.put("Fee", 0);
    rowBindings.put("Fie", 1);
    rowBindings.put("Foe", 2);
    m.setRowLabelBindings(rowBindings);
    assertEquals("row", rowBindings, m.getRowLabelBindings());
    Map<String, Integer> colBindings = new HashMap<String, Integer>();
    colBindings.put("Foo", 0);
    colBindings.put("Bar", 1);
    colBindings.put("Baz", 2);
    m.setColumnLabelBindings(colBindings);

    assertTrue("get value from label", m.get("Fee", "Foo") == m.get(0, 0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings1() {
    Matrix m = new RandomMatrix(3,3);
    m.set("Fee", "Foo", 1, 2, 9);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings2() {
    Matrix m = new RandomMatrix(3,3);
    double[] row = new double[3];
    m.set("Fee", row);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings3() {
    Matrix m = new RandomMatrix(3,3);
    double[] row = new double[3];
    m.set("Fee", 2, row);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings4() {
    Matrix m = new RandomMatrix(3,3);
    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    m.set("Fee", "Foo", 2);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings5() {
    Matrix m = new RandomMatrix(3,3);
    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    m.set("Fee", "Foo", 1, 2, 9);
  }

  @Test
  public void testLabelBindingSerialization() {
    Matrix m = new RandomMatrix(3,3);

    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    Map<String, Integer> rowBindings = new HashMap<String, Integer>();
    rowBindings.put("Fee", 0);
    rowBindings.put("Fie", 1);
    rowBindings.put("Foe", 2);
    m.setRowLabelBindings(rowBindings);
    assertEquals("row", rowBindings, m.getRowLabelBindings());
    Map<String, Integer> colBindings = new HashMap<String, Integer>();
    colBindings.put("Foo", 0);
    colBindings.put("Bar", 1);
    colBindings.put("Baz", 2);
    m.setColumnLabelBindings(colBindings);
    String json = m.asFormatString();
    Matrix mm = AbstractMatrix.decodeMatrix(json);
    assertEquals("Fee", m.get(0, 1), mm.get("Fee", "Bar"), EPSILON);
  }


}
