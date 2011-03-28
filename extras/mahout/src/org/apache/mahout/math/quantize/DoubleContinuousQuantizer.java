package org.apache.mahout.math.quantize;

/*
 * Quantize floating-point numbers to a given band
 * Also to float range.
 */

public class DoubleContinuousQuantizer extends Quantizer<Double> {
  final double cut;
  
  public DoubleContinuousQuantizer(double cut) {
    this.cut = cut; 
  }
  
  @Override
  public Double quantize(Double value) {
    if (Double.isNaN(value) || Double.isInfinite(value))
      return value;
    Double d;
    d = Math.min(value, Float.MAX_VALUE);
    d = Math.max(value, -Float.MAX_VALUE);
    if (cut == 1.0) {
      return d;
    } else if (cut < 1.0) {
      d = d * 1/cut;
      d = Math.floor(d);
      return Math.floor(d*cut);

    } else {
    d = d * cut;
    d = Math.floor(d);
    return Math.floor(d/cut);
    }
  }

}
