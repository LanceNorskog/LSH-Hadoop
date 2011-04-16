package org.apache.mahout.math.quantize;

import java.util.Iterator;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.junit.Assert;
import org.junit.Test;

import lsh.mahout.core.Hasher;
import lsh.mahout.core.OrthonormalHasher;
import lsh.mahout.quantizer.DenseLSHQuantizer;

public final class TestDenseLSHQuantizer extends Assert {
  static double EPSILON = 0.0000001;
  
  double[] lowerData = {1.2, 2.9};
  double[] lowerQuantized = {1.0,2.0};
  double[][] lowerHashed = {
      {1,2,},
      {1,3,},
      {2,3,},
  };
  
  double[] v2Data = {1.2, 2.9};
  double[] v2Quantized = {1.0,2.0};
  double[][] v2Hashed = {
      {1,2,},
      {1,3,},
      {2,3,},
  };
  
  // iterates over lower right triangle
  @Test
  public void TestOrtho2dLower() throws Exception {
    int dim = 2;
    
    Hasher hasher = new OrthonormalHasher(dim, 1.0);
    DenseLSHQuantizer vlq = new DenseLSHQuantizer(hasher);
    Vector lower = new DenseVector(lowerData);
    Vector q = vlq.quantize(lower);
    verify(dim, lowerQuantized, lowerHashed, vlq, lower, q);
  }
  
  // iterates over lower right triangle
  @Test
  public void TestOrtho2dUpper() throws Exception {
    int dim = 2;
    
    Hasher hasher = new OrthonormalHasher(dim, 1.0);
    DenseLSHQuantizer vlq = new DenseLSHQuantizer(hasher);
    Vector upper = new DenseVector(v2Data);
    Vector q = vlq.quantize(upper);
    verify(dim, v2Quantized, v2Hashed, vlq, upper, q);
  }
  
  private void verify(int dim, double[] lowerQuantized, double[][] lowerHashed,
      DenseLSHQuantizer vlq, Vector lower, Vector q1) {
    for(int i = 0; i < dim; i++) {
      assertTrue(q1.get(i) < lower.get(i));
    }
    
    int[] h1 = vlq.getHash(lower);
    for(int i = 0; i < dim; i++) {
      assertEquals(q1.get(i), lowerQuantized[i], EPSILON);
    }
    
    // check list of nearest. Do not know what order the iteration will produce.
    int count = 0;
    Iterator<Vector> it = vlq.getNearest(lower);
    for(int i = 0; i < dim + 1; i++) {
      int j = 0;
      Vector x = it.next();
      for(; j < dim; j++) {
        if (lowerHashed[i][j] != x.get(j))
          break;
      }
      if (j == dim)
        count++;
    }
    assertEquals(count, dim + 1);
  }
  
}
