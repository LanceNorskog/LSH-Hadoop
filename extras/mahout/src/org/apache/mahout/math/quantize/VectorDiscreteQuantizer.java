package org.apache.mahout.math.quantize;

import java.util.Collection;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.Vector;


/*
 * Quantize a Vector against a fixed set of vectors.
 * Use cases: 
 *    assign a vector to the centroid of the nearest cluster.
 * 
 * Slow N-ary implementation.  
 */

public class VectorDiscreteQuantizer extends Quantizer<Vector> {
  final private Collection<Vector> matches;
  final private DistanceMeasure measure;
  
  public VectorDiscreteQuantizer(Collection<Vector> matches, DistanceMeasure measure) {
    this.matches = matches;
    this.measure = measure;
  }
  
  @Override
  public Vector quantize(Vector value) {
    Vector closest = null;
    double min = Double.MAX_VALUE;
    for(Vector match: matches) {
      double distance = measure.distance(value, match);
      if (min > distance) {
        min = distance;
        closest = match;
        ;
      }
    }
    return closest;
  }

  @Override
  public void quantize(Vector value, Vector target) {
    Vector closest = quantize(value);
    target.assign(closest);
  }   
    
}