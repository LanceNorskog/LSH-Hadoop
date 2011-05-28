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
  final SampleFunction<T> mapSample;
  final DoubleFunction mapX;
  final Random rnd;
  final double high;
  final double low;
  private boolean debug = false;
  
  /*
   * mapper: function on a type
   * rnd: random generator
   * width: initial width of slice
   * low, high: range of output function expected
   */
  public SliceSampler1D(SampleFunction<T> mapSample, DoubleFunction mapX, Random rnd, double low, double high) {
    this.mapSample = mapSample;
    this.mapX = mapX;
    this.rnd = rnd;
    this.low = low;
    this.high = high;
  }
  
  @Override
  public void addSample(T sample) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Iterator<T> getSamples(boolean flush) {
    throw new UnsupportedOperationException();
  }
  
  /*
   * Do one pass of "pull a sample within a sliding window"
   */
  @Override
  public boolean isDropped(T sample) {
    double x = nextX();
    double slice = mapX.apply(x);
    double sampleX = mapSample.apply(sample);
    double test = mapX.apply(sampleX);
    if (debug )
      System.out.println("low/high/x/slice/sample/test: " + low + ",\t" + high + ",\t" + x + ",\t" + slice + ",\t" + sampleX + ",\t" + test);
    
    return test < slice;
  }
  
  private double nextX() {
    double range = high - low;
    double r = rnd.nextDouble() * range;
    return low + r;
  }
  
  @Override
  public void stop() {
  }
  
}
