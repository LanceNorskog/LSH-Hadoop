package org.apache.mahout.math.stats.sampler;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Also called Binomial sampling. For each sample, roll the dice and pass/fail.
 * Known as not very stable, and does not give a fixed number of samples.
 * There are tricks to avoid doing random each time, but this is not really important.
 */
public class BernoulliSampler {
  final double percent;
  final Random rnd;
  private double nextRnd;
  private boolean lastSample = false;
  int current = 0;
  
  
  public BernoulliSampler(double percent) {
    this.percent = percent;
    rnd = RandomUtils.getRandom();
  }
  
  public BernoulliSampler(double percent, Random rnd) {
    this.percent = percent;
    this.rnd = rnd;
    stage();
    pushback(check());
  }
  
  /*
   * Fetch next sample. Sampling state always ticks over. 
   */
  //  protected boolean sample()  {
  //    boolean val = check();
  //    stage();
  //    return val;
  //  }
  
  private boolean check() {
    return nextRnd < percent;
  }
  
  private void stage() {
    nextRnd = rnd.nextDouble();
  }
  
  /* Is sampled? */
  public final boolean isSampled() {
    boolean tmp = lastSample;
    stage();
    lastSample = check();
    return tmp;
  }
  
  // allow checking without ticking over
  public final void pushback(boolean sample) {
    lastSample = sample;
  }
  
  /*
   * Return next without ticking over.
   */
  public final boolean peek() {
    return lastSample;
  }
  
}
