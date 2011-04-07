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

package org.apache.mahout.common.distance;

import java.util.Random;

import org.apache.mahout.common.MahoutTestCase;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

public final class TestMinkowskiMeasure extends MahoutTestCase {
  
  static double EPSILON = 0.05;
  
  @Test
  public void testMeasure() {
    
    DistanceMeasure minkowskiDistanceMeasure = new MinkowskiDistanceMeasure(1.5, false);
    DistanceMeasure manhattanDistanceMeasure = new ManhattanDistanceMeasure();
    DistanceMeasure euclideanDistanceMeasure = new EuclideanDistanceMeasure();
    
    Vector[] vectors = {
        new DenseVector(new double[]{1, 0, 0, 0, 0, 0}),
        new DenseVector(new double[]{1, 1, 1, 0, 0, 0}),
        new DenseVector(new double[]{1, 1, 1, 1, 1, 1})
    };
    
    double[][] minkowskiDistanceMatrix = new double[3][3];
    double[][] manhattanDistanceMatrix = new double[3][3];
    double[][] euclideanDistanceMatrix = new double[3][3];
    
    for (int a = 0; a < 3; a++) {
      for (int b = 0; b < 3; b++) {
        minkowskiDistanceMatrix[a][b] = minkowskiDistanceMeasure.distance(vectors[a], vectors[b]);
        manhattanDistanceMatrix[a][b] = manhattanDistanceMeasure.distance(vectors[a], vectors[b]);
        euclideanDistanceMatrix[a][b] = euclideanDistanceMeasure.distance(vectors[a], vectors[b]);
      }
    }
    
    for (int a = 0; a < 3; a++) {
      for (int b = 0; b < 3; b++) {
        assertTrue(minkowskiDistanceMatrix[a][b] <= manhattanDistanceMatrix[a][b]);
        assertTrue(minkowskiDistanceMatrix[a][b] >= euclideanDistanceMatrix[a][b]);
      }
    }
    
    assertEquals(0.0, minkowskiDistanceMatrix[0][0], EPSILON);
    assertTrue(minkowskiDistanceMatrix[0][0] < minkowskiDistanceMatrix[0][1]);
    assertTrue(minkowskiDistanceMatrix[0][1] < minkowskiDistanceMatrix[0][2]);
  }
  
  
  
  @Test
  public void testApproximation() {
    
    Vector[] vectors = {
        new DenseVector(new double[]{1, 0, 0, 0, 0, 0}),
        new DenseVector(new double[]{1, 1, 1, 0, 0, 0}),
        new DenseVector(new double[]{1, 1, 1, 1, 1, 1})
    };
    
    double exponent = 2.0;
    drive(vectors[0], vectors[1], 50);
    drive(vectors[0], vectors[2], 50);
    drive(vectors[1], vectors[2], 50);

    double[] huge = new double[100000];
    double[] huge2 = new double[100000];
    Random rnd = new Random(0);
    for(int i = 0; i < huge.length; i++) {
      huge[i] = rnd.nextGaussian();
    }
    for(int i = 0; i < huge.length; i++) {
      huge2[i] = rnd.nextDouble() * rnd.nextLong();
    }
    drive(new DenseVector(huge), new DenseVector(huge2), 0.02);

  }
  
  private void drive(Vector v1, Vector v2, double exponent) {
    DistanceMeasure approx = new MinkowskiDistanceMeasure(exponent, false);
    DistanceMeasure exact = new MinkowskiDistanceMeasure(exponent, true);
//    DistanceMeasure approx = new TanimotoDistanceMeasure();
//    DistanceMeasure exact = approx;
    check(v1, v2, exact, approx);
    
  }
  
  private void check(Vector v1, Vector v2, DistanceMeasure exact, DistanceMeasure approx) {
    double lesser = Math.min(exact.distance(v1, v2), approx.distance(v1, v2));
    double greater = Math.max(exact.distance(v1, v2), approx.distance(v1, v2));
    assertTrue(lesser != Double.POSITIVE_INFINITY && greater != Double.NEGATIVE_INFINITY);
    if (greater < 0.0000001)
      return;
    if (lesser > 100000000d)
      return;
    assertTrue(greater / lesser < 1.0 + EPSILON);
  }
}
