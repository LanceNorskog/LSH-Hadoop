package org.apache.mahout.math.quantize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.Vector;


/*
 * Quantize a Vector against a fixed set of vectors.
 * Use cases: 
 *    assign a vector to the centroid of the nearest cluster.
 * 
 * Slow N-ary implementation. 
 * 
 *  Would be useful for finding outliers.
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
    
  @Override
  public Iterator<Vector> getNearest(Vector value) {
    return new NaborIterator(value);
  }
}

class NaborIterator implements Iterator<Vector> {
  final Vector v;
  int dim = 0;
  boolean sign = false;
  
  NaborIterator(Vector v) {
    this.v = v;
  }

  @Override
  public boolean hasNext() {
    return dim < v.size();
  }

  @Override
  public Vector next() {
    Vector nabe = v.like();
    
    
    return nabe;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub
    
  }
  
}