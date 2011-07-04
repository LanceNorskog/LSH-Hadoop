package org.apache.mahout.math.simplex;

import org.apache.mahout.math.MahoutTestCase;
import org.junit.Test;

public class TestHasher extends MahoutTestCase {
  
  @Test
  public void testOrthoNormal() {
    int dim = 4;
    double[] values = new double[dim];

    Hasher hasher = new OrthonormalHasher(dim, 0.5);
    int[] hashed = new int[dim];
    values[0] = 0.1;
    values[1] = 1.1;
    values[2] = 2.1;
    values[3] = -0.9;
    double factor = hasher.getFactor(values);
    hasher.hashDense(values, hashed, factor);
    assertEquals(0, hashed[0]);
    assertEquals(2, hashed[1]);
    assertEquals(4, hashed[2]);
    assertEquals(-2, hashed[3]);
  }
  
  @Test
  public void testVertexTransitive() {
    int dim = 4;
    double[] values = new double[dim];

    Hasher hasher = new VertexTransitiveHasher(dim, 0.5);
    int[] hashed = new int[dim];
    values[0] = 0.1;
    values[1] = 1.1;
    values[2] = 2.1;
    values[3] = -0.9;
    double factor = hasher.getFactor(values);
    assertEquals(4.8, factor, EPSILON);
    hasher.hashDense(values, hashed, factor);
    assertEquals(1, hashed[0]);
    assertEquals(2, hashed[1]);
    assertEquals(3, hashed[2]);
    assertEquals(-1, hashed[3]);
  }

}
