package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Also called Binomial sampling. For each sample, roll the dice and pass/fail.
 * Known as not very stable, and does not give a fixed number of samples.
 * There are tricks to avoid doing random each time, but this is not really important.
 */
public class BernoulliSampler<T> extends Sampler<T> {
  List<T> samples = new ArrayList<T>();
  final double percent;
  final Random rnd;
  
  public BernoulliSampler(double percent) {
    this.percent = percent;
    rnd = RandomUtils.getRandom();
  }
  
  public BernoulliSampler(double percent, Random rnd) {
    this.percent = percent;
    this.rnd = rnd;
  }
  
  @Override
  public void addSample(T sample) {
    if (null == samples)
      throw new NullPointerException();
    double r = rnd.nextDouble();
    if (r <= percent) {
      samples.add(sample);
    }
  }
  
  @Override
  public T getSample() {
    if (null == samples)
      return null;
    if (samples.size() > 0) {
      T sample = samples.get(0);
      samples.remove(0);
      return sample;
    } else {
      return null;
    }
  }
  
  @Override
  public Iterator<T> getSamples(boolean flush) {
    if (null == samples)
      return (Iterator<T>) Collections.EMPTY_LIST.iterator();
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
  
//  @Override
//  public boolean hasSamples() {
//    return samples.size() > 0;
//  }
  
}
