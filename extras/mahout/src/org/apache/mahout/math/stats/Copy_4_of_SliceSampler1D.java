package org.apache.mahout.math.stats;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.math.function.DoubleFunction;

/*
 * Simple "Slice Sampler". One-dimensional variant.
 * Given a function on a type T, return a distribution the shape of the function. 
 * http://en.wikipedia.org/wiki/Slice_sampling
 */

public class Copy_4_of_SliceSampler1D<T> extends Sampler<T> {
  final DoubleFunction mapper;
  final Random rnd;
  final double high;
  final double low;
  // constant for all samples
  final double width;   // starting width
  double slice = 0;   // output of func(nextX)
  double left = 0;
  double right = 0;
  private boolean debug = false;
  
  /*
   * mapper: function on a type
   * rnd: random generator
   * width: initial width of slice
   * low, high: range of output function expected
   */
  public Copy_4_of_SliceSampler1D(DoubleFunction mapper, Random rnd, double width, double low, double high) {
    this.mapper = mapper;
    this.rnd = rnd;
    this.width = width;
    this.low = low;
    this.high = high;
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
    double x = nextX(left, right);
    // do one check, return valid
    double test = mapper.apply(x);
    if (debug )
      System.out.println("left/right/x/test/slice: " + left + " right: " + right + "x: " + x + " test: " + test + " slice: " + slice);
    
    if (test > slice) {
      resetWindow();
      return false;
    }
    if (x - left < right - x) {
      left = x;
    } else {
      right = x;
    }
    // if we collapsed completely, just start over
    if (left + 0.000001 > right) {
      resetWindow();
      System.out.print("!");
    }
    return true;
  }
  
  private void resetWindow() {
    // set initial window within full range, of given width
    // Now must set the boundary around a sample inside
    double sliceX = nextX();
    slice = mapper.apply(sliceX);
    double center = nextX();
    left = center - width/2;
    right = center + width/2;
    
    while(mapper.apply(left) > slice && left > low) {
      left -= width/2;
    }
    while(mapper.apply(right) > slice && right < high) {
      right += width/2;
    }
    
    // clamp to prescribed range
    if (left < low) {
      left = low;
      System.out.print("<");
    } else if (right > high) {
      right = high;
      System.out.print(">");
    }
    if (debug)
      System.out.println("Slice: " + slice + ",");
    if (debug)
      System.out.println("Reset: " + left + "," + right);
  }
  
  private double nextX() {
    return nextX(low, high);
  }
  
  private double nextX(double lower, double upper) {
    //    long seed = rnd.nextLong();
    //    rnd.setSeed(seed ^ 010101010101010101010L);
    double range = upper - lower;
    double r = rnd.nextDouble() * range;
    return lower + r;
  }
  
  @Override
  public void stop() {
  }
  
  //  /**
  //   * @param args
  //   */
  //  public static void main(String[] args) {
  //    // TODO Auto-generated method stub
  //    
  //  }
  //  
  
}
