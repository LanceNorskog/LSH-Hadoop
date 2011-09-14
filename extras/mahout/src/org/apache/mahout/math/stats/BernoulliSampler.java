package org.apache.mahout.math.stats;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Also called Binomial sampling. For each sample, roll the dice and pass/fail.
 * Known as not very stable, and does not give a fixed number of samples.
 * There are tricks to avoid doing random each time, but this is not really important.
 */
public class BernoulliSampler extends Sampler {
  final double percent;
  final Random rnd;
  private Double nextRnd = null;
  
  public BernoulliSampler(double percent) {
    this.percent = percent / 100;
    rnd = RandomUtils.getRandom();
  }
  
  public BernoulliSampler(double percent, Random rnd) {
    this.percent = percent;
    this.rnd = rnd;
    stage();
  }
  
  @Override
  protected boolean sample()  {
    boolean val = check();
    stage();
    return val;
  }
  
  private boolean check() {
    return nextRnd < percent;
  }
  
  private void stage() {
    nextRnd = rnd.nextDouble();
  }
}
