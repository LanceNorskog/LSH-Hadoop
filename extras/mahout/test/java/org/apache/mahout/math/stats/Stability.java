package org.apache.mahout.math.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.OnlineSummarizer;
import org.apache.mahout.math.stats.ReservoirSampler;
import org.apache.mahout.math.stats.Sampler;

/*
 * Measure the stability of <s>the author</s> various sampling algorithms.
 */
public class Stability {
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    int total = 100000;
    int samples = 500;
    int[] scrambled = new int[total];
    Random rnd = getRnd();
    
    Arrays.fill(scrambled, -1);
    // dups are ok
    for(int i = 0; i < total; i++) {
      scrambled[i] = rnd.nextInt(total);
    }
    bernoulli(total, samples, scrambled, rnd);
    reservoir(total, samples, scrambled, rnd);
  }
  
  private static void reservoir(int total, int samples, int[] scrambled,
      Random rnd) {
    RunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    for(int i = 0; i < 50; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer(); //(0.45, 0.45);
      Sampler<Integer> sampler = new ReservoirSampler<Integer>(samples, rnd);
      stability(scrambled, sampler, total, samples, tracker);
//      System.out.println(i + "," + tracker.toString());
      // subtract what should be the mean instead of the actual mean
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
    }
    System.out.println("Reservoir stability: (mean,median,25,75) " + mean.getStandardDeviation() + ", " +
        median.getStandardDeviation() + ", " + q1.getStandardDeviation() + ", " + q3.getStandardDeviation());
  }
  
  private static void bernoulli(int total, int samples, int[] scrambled, Random rnd) {
    RunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    double percent = ((double) samples) / total;
    for(int i = 0; i < 50; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer();
      Sampler<Integer> sampler = new BernoulliSampler<Integer>(percent, rnd);
      stability(scrambled, sampler, total, samples, tracker);
//      System.out.println(i + "," + tracker.toString());
      // subtract what should be the mean instead of the actual mean
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
    }
    System.out.println("Bernoulli stability: (mean,median,25,75) " + mean.getStandardDeviation() + ", " +
        median.getStandardDeviation() + ", " + q1.getStandardDeviation() + ", " + q3.getStandardDeviation());
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
