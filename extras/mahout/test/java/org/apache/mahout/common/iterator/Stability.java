package org.apache.mahout.common.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.OnlineSummarizer;
import org.apache.mahout.math.stats.ReservoirSampler;
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
    int samples = 500;
    double percent = ((double) samples) / total;
    int[] scrambled = new int[total];
    Random rnd = getRnd();
    
    Arrays.fill(scrambled, -1);
    // dups are ok
    for(int i = 0; i < total; i++) {
      scrambled[i] = rnd.nextInt(total);
    }
    RunningAverage avg;
    RunningAverage stdev;
    bernoulli(total, samples, scrambled, rnd);
    reservoir(total, samples, scrambled, rnd);
  }
  
  private static void reservoir(int total, int samples, int[] scrambled,
      Random rnd) {
    OnlineSummarizer os = new OnlineSummarizer();
    for(int i = 0; i < 5; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer(); //(0.45, 0.45);
      Sampler<Integer> sampler = new ReservoirSampler<Integer>(samples, rnd);
      stability(scrambled, sampler, total, samples, tracker);
      System.out.println(i + "," + tracker.toString());
//      os.add(tracker.;
    }
    System.out.println("Reservoir: " );
  }
  
  private static void bernoulli(int total, int samples, int[] scrambled, Random rnd) {
    RunningAverage avg = new FullRunningAverage();
    RunningAverage stdev = new FullRunningAverage();
    double percent = ((double) samples) / total;
    for(int i = 0; i < 5; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer();
      Sampler<Integer> sampler = new BernoulliSampler<Integer>(percent, rnd);
      stability(scrambled, sampler, total, samples, tracker);
      System.out.println(i + "," + tracker.toString());
    }
    System.out.println("Bernoulli: " + avg.getAverage() + "," + stdev.getAverage());
  }
  
  // run scrambled integer stream through sampler
  // pull output stream and get average and standard deviation
  private static void stability(int[] scrambled, Sampler<Integer> sampler,
      int total, int samples, OnlineSummarizer tracker) {
    for(int sample = 0; sample < total; sample++) {
      int r = scrambled[sample];
      sampler.addSample(r);
    }
    Iterator<Integer> it = sampler.getSamples(true);
    while(it.hasNext()) {
      Integer r = it.next();
      tracker.add(r);
    }
  }
  
  private static Random getRnd() {
    RandomUtils.useTestSeed();
    return RandomUtils.getRandom();
  }
  
}
