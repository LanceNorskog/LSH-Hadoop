package org.apache.mahout.math.stats;

import java.util.Random;

public class RandomSquared extends Random {
  
  public RandomSquared(long seed) {
    super(seed);
  }

  @Override
  public double nextDouble() {
    this.setSeed(super.nextLong());
    return super.nextDouble();
  }
}
