package org.apache.mahout.math.quantize;

/*
 * Quantize floating-point numbers to a 
 * Also to float range.
 * 
 * Do not implement iterators
 */

public class FloatingDiscreteQuantizer extends Quantizer<Double> {
  final double cut;
  
  public FloatingDiscreteQuantizer(double cut) {
    this.cut = cut; 
  }
  
  @Override
  public Double quantize(Double value) {
    if (Double.isNaN(value) || Double.isInfinite(value))
      return value;
    Double d;
    d = Math.min(value, Float.MAX_VALUE);
    d = Math.max(value, -Float.MAX_VALUE);
    d = d * cut;
    d = Math.floor(d);
    return d/cut;
  }
  
}
