package org.apache.mahout.math.stats;

import java.util.Random;

import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.MahoutTestCase;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Mult;
import org.apache.mahout.math.function.SquareRootFunction;
import org.junit.Test;


public class TestSliceSampler1D extends MahoutTestCase {
  
  @Test
  public void testA() {
    SampleFunction<String> toX = new String2Double();
    DoubleFunction dist = new Jubble();
    System.out.println("Start test");
    for(int seed = 0; seed < 10; seed++) {
      Random rnd = new RandomSquared(seed);
      int N = 40;
      SliceSampler1D<String> sampler = new SliceSampler1D<String>(toX, dist, rnd, 0, N);
      for(int i = 0; i < N; i++) {
        if (sampler.isDropped("" + i))
          System.out.print(".");
        else 
          System.out.print("+");
      }
      System.out.println();
    }
    System.out.println("End test");
  }
  
}
