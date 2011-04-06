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

import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.function.VectorFunction;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class TestReadOnlyMatrixBase extends MahoutTestCase {
  protected static final int ROW = AbstractMatrix.ROW;
  protected static final int COL = AbstractMatrix.COL;
  private final int[] size = {4,5};
  private final double[] vectorAValues = {1.0 / 1.1, 2.0 / 1.1};
  protected Matrix test;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    test = matrixFactory(size[ROW], size[COL]);
  }

  public abstract Matrix matrixFactory(int rows, int columns);
  public abstract Matrix matrixFactory(int rows, int columns, Map<String,Integer> rowLabelBindings, Map<String,Integer> columnLabelBindings);

  @Test
  public void testCardinality() {
    int[] c = test.size();
    assertEquals("row cardinality", size[ROW], c[ROW]);
    assertEquals("col cardinality", size[COL], c[COL]);
  }

  @Test
  public void testCopy() {
    int[] c = test.size();
    Matrix copy = test.clone();
    assertSame("wrong class", copy.getClass(), test.getClass());
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.getQuick(row, col), copy.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test
  public void testIterate() {
    Iterator<MatrixSlice> it = test.iterator();
    MatrixSlice m;
    while(it.hasNext() && (m = it.next()) != null) {
      Vector v = m.vector();
      Vector w = test instanceof SparseColumnMatrix ? test.getColumn(m.index()) : test.getRow(m.index());
      assertEquals("iterator: " + v.asFormatString() + ", randomAccess: " + w, v, w);
    }
  }

  @Test
  public void testGetQuick() {
    int[] c = test.size();
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals( test.get(row, col), test.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test
  public void testLike() {
    Matrix like = test.like();
    assertEquals("rows", test.size()[ROW], like.size()[ROW]);
    assertEquals("columns", test.size()[COL], like.size()[COL]);
    like.set(0,0,0.0d);
  }

  @Test
  public void testLikeIntInt() {
    Matrix like = test.like(4, 4);
    assertEquals("rows", 4, like.size()[ROW]);
    assertEquals("columns", 4, like.size()[COL]);
    like.set(0,0,0.0d);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetQuick() {
    int[] c = test.size();
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        test.setQuick(row, col, 1.23);
        assertEquals("value[" + row + "][" + col + ']', 1.23, test.getQuick(
            row, col), EPSILON);
      }
    }
  }

  @Test
  public void testViewPart() {
    int[] offset = {1, 1};
    int[] size = {2, 1};
    Matrix view = test.viewPart(offset, size);
    assertEquals(2, view.rowSize());
    assertEquals(1, view.columnSize());
    int[] c = view.size();
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.get(row + 1, col + 1), view.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testViewPartCardinality() {
    int[] offset = {100, 1};
    int[] size = {3, 3};
    test.viewPart(offset, size);
  }

  @Test(expected = IndexException.class)
  public void testViewPartIndexOver() {
    int[] offset = {100, 1};
    int[] size = {2, 2};
    test.viewPart(offset, size);
  }

  @Test(expected = IndexException.class)
  public void testViewPartIndexUnder() {
    int[] offset = {-1, -1};
    int[] size = {2, 2};
    test.viewPart(offset, size);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDouble() {
    test.assign(4.53);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDoubleArrayArray() {
    test.assign(new double[3][2]);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrixBinaryFunction() {
    test.assign(test, Functions.PLUS);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrixBinaryFunctionCardinality() {
    test.assign(test.transpose(), Functions.PLUS);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignMatrix() {
    test.assign(test);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignUnaryFunction() {
    test.assign(Functions.mult(-1));
  }

  @Test
  public void testLikeIsRW() {
    Matrix like = test.like();
    like.set(0,0,1);
    assertEquals(1, like.get(0,0), EPSILON);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignMatrixCardinality() {
    test.assign(test.transpose());
  }

  @Test
  public void testRowView() {
    int[] c = test.size();
    for (int row = 0; row < c[ROW]; row++) {
      Vector v = new DenseVector(c[COL]);
      for(int i = 0; i < c[COL]; i++)
        v.setQuick(i, test.getQuick(row, i));
      assertEquals(0.0, v.minus(test.viewRow(row)).norm(1), EPSILON);
    }

    assertEquals(c[COL], test.viewRow(3).size());
    assertEquals(c[COL], test.viewRow(5).size());

  }

  @Test
  public void testColumnView() {
    int[] c = test.size();
    Matrix result = copyMatrix(c);

    for (int col = 0; col < c[COL]; col++) {
      assertEquals(0.0, result.getColumn(col).minus(test.viewColumn(col)).norm(1), EPSILON);
    }

    assertEquals(c[ROW], test.viewColumn(3).size());
    assertEquals(c[ROW], test.viewColumn(5).size());

  }

  private Matrix copyMatrix(int[] c) {
    Matrix result = new DenseMatrix(test.rowSize(), test.columnSize());
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        result.setQuick(row, col, test.getQuick(row, col));
      }
    }
    return result;
  }


  @Test
  public void testAggregateRows() {
    Vector v = test.aggregateRows(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    });

    for (int i = 0; i < test.numRows(); i++) {
      assertEquals(test.getRow(i).zSum(), v.get(i), EPSILON);
    }
  }

  @Test
  public void testAggregateCols() {
    Vector v = test.aggregateColumns(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    });

    for (int i = 0; i < test.numCols(); i++) {
      assertEquals(test.getColumn(i).zSum(), v.get(i), EPSILON);
    }
  }

  @Test
  public void testAggregate() {
    double total = test.aggregate(Functions.PLUS, Functions.IDENTITY);
    assertEquals(test.aggregateRows(new VectorFunction() {
      public double apply(Vector v) {
        return v.zSum();
      }
    }).zSum(), total, EPSILON);
  }

  @Test
  public void testDivide() {
    int[] c = test.size();
    Matrix value = test.divide(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.get(row, col) / 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testGetIndexUnder() {
    int[] c = test.size();
    for (int row = -1; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        test.get(row, col);
      }
    }
  }

  @Test(expected = IndexException.class)
  public void testGetIndexOver() {
    int[] c = test.size();
    for (int row = 0; row < c[ROW] + 1; row++) {
      for (int col = 0; col < c[COL]; col++) {
        test.get(row, col);
      }
    }
  }

  @Test
  public void testMinus() {
    int[] c = test.size();
    Matrix value = test.minus(test);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']', 0.0, value.getQuick(
            row, col), EPSILON);
      }
    }
  }

  @Test(expected = CardinalityException.class)
  public void testMinusCardinality() {
    test.minus(test.transpose());
  }

  @Test
  public void testPlusDouble() {
    int[] c = test.size();
    Matrix value = test.plus(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.get(row, col) + 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test
  public void testPlusMatrix() {
    int[] c = test.size();
    Matrix value = test.plus(test);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']', test.get(row, col) * 2,
            value.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test(expected = CardinalityException.class)
  public void testPlusMatrixCardinality() {
    test.plus(test.transpose());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetUnder() {
    int[] c = test.size();
    for (int row = -1; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        test.set(row, col, 1.23);
      }
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetOver() {
    int[] c = test.size();
    for (int row = 0; row < c[ROW] + 1; row++) {
      for (int col = 0; col < c[COL]; col++) {
        test.set(row, col, 1.23);
      }
    }
  }

  @Test
  public void testTimesDouble() {
    int[] c = test.size();
    Matrix value = test.times(4.53);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.get(row, col) * 4.53, value.getQuick(row, col), EPSILON);
      }
    }
  }

  /*
   * Compare fab matrix times(Matrix) to DenseMatrix times(Matrix)
   */
  @Test
  public void testTimesMatrix() {
    Matrix copy = new DenseMatrix(test.numRows(), test.numCols());
    copy.assign(test);
    Matrix transpose = test.transpose();
    Matrix value = copy.times(transpose);
    Matrix expected = test.times(transpose);

    for (int i = 0; i < expected.numCols(); i++) {
      for (int j = 0; j < expected.numRows(); j++) {
        assertTrue("Matrix times transpose not correct: " + i + ", " + j
            + "\nexpected:\n\t" + expected.get(i, j) + "\nactual:\n\t"
            + test.get(i, j),
            Math.abs(expected.get(i, j) - value.get(i, j)) < 1.0e-5);
      }
    }

  }

  @Test(expected = CardinalityException.class)
  public void testTimesVector() {
    Vector vectorA = new DenseVector(vectorAValues);
    Vector testTimesVectorA = test.times(vectorA);
    Vector expected = new DenseVector(new double[]{5.0, 11.0, 17.0});
    assertTrue("Matrix times vector not equals: " + vectorA.asFormatString()
        + " != " + testTimesVectorA.asFormatString(),
        expected.minus(testTimesVectorA).norm(2) < 1.0e-12);
    test.times(testTimesVectorA);
  }

  @Test
  public void testTimesSquaredTimesVector() {
    double[] zeros = new double[test.numCols()];
    Vector vectorA = new DenseVector(zeros);
    Vector ttA = test.timesSquared(vectorA);
    Vector ttASlow = test.transpose().times(test.times(vectorA));
    assertTrue("M'Mv != M.timesSquared(v): " + ttA.asFormatString()
        + " != " + ttASlow.asFormatString(),
        ttASlow.minus(ttA).norm(2) < 1.0e-12);

  }

  @Test(expected = CardinalityException.class)
  public void testTimesMatrixCardinality() {
    Matrix other = test.like(1, 1);
    test.times(other);
  }

  @Test
  public void testTranspose() {
    int[] c = test.size();
    Matrix transpose = test.transpose();
    int[] t = transpose.size();
    assertEquals("rows", c[COL], t[ROW]);
    assertEquals("cols", c[ROW], t[COL]);
    for (int row = 0; row < c[ROW]; row++) {
      for (int col = 0; col < c[COL]; col++) {
        assertEquals("value[" + row + "][" + col + ']',
            test.getQuick(row, col), transpose.getQuick(col, row), EPSILON);
      }
    }
  }

  @Test
  public abstract void testZSum();

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignRow() {
    test.assignRow(1, new DenseVector());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignRowCardinality() {
    test.assignRow(1, new DenseVector());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignColumn() {
    test.assignColumn(1, new DenseVector());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testAssignColumnCardinality() {
    double[] data = {2.1, 3.2};
    test.assignColumn(1, new DenseVector(data));
  }

  @Test
  public void testGetRow() {
    Vector row = test.getRow(1);
    assertEquals("row size", test.numCols(), row.getNumNondefaultElements());
    Iterator<Element> it = row.iterateNonZero();
    while(it.hasNext()) {
      Element e = it.next();
      Double expected = e.get();
      Double actual = test.get(1, e.index());
      assertEquals("row vector: " + e.index() + " equals ", expected, actual, EPSILON);
      assertEquals(expected, actual, 1.0e12);
      }
  }

  @Test
  public void testGetColumn() {
    Vector column = test.getColumn(1);
    assertEquals("column size", test.numRows(), column.getNumNondefaultElements());
    Iterator<Element> it = column.iterateNonZero();
    while(it.hasNext()) {
      Element e = it.next();
      Double expected = e.get();
      Double actual = test.get(e.index(), 1);
      assertEquals("row vector: " + e.index() + " equals ", expected, actual, EPSILON);
      assertEquals(expected, actual, 1.0e12);
      }
  }

  @Test(expected = IndexException.class)
  public void testGetRowIndexUnder() {
    test.getRow(-1);
  }

  @Test(expected = IndexException.class)
  public void testGetRowIndexOver() {
    test.getRow(5);
  }

  @Test(expected = IndexException.class)
  public void testGetColumnIndexUnder() {
    test.getColumn(-1);
  }

  @Test(expected = IndexException.class)
  public void testGetColumnIndexOver() {
    test.getColumn(5);
  }

  @Test
  public abstract void testDeterminant();

   @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings1() {
    Matrix m = matrixFactory(3,3);
    m.set("Fee", "Foo", 1, 2, 9);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings2() {
    Matrix m = matrixFactory(3,3);
    double[] row = new double[3];
    m.set("Fee", row);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings3() {
    Matrix m = matrixFactory(3,3);
    double[] row = new double[3];
    m.set("Fee", 2, row);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings4() {
    Matrix m = matrixFactory(3,3);
    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    m.set("Fee", "Foo", 2);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings5() {
    Matrix m = matrixFactory(3,3);
    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    m.set("Fee", "Foo", 1, 2, 9);
  }


  @Test
  public void testLabelBindings() {
    Map<String, Integer> rowBindings = new HashMap<String, Integer>();
    rowBindings.put("Fee", 0);
    rowBindings.put("Fie", 1);
    rowBindings.put("Foe", 2);
    Map<String, Integer> columnBindings = new HashMap<String, Integer>();
    columnBindings.put("Foo", 0);
    columnBindings.put("Bar", 1);
    columnBindings.put("Baz", 2);
    Matrix m = matrixFactory(3,3,rowBindings,columnBindings);

    assertEquals("column", columnBindings, m.getColumnLabelBindings());
    assertEquals("row", rowBindings, m.getRowLabelBindings());
    assertEquals("Fee", m.get(0, 1), m.get("Fee", "Bar"), EPSILON);
  }

  @Test
  public void testGettingLabelBindings() {
    Map<String, Integer> rowBindings = new HashMap<String, Integer>();
    rowBindings.put("Fee", 0);
    rowBindings.put("Fie", 1);
    rowBindings.put("Foe", 2);
    Map<String, Integer> columnBindings = new HashMap<String, Integer>();
    columnBindings.put("Foo", 0);
    columnBindings.put("Bar", 1);
    columnBindings.put("Baz", 2);
    Matrix m = matrixFactory(3,3,rowBindings,columnBindings);

    assertTrue("get value from label", m.get("Fee", "Foo") == m.get(0, 0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSettingLabelBindings() {
    Matrix m = matrixFactory(3,3);
    assertNull("row bindings", m.getRowLabelBindings());
    assertNull("col bindings", m.getColumnLabelBindings());
    m.set("Fee", "Foo", 1, 2, 9);
  }

 }
