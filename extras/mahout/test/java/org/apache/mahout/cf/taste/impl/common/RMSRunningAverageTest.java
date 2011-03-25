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

package org.apache.mahout.cf.taste.impl.common;

import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.common.RandomUtils;
import org.junit.Test;

import java.util.Random;

public final class RMSRunningAverageTest extends TasteTestCase {

	private static final double SMALL_EPSILON = 1.0;
	private static final double BIG_EPSILON = 100 * SMALL_EPSILON;

	@Test
	public void testFull() {
		doTestRMSAverageAndStdDev(new RMSRunningAverage());
	}

	@Test
	public void testBig() {
	  doTestBig(new RMSRunningAverageAndStdDev(), BIG_EPSILON);
	}
	
	private static void doTestRMSAverageAndStdDev(RunningAverage average) {
		RunningAverageAndStdDev stDev = null;
		if (average instanceof RunningAverageAndStdDev)
			stDev = (RunningAverageAndStdDev) average;

		assertEquals(0, average.getCount());
		assertTrue(Double.isNaN(average.getAverage()));
		if (null != stDev)
			assertTrue(Double.isNaN(stDev.getStandardDeviation()));

		average.addDatum(6.0);
		assertEquals(1, average.getCount());
		assertEquals(6.0, average.getAverage(), EPSILON);
		if (null != stDev)
			assertTrue(Double.isNaN(stDev.getStandardDeviation()));

		average.addDatum(6.0);
		assertEquals(2, average.getCount());
		assertEquals(6.0, average.getAverage(), EPSILON);
		if (null != stDev)
			assertEquals(0.0, stDev.getStandardDeviation(), EPSILON);

		average.addDatum(-4.0);
		assertEquals(3, average.getCount());
		assertEquals(5.41602560309064, average.getAverage(), EPSILON);
		if (null != stDev)
			assertEquals(5.773502691896257, stDev.getStandardDeviation(), EPSILON);

	}

	private static void doTestBig(RunningAverageAndStdDev average, double epsilon) {

		Random r = RandomUtils.getRandom();
		for (int i = 0; i < 100000; i++) {
			average.addDatum(r.nextDouble() * 1000.0);
		}
		assertEquals(500.0, average.getAverage(), epsilon);
		assertEquals(1000.0 / Math.sqrt(12.0), average.getStandardDeviation(), epsilon);

	}

}
