package org.apache.mahout.math.stats;

import java.util.Random;

import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.mahout.math.MahoutTestCase;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.SquareRootFunction;
import org.junit.Test;


public class TestSliceSampler1D extends MahoutTestCase {

  @Test
  public void testA() {
    DoubleFunction sqrt = new SquareRootFunction();
    Random rnd = new Random(0);
    SliceSampler1D<String> sampler = new SliceSampler1D<String>(sqrt, rnd, 0.01, 0.5, 100);
    System.out.println("Start test");
    for(int i = 0; i < 100; i++) {
      if (sampler.isDropped(""))
        System.out.print(".");
      else 
        System.out.print(";");
    }
    System.out.println();
    System.out.println("End test");
  }
  
}
