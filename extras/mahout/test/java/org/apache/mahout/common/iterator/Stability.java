package org.apache.mahout.common.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.Sampler;

import org.apache.commons.collections.iterators.ArrayIterator;

/*
 * Measure the stability of <s>the author</s> various Sampling algorithms and tools.
 */
public class Stability {
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    int total = 100000;
    int samples = 1000;
    double percent = ((double) samples) / total;
    FastByIDMap<Integer> idmap = new FastByIDMap<Integer>(total);
    int[] scrambled = new int[total];
    Random rnd = getRnd();
    
    Arrays.fill(scrambled, -1);
    // dups are ok
    for(int i = 0; i < total; i++) {
      scrambled[i] = rnd.nextInt(total);
    }
    RunningAverageAndStdDev tracker = new FullRunningAverageAndStdDev();
    for(int i = 0; i < 20; i++) {
      Sampler<Integer> sampler = new BernoulliSampler<Integer>(0.1, rnd);
      stability(scrambled, sampler, total, samples, tracker);
    }
    System.out.println("Bernoulli: " + tracker.toString());
  }
  
  private static void stability(int[] scrambled, Sampler<Integer> sampler,
      int total, int samples, RunningAverageAndStdDev tracker) {
    int i = 0;
    while(i < total) {
      for(int x = 0; x < 100; x++) {
        int r = scrambled[i + x];
        sampler.addSample(r);
        if (i == total)
          break;
      }
      Iterator<Integer> it = sampler.getSamples(true);
      while(it.hasNext()) {
        Integer r = it.next();
        tracker.addDatum(r);
      }
      i += 100;
    }
  }
  
  private static Random getRnd() {
    RandomUtils.useTestSeed();
    return RandomUtils.getRandom();
  }
  
}
