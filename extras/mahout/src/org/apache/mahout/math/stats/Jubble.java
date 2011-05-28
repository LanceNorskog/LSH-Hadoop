package org.apache.mahout.math.stats;

import org.apache.mahout.math.function.DoubleFunction;

public class Jubble implements DoubleFunction {
  
  @Override
  public double apply(double arg1) {
    return Math.sqrt(arg1);
  }
  
}
