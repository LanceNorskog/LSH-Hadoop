package org.apache.mahout.math.stats;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.math.function.DoubleFunction;

/*
 * Simple "Slice Sampler". One-dimensional variant.
 * Given a function on a type T, return a distribution the shape of the function. 
 * http://en.wikipedia.org/wiki/Slice_sampling
 */

public class SliceSampler1D<T> extends Sampler<T> {
  final DoubleFunction mapper;
  final Random rnd;
  final double high;
  final double low;
  // constant for all samples
  final double width;   // starting width
  double slice = 0;   // output of func(nextX)
  double left = 0;
  double right = 0;
  boolean reset = true;
  
  /*
   * mapper: function on a type
   * rnd: random generator
   * width: initial width of slice
   * low, high: range of output function expected
   */
  public SliceSampler1D(DoubleFunction mapper, Random rnd, double width, double low, double high) {
    this.mapper = mapper;
    this.rnd = rnd;
    this.width = width;
    this.low = low;
    this.high = high;
    double r = nextX();
    resetWindow();
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
   * 
   * Do one pass of "pull a sample within a sliding window"
   */
  @Override
  public boolean isDropped(T sample) {
    // expand left and/or rightwards until both outside the slice
    double leftY = mapper.apply(left);
    double rightY = mapper.apply(right);
    if (leftY < slice || rightY < slice) {
      while(mapper.apply(left) < slice) {
        left -= width;
      }
      if (left < low)
        left = low;
      while(mapper.apply(right) < slice) {
        right += width;
      }
      if (right > high)
        right = high;
    }
    // do one check, return valid
    double x = nextX(left, right);
    double test = mapper.apply(x);
    if (test > slice)
      return false;
    resetWindow();
    return true;
  }
  
  private void resetWindow() {
    // set initial window within full range, of given width
    // horizontal bar: under is within slice
    slice = mapper.apply(nextX());
    double starter = nextX();
    double test = mapper.apply(starter);
    while(test > slice) {
      starter = nextX();
      test = mapper.apply(starter);
    }
    // establish initial window
    left = starter - width/2;
    right = starter + width/2;
    // clamp to prescribed range
    if (left < low) {
      right += low - left;
      left = low;
    } else if (right > high) {
      left -= right - high;
      right = high;
    }
    
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
    return nextX(low, high);
  }
  
  private double nextX(double lower, double upper) {
    return lower + (rnd.nextDouble() * (upper - lower));
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