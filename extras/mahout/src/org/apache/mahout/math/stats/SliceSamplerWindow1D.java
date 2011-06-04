package org.apache.mahout.math.stats;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.math.function.DoubleFunction;

/*
 * Simple "Slice Sampler". One-dimensional variant.
 * Given a function on a type T, return a distribution the shape of the function. 
 * http://en.wikipedia.org/wiki/Slice_sampling
 * 
 * Subsample a sample<T> set against the given DoubleFunction.
 * If the sample<T> distribution is linear, the output distribution 
 * will be the shape of DoubleFunction.
 * 
 * This just selects a slice and tests the sample against it.
 * SampleFunction<T> returns an X value based on the sample contents.
 * DoubleFunction returns a Y value based on an X value.
 * The algorithm:
 *    Generate a random X
 *    Apply DoubleFunction on X to get "Slice" Y:
 *      DoubleFunction.apply(random X) gives the lower bound of an area 
 *      underneath DoubleFunction. The "slice" is the area under the curve and above Y.
 *    Generate an X based on the given sample<T> via SampleFunction<T>.
 *    Test DoubleFunction against sample's X: Y must be above slice boundary Y.
 */

public class SliceSamplerWindow1D<T> extends Sampler<T> {
  final SampleFunction<T> mapSample;
  final DoubleFunction mapX;
  final Random rnd;
  final double lowX;
  final double highX;
  final double width;   // starting width
  private boolean debug = false;
  
  double slice = 0;   // current lower Y value until a correct sample
  double windowL = 0;
  double windowH = 0;
  boolean needSlice = true;
  
  /*
   * mapSample: function on type, generates range X
   * mapX: function on a double, generates slice and test Y
   * rnd: random generator
   * low, high: range of mapSample
   */
  public SliceSamplerWindow1D(SampleFunction<T> mapSample, DoubleFunction mapX, Random rnd, double lowX, double highX, double width) {
    this.mapSample = mapSample;
    this.mapX = mapX;
    this.rnd = rnd;
    this.lowX = lowX;
    this.highX = highX;
    this.width = width;
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
   * If input sample within window and above current slice, 
   *  reset window and slice, return true
   *  else return false. 
   *  This way, we only pick a slice for every successful sample.
   */
  @Override
  public boolean isSampled(T sample) {
    if (needSlice) {
      // place new random X and matching slice value
      double randomX = nextX();
      windowL = Math.max(randomX - width/2, lowX);
      windowH = Math.min(randomX + width/2, highX);
      double slice = mapX.apply(randomX);  
      // set windows so that they are outside the slice
      while(windowL > lowX) {
        double test = mapX.apply(windowL);
        if (test < slice)
          break;
        windowL = Math.max(windowL - width, lowX);
      }
      while(windowH < highX) {
        double test = mapX.apply(windowH);
        if (test < slice)
          break;
        windowH = Math.min(windowH + width, lowX);
      }
      needSlice = false;
    } else {
      this.hashCode();
    }
    double sampleX = mapSample.apply(sample);
    double test = mapX.apply(sampleX);
    if (debug )
      System.out.println("low/high/slice/sample/test: " + windowL + ",\t" + windowH + ",\t" + slice + ",\t" + sampleX + ",\t" + test);
    if (test > slice) {
      needSlice = true;
      return true;
    } else {
      // narrow the windows
      if (sampleX - windowL > windowH - sampleX) {
        windowH = sampleX;
      } else {
        windowL = sampleX;
      }
      if (windowL >= windowH) {
        needSlice = true;
      }
      return false;
    }
  }
  
  private double nextX() {
    double range = highX - lowX;
    double r = rnd.nextDouble() * range;
    return lowX + r;
  }
  
  @Override
  public void stop() {
  }
  
}
