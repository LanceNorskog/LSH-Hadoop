package org.apache.mahout.math.stats;

import java.util.Random;

import org.apache.mahout.math.function.DoubleFunction;

/*
 * Simple "Slice Sampler". One-dimensional variant.
 * Given a function on a type T, return a distribution the shape of the function. 
 * http://en.wikipedia.org/wiki/Slice_sampling
 */

public class SliceRandom extends Random {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  final DoubleFunction mapper;
  final Random rnd;
  final double high;
  final double low;
  // constant for all samples
  final double width;   // starting width
  //  final double slice;   // output of func(nextX)
  int repeats = 0;  // total 2nd through N through the loop
  int skip = 0;     // number of discontinuities
  
  /*
   * mapper: function on a type
   * rnd: random generator
   * width: initial width of slice
   * low, high: range of output function expected
   */
  public SliceRandom(DoubleFunction mapper, Random rnd, double width, double low, double high) {
    this.mapper = mapper;
    this.rnd = rnd;
    this.width = width;
    this.low = low;
    this.high = high;
  }
  
  /*
   * Implement expanding/contracting window algorithm:
   * @see org.apache.mahout.math.stats.Sampler#isDropped(java.lang.Object)
   */
  @Override
  public double nextDouble() {
    double x;
    repeats = 0;
    while(true) {
      // horizontal bar: under is within slice
      double slice = mapper.apply(nextX());
      // establish initial window
      x = nextX();
      double left = Math.max(x - width, low);
      double right = Math.min(x + width, high);
      // expand until both sides are outside of the slice
      double y = 0;
      while ((y = mapper.apply(left)) < slice && left > low) {
        left = Math.max(left - width, low);
      }
      while ((y = mapper.apply(right)) < slice && right < high) {
        right = Math.min(right + width, high);
      }
      if (left == low && right == high) {
        // the window is wider than a local peak of the function
        // should make window half as big
        repeats++;
        skip++;
        continue;
      }
      double fx = mapper.apply(x);
      if (fx < slice)
        break;
      if (high - x > x - low)
        left = x;
      else
        right = x;
      if (left >= right)
      repeats++;
    }
    
    double delta = high - low;
    return (x - low)/delta;
  }
  
  private double nextX() {
    return low + (rnd.nextDouble() * (high - low));
  }
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
  }
  
  
  
}
