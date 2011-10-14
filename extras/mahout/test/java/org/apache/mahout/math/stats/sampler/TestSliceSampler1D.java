package org.apache.mahout.math.stats.sampler;

import java.util.Random;

import org.apache.mahout.math.MahoutTestCase;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.SquareRootFunction;
import org.junit.Test;


public class TestSliceSampler1D extends MahoutTestCase {
  boolean debug = true;
  
  @Test
  public void testDist() {
    SampleFunction<String> toX = new String2Double();
    int N = 3000;
    DoubleFunction dist = new SeesawFunction(N);
    if (debug) {
      System.out.print("Distribution");
      for(int i = 0; i < N - "Distribution".length(); i++) {
        System.out.print(' ');
      }
      System.out.println("\tDensity");
    }
    for(int seed = 0; seed < 10; seed++) {
      int low = 0;
      int high = 0;
      Random rnd = new Random(seed);
      SliceSampler1D<String> sampler = new SliceSampler1D<String>(toX, dist, rnd, 0, N);
      for(int i = 0; i < N; i++) {
        boolean sampled = sampler.isSampled("" + i);
        if (i < N/2 && sampled)
          low++;
        if (i > N/2 && sampled)
          high++;
        if (debug) {
          System.out.print(sampled ? "+" : ".");
          if (i % 100 == 0)
            System.out.println();
        }
      }
//      assertTrue("Lower half failed", low < (N * 0.4));
//      assertTrue("Upper half failed", high > (N * 0.2));
      if (debug)
        System.out.println("\t" + low + "\t" + high);
    }
  }
}

class RandomSquared extends Random {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public RandomSquared(long seed) {
    super(seed);
  }

  @Override
  public double nextDouble() {
    this.setSeed(super.nextLong());
    return super.nextDouble();
  }
}
