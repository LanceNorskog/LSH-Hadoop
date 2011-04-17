package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;

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
    double r = rnd.nextDouble();
    if (r <= percent) {
      samples.add(sample);
    }
  }
  
  @Override
  public T getSample() {
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
    Iterator<T> iter = samples.iterator();
    if (flush) {
      samples = new ArrayList<T>();
    }
    return iter;
  }
  
}
