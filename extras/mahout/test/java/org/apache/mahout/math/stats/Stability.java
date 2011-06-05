package org.apache.mahout.math.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.ReservoirSampler;
import org.apache.mahout.math.stats.Sampler;

/*
 * Measure the stability of <s>the author</s> various sampling algorithms.
 */
public class Stability {
  static int TOTAL = 5000;        // total number of samples
  static int ITERATIONS = 20;     // number of iterations to build standard deviation
  static int RANGE = 50;         // numerical samples are 0-RANGE
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    for(int samples = 101; samples < 4000; samples += 300) {
      System.out.println("Full pass with total=" + TOTAL + ", subset=" + samples);
      full(TOTAL, samples, 500);
      full(TOTAL, samples, 600);
    }
  }
  
  private static void full(int total, int samples, long seed) {
    RunningAverage mean_r = new FullRunningAverage();
    RunningAverage median_r = new FullRunningAverage();
    RunningAverage q1_r = new FullRunningAverage();
    RunningAverage q3_r = new FullRunningAverage();
    RunningAverage mean_b = new FullRunningAverage();
    RunningAverage median_b = new FullRunningAverage();
    RunningAverage q1_b = new FullRunningAverage();
    RunningAverage q3_b = new FullRunningAverage();
    double percent = ((double) samples) / total;
    bernoulli(total, percent, RandomUtils.getRandom(seed), mean_b, median_b, q1_b, q3_b);
//    bernoulli(total, percent, RandomUtils.getRandom(seed + 1), mean_b, median_b, q1_b, q3_b);
//    bernoulli(total, percent, RandomUtils.getRandom(seed + 2), mean_b, median_b, q1_b, q3_b);
//    bernoulli(total, percent, RandomUtils.getRandom(seed + 2), mean_b, median_b, q1_b, q3_b);
//    bernoulli(total, percent, RandomUtils.getRandom(seed + 2), mean_b, median_b, q1_b, q3_b);
    reservoir(total, total, samples, RandomUtils.getRandom(-seed + 1), mean_r, median_r, q1_r, q3_r);
//    reservoir(total, total, samples, RandomUtils.getRandom(-seed + 2), mean_r, median_r, q1_r, q3_r);
//    reservoir(total, total, samples, RandomUtils.getRandom(-seed + 3), mean_r, median_r, q1_r, q3_r);
//    reservoir(total, total, samples, RandomUtils.getRandom(-seed + 3), mean_r, median_r, q1_r, q3_r);
//    reservoir(total, total, samples, RandomUtils.getRandom(-seed + 3), mean_r, median_r, q1_r, q3_r);
//    System.out.println("Reservoir: (mean,median,25,75) " + mean_r.getAverage() + ", " +
//        median_r.getAverage() + ", " + q1_r.getAverage() + ", " + q3_r.getAverage());
//    System.out.println("Bernoulli: (mean,median,25,75) " + (1.0*mean_b.getAverage()) + ", " +
//        (1.0*median_b.getAverage()) + ", " + (1.0*q1_b.getAverage()) + ", " + (1.0*q3_b.getAverage()));
  }
  
  private static void reservoir(int samples, int total, int reservoir,
      Random rnd, RunningAverage mean_r, RunningAverage median_r, 
      RunningAverage q1_r, RunningAverage q3_r) {
    FullRunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    int[] scrambled = new int[total];
    double percent = ((double) reservoir) / total;
    for(int i = 0; i < ITERATIONS; i++) {
      scramble(rnd, total, scrambled);
      OnlineSummarizer tracker = new OnlineSummarizer(); 
      Sampler<Integer> sampler = new ReservoirSampler<Integer>(reservoir, rnd);
      stability(scrambled, sampler, samples, tracker);
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
    }
    System.out.println("Reservoir deviation: (mean,median,25,75) " + mean.getStandardDeviation() + ", " +
        median.getStandardDeviation() + ", " + q1.getStandardDeviation() + ", " + q3.getStandardDeviation());
    mean_r.addDatum(mean.getStandardDeviation());
    median_r.addDatum(median.getStandardDeviation());
    q1_r.addDatum(q1.getStandardDeviation());
    q3_r.addDatum(q3.getStandardDeviation());
  }
  
  private static void bernoulli(int total, double percent, Random rnd, RunningAverage mean_b, RunningAverage median_b, RunningAverage q1_b, RunningAverage q3_b) {
    int[] scrambled = new int[total];
    FullRunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    for(int i = 0; i < ITERATIONS; i++) {
      scramble(rnd, total, scrambled);
      OnlineSummarizer tracker = new OnlineSummarizer();
      Sampler<Integer> sampler = new BernoulliSampler<Integer>(percent, rnd);
      stability(scrambled, sampler, total, tracker);
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
    }
    mean_b.addDatum(mean.getStandardDeviation());
    median_b.addDatum(median.getStandardDeviation());
    q1_b.addDatum(q1.getStandardDeviation());
    q3_b.addDatum(q3.getStandardDeviation());
    System.out.println("Bernoulli deviation: (mean,median,25,75) " + (1.0*mean.getStandardDeviation()) + ", " +
        (1.0*median.getStandardDeviation()) + ", " + (1.0*q1.getStandardDeviation()) + ", " + (1.0*q3.getStandardDeviation()));
  }
  
  // run scrambled integer stream through sampler
  // pull output stream and get average and standard deviation
  private static void stability(int[] scrambled, Sampler<Integer> sampler,
      int samples, OnlineSummarizer tracker) {
    int count = 0;
    int sample = 0;
    while (count < samples) {
      int r = scrambled[sample++];
      while (r == -1)
        r = scrambled[sample++];
      sampler.addSample(r);
      count++;
    }
    Iterator<Integer> it = sampler.getSamples(true);
    Integer r = 0;
    while(it.hasNext()) {
      r = it.next();
      if (null != r)
        tracker.add(r);
    }
  }
  
  private static void order(int total, int[] scrambled) {
    for(int i = 0; i < total; i++) {
      scrambled[i] = i;
      }
  }
  
  private static void scramble(Random rnd, int total, int[] scrambled) {
    Arrays.fill(scrambled, -1);
    for(int i = 0; i < total * 10; i++) {
      scrambled[i % total] = rnd.nextInt(RANGE);
    }
  }
  
}
