package org.apache.mahout.math.stats;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.math.function.DoubleFunction;

/*
 * Simple "Slice Sampler". One-dimensional variant.
 * Given a function on a type T, return a distribution the shape of the function. 
 * http://en.wikipedia.org/wiki/Slice_sampling
 */

public class Copy_2_of_SliceSampler1D<T> extends Sampler<T> {
  final DoubleFunction mapper;
  final Random rnd;
  final double high;
  final double low;
  // constant for all samples
  final double width;   // starting width
//  final double slice;   // output of func(nextX)
  
  /*
   * mapper: function on a type
   * rnd: random generator
   * width: initial width of slice
   * low, high: range of output function expected
   */
  public Copy_2_of_SliceSampler1D(DoubleFunction mapper, Random rnd, double width, double low, double high) {
    this.mapper = mapper;
    this.rnd = rnd;
    this.width = width;
    this.low = low;
    this.high = high;
    double r = nextX();
//    slice = mapper.apply(r);
  }
  
  @Override
  public void addSample(T sample) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public T getSample() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Iterator<T> getSamples(boolean flush) {
    throw new UnsupportedOperationException();
  }
  
  /*
   * Implement expanding/contracting window algorithm:
   * @see org.apache.mahout.math.stats.Sampler#isDropped(java.lang.Object)
   */
  @Override
  public boolean isDropped(T sample) {
    double slice = mapper.getSample(input)
    double x = mapper.getSample(sample);
    double left = x - width/2;
    double right = x + width/2;
    if ()
    
    return true;
  }
  
//  private void setCenter() {
//    while(true) {
//      double r = nextX();
//      center = r;
//      if (center - width/2 < low) {
//        continue;
//      }
//      if (center + width/2 > high) {
//        continue;
//      }
//    }
//  }
  
  private double nextX() {
    return low + (rnd.nextDouble() * (high - low));
  }
  
  @Override
  public void stop() {
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
  }
  
  
}

abstract class GenericFunc<T> {
  abstract double getSample(T input);
}