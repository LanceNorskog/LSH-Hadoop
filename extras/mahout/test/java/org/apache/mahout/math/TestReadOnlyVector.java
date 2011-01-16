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

import java.util.Iterator;

/*
 * AbstractTestVector assumes all vectors are writable.
 */

public abstract class TestReadOnlyVector extends MahoutTestCase {
  
  protected ReadOnlyVector four;
  protected ReadOnlyVector twoK;
  
  @Override
  public void setUp() throws Exception {
    four = generateTestVector(4);
    twoK = generateTestVector(2000);
  }
  
  abstract ReadOnlyVector generateTestVector(int cardinality);

  /* TODO: why doesn't this work? some library problem? 
  @Test
  public void testAsFormatString() {
    String formatString = twoK.asFormatString();
    Vector vec = AbstractVector.decodeVector(formatString);
    assertEquals(vec, twoK);
  }
  */

  @Test
  public void testCardinality() {
    assertEquals("size", 2000, twoK.size());
  }

  @Test
  public void testIterator() throws Exception {
    DenseVector copy = new DenseVector(four);
    double[] gold = new double[copy.size()];
    for(int i = 0; i < gold.length; i++)
      gold[i] = copy.getQuick(i);
    Iterator<Vector.Element> iterator = four.iterateNonZero();
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
    Vector copy = twoK.clone();
    for (int i = 0; i < twoK.size(); i++) {
      assertEquals("copy [" + i + ']', twoK.get(i), copy.get(i), EPSILON);
    }
  }

  @Test(expected = IndexException.class)
  public void fouretOver() {
    twoK.get(twoK.size());
  }

  @Test(expected = IndexException.class)
  public void fouretUnder() {
    twoK.get(-1);
  }

  @Test
  public void testViewPart() throws Exception {
    Vector part = twoK.viewPart(1, 2);
    assertEquals("part size", 2, part.getNumNondefaultElements());
    for (int i = 0; i < part.size(); i++) {
      assertEquals("part[" + i + ']', twoK.get(i+1), part.get(i), EPSILON);
    }
  }

  @Test(expected = IndexException.class)
  public void testViewPartUnder() {
    twoK.viewPart(-1, twoK.size());
  }

  @Test(expected = IndexException.class)
  public void testViewPartOver() {
    four.viewPart(2, 7);
  }

  @Test(expected = IndexException.class)
  public void testViewPartCardinality() {
    four.viewPart(1, 8);
  }

  /*
  @Test
  public void testDecodeVector() throws Exception {
    Vector val = AbstractVector.decodeVector(four.asFormatString());
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', four.get(i), val.get(i), EPSILON);
    }
  }
  */

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
    double res = four.dot(four);
    double expected = 0.0;
    for(int i = 0; i < 4; i++) {
      double value = four.get(i);
      expected += value * four.get(i);
    }
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
    twoK.dot(new DenseVector(twoK.size() + 1));
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
    Vector val = four.minus(four);
    assertEquals("size", four.size(), val.size());
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
    }

    val = four.minus(four).minus(four);
    assertEquals("cardinality", four.size(), val.size());
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', 0.0, val.get(i) + four.get(i), EPSILON);
    }

    Vector val1 = four.plus(1);
    val = val1.minus(four);
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', 1.0, val.get(i), EPSILON);
    }

    val1 = four.plus(-1);
    val = val1.minus(four);
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', -1.0, val.get(i), EPSILON);
    }
  }



  @Test
  public void testPlusDouble() throws Exception {
    Vector val = twoK.plus(1);
    assertEquals("size", twoK.size(), val.size());
    for (int i = 0; i < twoK.size(); i++) {
      assertEquals("get [" + i + ']', twoK.get(i) + 1, val.get(i), EPSILON);
    }
  }

  @Test
  public void testPlusVector() throws Exception {
    Vector val = twoK.plus(twoK);
    assertEquals("size", twoK.size(), val.size());
    for (int i = 0; i < twoK.size(); i++) {
      assertEquals("get [" + i + ']', twoK.get(i) * 2, val.get(i), EPSILON);
    }
  }

   @Test
  public void testTimesDouble() throws Exception {
    Vector val = twoK.times(3);
    assertEquals("size", twoK.size(), val.size());
    for (int i = 0; i < twoK.size(); i++) {
      assertEquals("get [" + i + ']', twoK.get(i) * 3, val.get(i), EPSILON);
    }
  }

  @Test
  public void testDivideDouble() throws Exception {
    Vector val = twoK.divide(3);
    assertEquals("size", twoK.size(), val.size());
    for (int i = 0; i < twoK.size(); i++) {
      assertEquals("get [" + i + ']', twoK.get(i) / 3, val.get(i), EPSILON);
    }
  }

  @Test
  public void testTimesVector() throws Exception {
    Vector val = four.times(four);
    assertEquals("size", four.size(), val.size());
    for (int i = 0; i < four.size(); i++) {
      assertEquals("get [" + i + ']', four.getQuick(i) * four.getQuick(i),
          val.get(i), EPSILON);
    }
  }

  @Test(expected = CardinalityException.class)
  public void testPlusVectorCardinality() {
    four.plus(new DenseVector(four.size() + 1));
  }

  @Test(expected = CardinalityException.class)
  public void testTimesVectorCardinality() {
    four.times(new DenseVector(four.size() + 1));
  }

  @Test
  public void testZSum() {
    assertEquals("linear zSum < length", four.zSum(), 2.5 * 4, EPSILON);
  }

  @Test
  public void fouretDistanceSquared() {
    Vector other = new RandomAccessSparseVector(four.size());
    other.set(1, -2);
    other.set(2, -5);
    other.set(3, -9);
    double expected = four.minus(other).getLengthSquared();
    assertTrue("a.getDistanceSquared(b) != a.minus(b).getLengthSquared",
        Math.abs(expected - four.getDistanceSquared(other)) < 10.0E-7);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignDouble() {
    twoK.assign(0);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignVector() throws Exception {
    Vector other = new DenseVector(twoK.size());
    twoK.assign(other);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignUnaryFunction() {
    twoK.assign(Functions.NEGATE);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testAssignBinaryFunction() throws Exception {
    twoK.assign(twoK, Functions.PLUS);
  }

  @Test
  public void testLike() {
    Vector other = four.like();
    assertEquals("size", four.size(), other.size());
    other.assign(3.0);
  }

  @Test
  public void testCrossProduct() {
    Matrix result = four.cross(four);
    assertEquals("row size", four.size(), result.size()[0]);
    assertEquals("col size", four.size(), result.size()[1]);
    for (int row = 0; row < result.size()[0]; row++) {
      for (int col = 0; col < result.size()[1]; col++) {
        assertEquals("cross[" + row + "][" + col + ']', four.getQuick(row)
            * four.getQuick(col), result.getQuick(row, col), EPSILON);
      }
    }
  }

}
