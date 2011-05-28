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
    // horizontal bar: under is within slice
    double slice = mapper.apply(nextX());
    while(true) {
      double x = nextX();
      // establish initial window
      double left = x - width/2;
      double right = x + width/2;
      // clamp to prescribed range
      if (left < low || right > high) {
        continue;
      }
      // expand until outside the slice
      while (mapper.apply(left) < slice) {
        left -= width;
      }
      while (mapper.apply(right) < slice) {
        right += width;
      }
      if (x < left || x > right)
        continue;
      double fx = mapper.apply(x);
      if (fx < slice)
        break;
      x = nextX();
    }
    
    return x;
  }
  
  private double nextX() {
    return low + (rnd.nextDouble() * (high - low));
  }
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
  }
  
  
  
}
