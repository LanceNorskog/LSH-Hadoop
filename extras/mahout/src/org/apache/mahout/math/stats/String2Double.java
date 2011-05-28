package org.apache.mahout.math.stats;

public class String2Double extends SampleFunction<String> {
  
  @Override
  public double apply(String arg1) {
    return Double.valueOf(arg1);
  }
  
}
