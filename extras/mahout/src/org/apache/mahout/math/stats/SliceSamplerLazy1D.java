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

public class SliceSamplerLazy1D<T> {
  final SampleFunction<T> mapSample;
  final DoubleFunction mapX;
  final Random rnd;
  final double lowX;
  final double highX;
  private boolean debug = false;
  
  double slice = 0;   // current lower Y value until a correct sample
  boolean needSlice = true;
  
  /*
   * mapSample: function on type, generates range X
   * mapX: function on a double, generates slice and test Y
   * rnd: random generator
   * low, high: range of mapSample
   */
  public SliceSamplerLazy1D(SampleFunction<T> mapSample, DoubleFunction mapX, Random rnd, double lowX, double highX, double width) {
    this.mapSample = mapSample;
    this.mapX = mapX;
    this.rnd = rnd;
    this.lowX = lowX;
    this.highX = highX;
  }
   
  /*
   * Do one pass of "pull a sample within a sliding window"
   * If input sample within window and above current slice, 
   *  reset window and slice, return true
   *  else return false. 
   *  This way, we only pick a slice for every successful sample.
   */
  public boolean sampled(T sample) {
    if (needSlice) {
      // place new random X and matching slice value
      double randomX = nextX();
      slice = mapX.apply(randomX);  
      needSlice = false;
    } else {
      this.hashCode();
    }
    double sampleX = mapSample.apply(sample);
    double test = mapX.apply(sampleX);
    if (debug )
      System.out.println("slice/sample/test: " + slice + ",\t" + sampleX + ",\t" + test);
    if (test > slice) {
      needSlice = true;
      return true;
    } else {
      return false;
    }
  }
  
  private double nextX() {
    double range = highX - lowX;
    double r = rnd.nextDouble() * range;
    return lowX + r;
  }
  
}
