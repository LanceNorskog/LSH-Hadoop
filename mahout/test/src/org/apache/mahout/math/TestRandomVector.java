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

import org.junit.Test;

import java.util.Iterator;

public class TestRandomVector extends MahoutTestCase {

	static private RandomVector test2 = new RandomVector(2);
	static private RandomVector testFull = new RandomVector(4);
	static private RandomVector test01 = new RandomVector(4, 0, 1, 0.0, 1.0, false);
	static private RandomVector test5_10 = new RandomVector(4, 0, 1, 5.0, 10.0, false);

	@Test
	public void testAsFormatString() {
		String formatString = testFull.asFormatString();
		Vector vec = AbstractVector.decodeVector(formatString);
		assertEquals(vec, testFull);
	}

	@Test
	public void testCardinality() {
		assertEquals("size", 4, testFull.size());
	}

	@Test
	public void testIterator() throws Exception {
		DenseVector copy = new DenseVector(test5_10);
		double[] gold = new double[copy.size()];
		for(int i = 0; i < gold.length; i++)
			gold[i] = copy.getQuick(i);
		Iterator<Vector.Element> iterator = test5_10.iterateNonZero();
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
		Vector copy = testFull.clone();
		for (int i = 0; i < testFull.size(); i++) {
			assertEquals("copy [" + i + ']', testFull.get(i), copy.get(i), EPSILON);
		}
	}

	@Test(expected = IndexException.class)
	public void testGetOver() {
		testFull.get(testFull.size());
	}

	@Test(expected = IndexException.class)
	public void testGetUnder() {
		testFull.get(-1);
	}

	@Test
	public void testViewPart() throws Exception {
		Vector part = testFull.viewPart(1, 2);
		assertEquals("part size", 2, part.getNumNondefaultElements());
		for (int i = 0; i < part.size(); i++) {
			assertEquals("part[" + i + ']', testFull.get(i+1), part.get(i), EPSILON);
		}
	}

	@Test(expected = IndexException.class)
	public void testViewPartUnder() {
		testFull.viewPart(-1, testFull.size());
	}

	@Test(expected = IndexException.class)
	public void testViewPartOver() {
		test5_10.viewPart(2, 7);
	}

	@Test(expected = IndexException.class)
	public void testViewPartCardinality() {
		test5_10.viewPart(1, 8);
	}

	@Test
	public void testDecodeVector() throws Exception {
		Vector val = AbstractVector.decodeVector(test5_10.asFormatString());
		for (int i = 0; i < test5_10.size(); i++) {
			assertEquals("get [" + i + ']', test5_10.get(i), val.get(i), EPSILON);
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
		double res = test2.dot(test2);
		double d1 = test2.get(0) * test2.get(0);
		double expected = d1 + (test2.get(1) * test2.get(1));
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
		test2.dot(new DenseVector(test2.size() + 1));
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
		Vector val = test5_10.minus(test5_10);
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
		}

		val = test5_10.minus(test5_10).minus(test5_10);
		assertEquals("cardinality", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			assertEquals("get [" + i + ']', 0.0, val.get(i) + test5_10.get(i), EPSILON);
		}

		Vector val1 = test5_10.plus(1);
		val = val1.minus(test5_10);
		for (int i = 0; i < test5_10.size(); i++) {
			assertEquals("get [" + i + ']', 1.0, val.get(i), EPSILON);
		}

		val1 = test5_10.plus(-1);
		val = val1.minus(test5_10);
		for (int i = 0; i < test5_10.size(); i++) {
			assertEquals("get [" + i + ']', -1.0, val.get(i), EPSILON);
		}
	}

	@Test
	public void testPlusDouble() throws Exception {
		Vector val = test5_10.plus(1);
		Vector gold = test5_10.getDense();
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 1.0, val.get(i), EPSILON);
			} else {
				assertEquals("get [" + i + ']', gold.getQuick(i/2) + 1.0, val.get(i), EPSILON);
			}
		}
	}

	@Test
	public void testPlusVector() throws Exception {
		Vector val = test5_10.plus(test5_10);
		Vector gold = test5_10.getDense();
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
			} else {
				assertEquals("get [" + i + ']', gold.getQuick(i/2) * 2.0, val.get(i), EPSILON);
			}
		}
	}

	@Test(expected = CardinalityException.class)
	public void testPlusVectorCardinality() {
		test5_10.plus(new DenseVector(test5_10.size() + 1));
	}

	@Test
	public void testTimesDouble() throws Exception {
		Vector val = test5_10.times(3);
		Vector gold = test5_10.getDense();
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
			} else {
				assertEquals("get [" + i + ']', gold.getQuick(i/2) * 3.0, val.get(i), EPSILON);
			}
		}
	}

	@Test
	public void testDivideDouble() throws Exception {
		Vector val = test5_10.divide(3);
		Vector gold = test5_10.getDense();
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
			} else {
				assertEquals("get [" + i + ']', gold.getQuick(i/2) / 3.0, val.get(i), EPSILON);
			}
		}
	}

	/*
	@Test
	public void testTimesVector() throws Exception {
		Vector val = test5_10.times(test5_10);
		Vector gold = test5_10.getDense();
		assertEquals("size", test5_10.size(), val.size());
		for (int i = 0; i < test5_10.size(); i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, val.get(i), EPSILON);
			} else {
				assertEquals("get [" + i + ']', gold.getQuick(i/2) * v, val.get(i), EPSILON);
			}
		}
	}
	*/

	@Test(expected = CardinalityException.class)
	public void testTimesVectorCardinality() {
		test5_10.times(new DenseVector(test5_10.size() + 1));
	}

	
	@Test
	public void testZSum() {
		double expected = 0;
		for (int i = 0; i < test5_10.size(); i++) {
			expected += test5_10.getQuick(i);
		}
		assertEquals("wrong zSum", expected, test5_10.zSum(), EPSILON);
	}
	

	@Test
	public void testGetDistanceSquared() {
		Vector other = new RandomAccessSparseVector(test5_10.size());
		other.set(1, -2);
		other.set(2, -5);
		other.set(3, -9);
		double expected = test5_10.minus(other).getLengthSquared();
		assertTrue("a.getDistanceSquared(b) != a.minus(b).getLengthSquared",
				Math.abs(expected - test5_10.getDistanceSquared(other)) < 10.0E-7);
	}

/*	@Test
	public void testAssignDouble() {
		test5_10.assign(0);
		for (int i = 0; i < values.length; i++) {
			assertEquals("value[" + i + ']', 0.0, test5_10.getQuick(i), EPSILON);
		}
	}

	@Test
	public void testAssignDoubleArray() throws Exception {
		double[] array = new double[test5_10.size()];
		test5_10.assign(array);
		for (int i = 0; i < values.length; i++) {
			assertEquals("value[" + i + ']', 0.0, test5_10.getQuick(i), EPSILON);
		}
	}

	@Test(expected = CardinalityException.class)
	public void testAssignDoubleArrayCardinality() {
		double[] array = new double[test5_10.size() + 1];
		test5_10.assign(array);
	}

	@Test
	public void testAssignVector() throws Exception {
		Vector other = new DenseVector(test5_10.size());
		test5_10.assign(other);
		for (int i = 0; i < values.length; i++) {
			assertEquals("value[" + i + ']', 0.0, test5_10.getQuick(i), EPSILON);
		}
	}

	@Test(expected = CardinalityException.class)
	public void testAssignVectorCardinality() {
		Vector other = new DenseVector(test5_10.size() - 1);
		test5_10.assign(other);
	}

	@Test
	public void testAssignUnaryFunction() {
		test5_10.assign(Functions.NEGATE);
		for (int i = 1; i < values.length; i += 2) {
			assertEquals("value[" + i + ']', -values[i], test5_10.getQuick(i+2), EPSILON);
		}
	}

	@Test
	public void testAssignBinaryFunction() throws Exception {
		test5_10.assign(test, Functions.PLUS);
		for (int i = 0; i < values.length; i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, test5_10.get(i), EPSILON);
			} else {
				assertEquals("value[" + i + ']', 2 * values[i - 1], test5_10.getQuick(i), EPSILON);
			}
		}
	}

	@Test
	public void testAssignBinaryFunction2() throws Exception {
		test5_10.assign(Functions.plus(4));
		for (int i = 0; i < values.length; i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 4.0, test5_10.get(i), EPSILON);
			} else {
				assertEquals("value[" + i + ']', values[i - 1] + 4, test5_10.getQuick(i), EPSILON);
			}
		}
	}

	@Test
	public void testAssignBinaryFunction3() throws Exception {
		test5_10.assign(Functions.mult(4));
		for (int i = 0; i < values.length; i++) {
			if (i % 2 == 0) {
				assertEquals("get [" + i + ']', 0.0, test5_10.get(i), EPSILON);
			} else {
				assertEquals("value[" + i + ']', values[i - 1] * 4, test5_10.getQuick(i), EPSILON);
			}
		}
	}*/

	@Test (expected = UnsupportedOperationException.class)
	public void testLike() {
		Vector other = test5_10.like();
		assertTrue("not like", test5_10.getClass().isAssignableFrom(other.getClass()));
		assertEquals("size", test5_10.size(), other.size());
	}

	@Test
	public void testCrossProduct() {
		Matrix result = test5_10.cross(test5_10);
		assertEquals("row size", test5_10.size(), result.size()[0]);
		assertEquals("col size", test5_10.size(), result.size()[1]);
		for (int row = 0; row < result.size()[0]; row++) {
			for (int col = 0; col < result.size()[1]; col++) {
				assertEquals("cross[" + row + "][" + col + ']', test5_10.getQuick(row)
						* test5_10.getQuick(col), result.getQuick(row, col), EPSILON);
			}
		}
	}

}
