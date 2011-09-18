package org.apache.mahout.math.stats.sampler;

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

public class NthSampler {
  final int n;
  boolean last = false;
  boolean lastSample = false;
  int current = 0;
  boolean bernoulli = true;
  
  /*
   * Systematic sampling: take every Nth sample
   */
  public NthSampler(int n, int start) {
    this.n = n;
    current = start;
    stage();
    lastSample = check();
  }
  
  private boolean check() {
    return current == 0;
  }
  
  private void stage() {
    current = (current + 1) % n;
  }
  
  /* Is sampled? */
  public final boolean isSampled() {
    boolean tmp = lastSample;
    stage();
    lastSample = check();
    return tmp;
  }
  
   /*
   * Return next without ticking over.
   */
  public final boolean peek() {
    return lastSample;
  }
  
  public final void pushback(boolean sample) {
    lastSample = sample;
    current--;
    if (current == -1)
      current = n - 1;
  }
  
}
