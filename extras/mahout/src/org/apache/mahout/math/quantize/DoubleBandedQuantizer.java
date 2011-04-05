package org.apache.mahout.math.quantize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * Quantize floating-point numbers to a given band
 * Also to float range.
 */

public class DoubleBandedQuantizer extends Quantizer<Double> {
  final double cut;
  
  public DoubleBandedQuantizer(double cut) {
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
  
  // return bracketing values of quantized value
  
  @Override
  public Iterator<Double> getNearest(Double value) {
    Double q = quantize(value);
    List<Double> nabes = new ArrayList<Double>(2);
    nabes.add(q - cut);
    nabes.add(q + cut);
    return nabes.iterator();
  }

}
