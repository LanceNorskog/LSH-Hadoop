package org.apache.mahout.math.stats;

/*
 * Decide whether the given sample, in the current state, would be accepted.
 * 
 * Default implementation: Systematic Sampling.
 *  Assuming a random input stream, select every Nth sample.
 * Various subclasses:
 *  Bernoulli Sampling: select the sample when n == random() % n
 *  Slice Sampling: select random value X when sample matches a random function 
 *  
 *  Use peek() and pushback() to avoid changes to the internal state.
 */

public class Sampler {
  boolean last = false;
  boolean lastSample = false;
  final int n;
  int current = 0;
  
  public Sampler() {
    n = 0;
  }
  
  public Sampler(int n) {
    this.n = n;
  }
  
  /* Is sampled? */
  public final boolean isSampled() {
    if (last) {
      last = false;
      return lastSample;
    }
    return true;
  }
  
  // allow checking without ticking over
  public final void pushback(boolean sample) {
    last = true;
    lastSample = sample;
  }
  
  /*
   * Return next without ticking over.
   */
  public final boolean peek() {
    last = true;
    lastSample = sample();
    return lastSample;
  }
  
  /*
   * Fetch next sample. Sampling state always ticks over. 
   */
  protected boolean sample() {
    current++;
    if (current == n) {
      current = 0;
      return true;
    }
    return false;
  }
  
}
