package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Also called Binomial sampling. For each sample, roll the dice and pass/fail.
 * Known as not very stable, and does not give a fixed number of samples.
 * There are tricks to avoid doing random each time, but this is not really important.
 */
public class BernoulliSampler<T> extends Sampler<T> {
  List<T> samples = new LinkedList<T>();
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
  public void addSample(T sample) {
    if (null == samples)
      throw new NullPointerException();
    if (check()) {
      samples.add(sample);
    }
    stage();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Iterator<T> getSamples(boolean flush) {
    if (null == samples)
      return (Iterator<T>) Collections.emptyList().iterator();
    Iterator<T> iter = samples.iterator();
    if (flush) {
      samples = new ArrayList<T>();
    }
    return iter;
  }
  
  @Override
  public void stop() {
    samples = null;
  }
  
  @Override
  public boolean isSampled(T sample) {
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
