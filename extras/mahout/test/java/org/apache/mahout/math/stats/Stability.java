package org.apache.mahout.math.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.ReservoirSampler;
import org.apache.mahout.math.stats.Sampler;

/*
 * Measure the stability of <s>the author</s> various sampling algorithms.
 */
public class Stability {
  static int TOTAL = 50000;        // total number of samples
  static int N = 1000;            // number of samples to fetch and add
  static int ITERATIONS = 50;     // number of iterations to build standard deviation
  static int RANGE = 500;         // numerical samples are 0-RANGE
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    for(int samples = 101; samples < 4000; samples += 300) {
      System.out.println("Full pass with total=" + TOTAL + ", samples=" + samples);
      full(TOTAL, samples, 500);
      full(TOTAL, samples, 600);
    }
  }
  
  private static void full(int total, int samples, long seed) {
    int[] scrambled = new int[total];
    double percent = ((double) samples) / total;
    bernoulli(total, percent, scrambled, RandomUtils.getRandom(seed));
//    reservoir(samples * 5, total, (int) (total * percent), scrambled, rnd, "(short)");
    reservoir(total, total, (int) (total * percent), scrambled, RandomUtils.getRandom(-seed), " (full)");
  }

  private static void reservoir(int samples, int total, int reservoir, int[] scrambled,
      Random rnd, String tag) {
    FullRunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    FullRunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
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
    System.out.println("Reservoir deviation " + tag + ": (mean,median,25,75) " + mean.getStandardDeviation() + ", " +
        median.getStandardDeviation() + ", " + q1.getStandardDeviation() + ", " + q3.getStandardDeviation());
  }
  
  private static void bernoulli(int total, double percent, int[] scrambled, Random rnd) {
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
    System.out.println("Bernoulli deviation        : (mean,median,25,75) " + (1.0*mean.getStandardDeviation()) + ", " +
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
    int r = 0;
    while(it.hasNext()) {
      r = it.next();
      tracker.add(r);
    }
  }
  
  private static void scramble(Random rnd, int total, int[] scrambled) {
    Arrays.fill(scrambled, -1);
    for(int i = 0; i < total; i++) {
      scrambled[i] = rnd.nextInt(RANGE);
    }
  }
  
}
