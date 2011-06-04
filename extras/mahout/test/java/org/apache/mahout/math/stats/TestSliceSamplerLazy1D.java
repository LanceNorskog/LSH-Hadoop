package org.apache.mahout.math.stats;

import java.util.Random;

import org.apache.mahout.math.MahoutTestCase;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.SquareRootFunction;
import org.junit.Test;


public class TestSliceSamplerLazy1D extends MahoutTestCase {
  boolean debug = true;
  
  @Test
  public void testDist() {
    SampleFunction<String> toX = new String2Double();
    int N = 300;
    DoubleFunction dist = new SeesawFunction(N);
    int width = 1;
    if (debug) {
      System.out.print("Distribution");
      for(int i = 0; i < N - "Distribution".length(); i++) {
        System.out.print(' ');
      }
      System.out.println("\tDensity");
    }
    for(int seed = 0; seed < 10; seed++) {
      System.out.print("Seed: " + seed);
      int low = 0;
      int high = 0;
      int trueCount = 0;
      int falseCount = 0;
      Boolean[] save = new Boolean[N];
      Random rnd = new Random(seed * 387);
      SliceSamplerLazy1D<String> sampler = new SliceSamplerLazy1D<String>(toX, dist, rnd, 0, N, width);
      for(int i = 1; i < N; i++) {
        int r = rnd.nextInt(N);
        if (null != save[r])
          continue;
        boolean sampled = sampler.isSampled("" + r);
        save[r] = sampled;
        if (sampled) 
          trueCount++;
        else
          falseCount++;
      }
      for(int i = 1; i < N; i++) {
        int r = rnd.nextInt(N);
        if (null != save[r])
          continue;
        boolean sampled = sampler.isSampled("" + r);
        save[r] = sampled;
        if (sampled) 
          trueCount++;
        else
          falseCount++;
      }
      for(int i = 1; i < N; i++) {
        int r = rnd.nextInt(N);
        if (null != save[r])
          continue;
        boolean sampled = sampler.isSampled("" + r);
        save[r] = sampled;
        if (sampled) 
          trueCount++;
        else
          falseCount++;
      }
      System.out.println(", true/false: " + trueCount + ", " + falseCount);
      for(int i = 1; i < N; i++) {
        if (null == save[i]) {
          System.out.print(' ');
        } else {
          boolean sampled = save[i];
          if (i < N/2 && sampled)
            low++;
          if (i > N/2 && sampled)
            high++;
          if (debug) {
            System.out.print(sampled ? "+" : ".");
          } 
          //      assertTrue("Lower half failed", low < (N * 0.4));
          //      assertTrue("Upper half failed", high > (N * 0.2));
          //          if (debug)
          //            System.out.println("\t" + low + "\t" + high);
        }
        if (debug) {
          if (i % 100 == 0)
            System.out.println();
          
        }
      }
      
      System.out.println();
    }
  }
}

//class RandomSquared extends Random {
//  
//  /**
//   * 
//   */
//  private static final long serialVersionUID = 1L;
//
//  public RandomSquared(long seed) {
//    super(seed);
//  }
//
//  @Override
//  public double nextDouble() {
//    this.setSeed(super.nextLong());
//    return super.nextDouble();
//  }
//}
