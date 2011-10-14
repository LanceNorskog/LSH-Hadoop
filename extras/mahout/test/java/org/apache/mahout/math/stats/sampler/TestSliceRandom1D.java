package org.apache.mahout.math.stats.sampler;

import java.util.Random;

import org.apache.mahout.math.MahoutTestCase;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.SquareRootFunction;
import org.junit.Test;


public class TestSliceRandom1D extends MahoutTestCase {
  boolean debug = true;
  
  @Test
  public void testDist() {
    double BIG = 10.0;
    int N = 800;
    DoubleFunction dist = new SeesawFunction(N); // new SquareRootFunction(); // 
    if (debug) {
      System.out.print("Distribution");
      for(int i = 0; i < N - "Distribution".length(); i++) {
        System.out.print(' ');
      }
      System.out.println("\tDensity");
    }
    for(int seed = 0; seed < 50; seed++) {
      double low = 1000000;
      double high = 0;
      Random rnd = new Random(seed);
      SliceRandom slicer = new SliceRandom(dist, rnd, 0.01d, 0d, BIG);
      int[] buckets = new int[100];
      for(int i = 0; i < N; i++) {
        double sample = slicer.nextDouble();
        high = Math.max(high, sample);
        low = Math.min(low, sample);
        buckets[(int) (sample * 99)]++;
       }
      for(int i = 0; i < 100; i++) {
        System.out.print(buckets[i] > N/100 ? "+" : ".");
      }
      System.out.println("\n\t" + low + "\t" + high + "\t count: " + ((double)slicer.repeats)/N);
    }
  }
}

class RandomSqrt extends Random {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public RandomSqrt(long seed) {
    super(seed);
  }

  @Override
  public double nextDouble() {
    this.setSeed(super.nextLong());
    return Math.sqrt(super.nextDouble());
  }
}

 final class BigFunction implements DoubleFunction {
  
  double top;
  
  public BigFunction(double top) {
    this.top = top;
  }

  @Override
  public double apply(double arg1) {
    return arg1 > top*0.9 ? top : 0;
  }
}
