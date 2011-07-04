package org.apache.mahout.math.simplex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.simplex.Simplex;
import org.apache.mahout.math.simplex.SimplexIterator;
import org.junit.Assert;
import org.junit.Test;

import com.sun.org.apache.xml.internal.utils.Trie;

public final class TestSimplexIterator extends Assert {
  static double EPSILON = 0.0000001;
  
  // Orthonormal hashing: every square is diagonally bisected.
  // lower left -> upper right
  // this point is in the lower right simplex
  double[] lowerData = {1.2, 2.9};
  double[] lowerQuantized = {1.0,2.0};
  int[][] lowerHashed = {
      {1,2,},   // ll
      {2,2,},   // lr
      {2,3,},   // ur
  };
  double lowerFactor = 3.1;
  boolean[] lowerNabes = {true, false};
  
  // this point is in the upper left simplex
  double[] upperData = {1.2, 2.9};
  double[] upperQuantized = {1.0,2.0};
  int[][] upperHashed = {
      {1,2,},   // ll
      {1,3,},   // ul
      {2,3,},   // ur
  };
  double upperFactor = 3.1;
  boolean[] upperNabes = {false, true};
  
  // iterates over lower right triangle
  @Test
  public void TestOrtho2dLower() throws Exception {
    int dim = 2;
    
    Hasher hasher = new OrthonormalHasher(dim, 1.0);
    Vector lower = new DenseVector(lowerData);
    SimplexIterator<String> sit = new SimplexIterator<String>(hasher, lower);
    verify(dim, lowerHashed, sit, lowerFactor);
  }
  
  // iterates over lower right triangle
  @Test
  public void TestOrtho2dUpper() throws Exception {
    int dim = 2;
    
    Hasher hasher = new OrthonormalHasher(dim, 1.0);
    Vector upper = new DenseVector(upperData);
    SimplexIterator sit = new SimplexIterator(hasher, upper);
    verify(dim, upperHashed, sit, upperFactor);
  }
  
  private void verify(int dim, int[][] hashed,
      SimplexIterator sit, double factor) {
    Set<Simplex> saved = new HashSet<Simplex>();
    int count = 0;
    int ones = 0;
    while (sit.hasNext()) {
      Simplex s = sit.next();
      int[] hash = s.getValues();
      int[] orig = hashed[count];
      saved.add(s);
      Simplex<String> base = new Simplex<String>(orig, null, factor, "");
      assert(base.equals(s) && s.equals(base));
      count++;
    }
    assertTrue(saved.size() == (dim + 1));
    
  }
  
  @Test
  public void testLarge() {
    Random rnd = new Random(0);
    int dim = 500;
    double[] big = new double[dim];
    for(int i = 0; i < dim; i++ ) {
      big[i] = rnd.nextInt() + rnd.nextGaussian();
    }
    Hasher hasher = new OrthonormalHasher(dim, 1.0);
    int[] hashed = new int[dim];
    double factor = hasher.getFactor(big);
    hasher.hashDense(big, hashed, factor);
    SimplexIterator sit = new SimplexIterator(hasher, (Vector) new DenseVector(big));
    distances(dim, sit, hashed, null);
  }
  
  private void distances(int dim, SimplexIterator sit, int[] orig, boolean[] neighbors) {
    Set<Simplex> saved = new HashSet<Simplex>();
    int count = 0;
    int ones = 0;
    
    while (sit.hasNext()) {
      Simplex s = sit.next();
      int[] hash = s.getValues();
      saved.add(s);
      for(int i = 0; i < dim; i++) {
        int delta = hash[i] - orig[i];
        assertTrue("Delta not 0 or 1", delta == 0 | delta == 1);
        if (delta == 1)
          ones++;
      }
      if (null != neighbors) {
        boolean[] nabes = s.neighbors;
        for(int i = 0; i < dim; i++) {
          assertEquals("neighbors", neighbors[i], nabes[i]);
        }
      }
      count++;
    }
    assertTrue(saved.size() == (dim + 1));
    
  }
  
  //    for(int i = 0; i < dim; i++) {
  //      assertTrue(q1.get(i) < lower.get(i));
  //    }
  //    
  //    int[] h1 = sit.getHash(lower);
  //    for(int i = 0; i < dim; i++) {
  //      assertEquals(q1.get(i), lowerQuantized[i], EPSILON);
  //    }
  //    
  //    // check list of nearest. Do not know what order the iteration will produce.
  //    int count = 0;
  //    Iterator<Vector> it = sit.getNearest(lower);
  //    for(int i = 0; i < dim + 1; i++) {
  //      int j = 0;
  //      Vector x = it.next();
  //      for(; j < dim; j++) {
  //        if (lowerHashed[i][j] != x.get(j))
  //          break;
  //      }
  //      if (j == dim)
  //        count++;
  //    }
  //    assertEquals(count, dim + 1);
  //  }
  //  
}
