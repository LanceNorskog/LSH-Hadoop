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
import org.junit.Test;

import java.util.Date;
import java.util.Iterator;

public class TestRandomVector extends MahoutTestCase {

  static private RandomVector testLinear = new RandomVector(2);
  static private RandomVector testBig = new RandomVector(500);
  static private RandomVector testG = new RandomVector(4, 0, new Date().getTime(), RandomMatrix.GAUSSIAN);

  @Test
  public void testAsFormatString() {
    String formatString = testBig.asFormatString();
    Vector vec = AbstractVector.decodeVector(formatString);
    assertEquals(vec, testBig);
  }

  @Test
  public void testCardinality() {
    assertEquals("size", 500, testBig.size());
  }

  @Test
  public void testIterator() throws Exception {
    DenseVector copy = new DenseVector(testG);
    double[] gold = new double[copy.size()];
    for(int i = 0; i < gold.length; i++)
      gold[i] = copy.getQuick(i);
    Iterator<Vector.Element> iterator = testG.iterateNonZero();
    checkIterator(iterator, gold);


  }

  private static void checkIterator(Iterator<Vector.Element> nzIter, double[] values) {
    while (nzIter.hasNext()) {
      Vector.Element elt = nzIter.next();
      assertEquals((elt.index()) + " Value: " + values[elt.index()]
                                                       + " does not equal: " + elt.get(), values[elt.index()], elt.get(), 0.0);
    }
  }

  @Test
  public void testCopy() throws Exception {
    Vector copy = testBig.clone();
    for (int i = 0; i < testBig.size(); i++) {
      assertEquals("copy [" + i + ']', testBig.get(i), copy.get(i), EPSILON);
    }
  }

  @Test(expected = IndexException.class)
  public void testGetOver() {
    testBig.get(testBig.size());
  }

  @Test(expected = IndexException.class)
  public void testGetUnder() {
    testBig.get(-1);
  }

  @Test
  public void testViewPart() throws Exception {
    Vector part = testBig.viewPart(1, 2);
    assertEquals("part size", 2, part.getNumNondefaultElements());
    for (int i = 0; i < part.size(); i++) {
      assertEquals("part[" + i + ']', testBig.get(i+1), part.get(i), EPSILON);
    }
  }

  @Test(expected = IndexException.class)
  public void testViewPartUnder() {
    testBig.viewPart(-1, testBig.size());
  }

  @Test(expected = IndexException.class)
  public void testViewPartOver() {
    testG.viewPart(2, 7);
  }

  @Test(expected = IndexException.class)
  public void testViewPartCardinality() {
    testG.viewPart(1, 8);
  }

  @Test
  public void testDecodeVector() throws Exception {
    Vector val = AbstractVector.decodeVector(testG.asFormatString());
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', testG.get(i), val.get(i), EPSILON);
    }
  }

  //	@Test
  //	public void testSparseDoubleVectorInt() throws Exception {
  //		Vector val = new RandomAccessSparseVector(4);
  //		assertEquals("size", 4, val.size());
  //		for (int i = 0; i < 4; i++) {
  //			assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
  //		}
  //	}
  //
  @Test
  public void testDot() throws Exception {
    double res = testLinear.dot(testLinear);
    double d1 = testLinear.get(0) * testLinear.get(0);
    double expected = d1 + (testLinear.get(1) * testLinear.get(1));
    assertEquals("dot", expected, res, EPSILON);
  }

  //	@Test
  //	public void testDot2() throws Exception {
  //		Vector test2 = test5_10.clone();
  //		test2.set(1, 0.0);
  //		test2.set(3, 0.0);
  //		assertEquals(3.3 * 3.3, test2.dot(test5_10), EPSILON);
  //	}
  //
  @Test(expected = CardinalityException.class)
  public void testDotCardinality() {
    testLinear.dot(new DenseVector(testLinear.size() + 1));
  }

  //	@Test
  //	public void testNormalize() throws Exception {
  //		Vector val = test2.normalize();
  //		double[] values = {test2.get(0), test2.get(1)};
  //		double mag = Math.sqrt(test2.get(0) * test2.get(0) + test2.get(1) + test2.get(1));
  //		for (int i = 0; i < test2.size(); i++) {
  //			if (i % 2 == 0) {
  //				assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
  //			} else {
  //				assertEquals("dot", val.getQuick(i) / mag, val.get(i), EPSILON);
  //			}
  //		}
  //	}
  //
  @Test
  public void testMinus() throws Exception {
    Vector val = testG.minus(testG);
    assertEquals("size", testG.size(), val.size());
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
    }

    val = testG.minus(testG).minus(testG);
    assertEquals("cardinality", testG.size(), val.size());
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', 0.0, val.get(i) + testG.get(i), EPSILON);
    }

    Vector val1 = testG.plus(1);
    val = val1.minus(testG);
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', 1.0, val.get(i), EPSILON);
    }

    val1 = testG.plus(-1);
    val = val1.minus(testG);
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', -1.0, val.get(i), EPSILON);
    }
  }



  @Test
  public void testPlusDouble() throws Exception {
    Vector val = testBig.plus(1);
    assertEquals("size", testBig.size(), val.size());
    for (int i = 0; i < testBig.size(); i++) {
      assertEquals("get [" + i + ']', testBig.get(i) + 1, val.get(i), EPSILON);
    }
  }

  @Test
  public void testPlusVector() throws Exception {
    Vector val = testBig.plus(testBig);
    assertEquals("size", testBig.size(), val.size());
    for (int i = 0; i < testBig.size(); i++) {
      assertEquals("get [" + i + ']', testBig.get(i) * 2, val.get(i), EPSILON);
    }
  }

  @Test
  public void testTimesDouble() throws Exception {
    Vector val = testBig.times(3);
    assertEquals("size", testBig.size(), val.size());
    for (int i = 0; i < testBig.size(); i++) {
      assertEquals("get [" + i + ']', testBig.get(i) * 3, val.get(i), EPSILON);
    }
  }

  @Test
  public void testDivideDouble() throws Exception {
    Vector val = testBig.divide(3);
    assertEquals("size", testBig.size(), val.size());
    for (int i = 0; i < testBig.size(); i++) {
      assertEquals("get [" + i + ']', testBig.get(i) / 3, val.get(i), EPSILON);
    }
  }

  @Test
  public void testTimesVector() throws Exception {
    Vector val = testG.times(testG);
    assertEquals("size", testG.size(), val.size());
    for (int i = 0; i < testG.size(); i++) {
      assertEquals("get [" + i + ']', testG.getQuick(i) * testG.getQuick(i),
          val.get(i), EPSILON);
    }
  }

  @Test(expected = CardinalityException.class)
  public void testPlusVectorCardinality() {
    testG.plus(new DenseVector(testG.size() + 1));
  }

  @Test(expected = CardinalityException.class)
  public void testTimesVectorCardinality() {
    testG.times(new DenseVector(testG.size() + 1));
  }

  @Test
  public void testZSum() {
    double zSum = testLinear.zSum();
    assertTrue("linear zSum > 0", zSum > 0);
    assertTrue("linear zSum < length", zSum <= testG.size());
    zSum = 0.0;
    for(int index = 0; index < testG.size(); index++) {
      zSum += testG.getQuick(index);
    }
    assertTrue("gaussian zSum correct", zSum == testG.zSum());
    assertTrue("gaussian zSum range", testG.zSum() > -10.0 && testG.zSum() < 10.0);
  }

  @Test
  public void testGetDistanceSquared() {
    Vector other = new RandomAccessSparseVector(testG.size());
    other.set(1, -2);
    other.set(2, -5);
    other.set(3, -9);
    double expected = testG.minus(other).getLengthSquared();
    assertTrue("a.getDistanceSquared(b) != a.minus(b).getLengthSquared",
        Math.abs(expected - testG.getDistanceSquared(other)) < 10.0E-7);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDouble() {
    testBig.assign(0);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignVector() throws Exception {
    Vector other = new DenseVector(testBig.size());
    testBig.assign(other);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignUnaryFunction() {
    testBig.assign(Functions.NEGATE);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignBinaryFunction() throws Exception {
    testBig.assign(testBig, Functions.PLUS);
  }

  @Test
  public void testLike() {
    Vector other = testG.like();
    assertEquals("size", testG.size(), other.size());
    other.assign(3.0);
  }

  @Test
  public void testCrossProduct() {
    Matrix result = testG.cross(testG);
    assertEquals("row size", testG.size(), result.size()[0]);
    assertEquals("col size", testG.size(), result.size()[1]);
    for (int row = 0; row < result.size()[0]; row++) {
      for (int col = 0; col < result.size()[1]; col++) {
        assertEquals("cross[" + row + "][" + col + ']', testG.getQuick(row)
            * testG.getQuick(col), result.getQuick(row, col), EPSILON);
      }
    }
  }

  @Test
  public void testRank() {
    int seed = 1000;
    Matrix rm = new DenseMatrix(30,50);
    for(int row = 0; row < 30; row++) {
      Vector v = new RandomVector(50, seed, 1, RandomMatrix.LINEAR);
      rm.assignRow(row, v);
      seed += 1000;
    }
    SingularValueDecomposition svd = new SingularValueDecomposition(rm);
    assertTrue("Random Vector rows must be full rank", svd.rank() == rm.rowSize());
    for(int column = 0; column < 50; column++) {
      Vector v = new RandomVector(30, seed, 1, RandomMatrix.LINEAR);
      rm.assignColumn(column, v);
      seed += 1000;
    }
    svd = new SingularValueDecomposition(rm);
    assertTrue("Random Vector columns must be full rank", svd.rank() == rm.rowSize());

  }

}
