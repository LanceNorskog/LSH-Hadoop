package org.apache.mahout.math.stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.math.stats.BernoulliSampler;
import org.apache.mahout.math.stats.ReservoirSampler;
import org.apache.mahout.math.stats.Sampler;

/*
 * Measure the stability of <s>the author</s> various sampling algorithms.
 */
public class Stability {
  static int TOTAL = 5000;
  static int N = 1000;
  static int ITERATIONS = 50;
  static int RANGE = 500;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    for(int i = 101; i < 2000; i += 300) {
      N = i;
      Random rnd = getRnd();
      double[] bmean1 = new double[ITERATIONS];
      double[] rmean1 = new double[ITERATIONS];
      full(rnd, 500, bmean1, rmean1);
      double[] bmean2 = new double[ITERATIONS];
      double[] rmean2 = new double[ITERATIONS];
      full(rnd, 1000, bmean2, rmean2);
    }
  }
  
  private static void full(Random rnd, int seed, double[] bmean, double[] rmean) {
    int total = TOTAL;
    int samples = N;
    int[] scrambled = new int[total];
    
    System.out.println("Full pass with total=" + total + ", samples=" + samples + ", seed=" + seed);
    
    Arrays.fill(scrambled, -1);
    // dups are ok
    for(int i = 0; i < total; i++) {
      scrambled[i] = rnd.nextInt(RANGE);
    }    
    rnd.setSeed(seed);
    bernoulli(total, samples, scrambled, rnd, bmean);
    rnd.setSeed(seed);
    reservoir(total, samples, scrambled, rnd, rmean);
  }
  
  private static void reservoir(int total, int samples, int[] scrambled,
      Random rnd, double[] dmean) {
    RunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    for(int i = 0; i < ITERATIONS; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer(); 
      Sampler<Integer> sampler = new ReservoirSampler<Integer>(samples, rnd);
      stability(scrambled, sampler, total, samples, tracker);
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
      dmean[i] = tracker.getMedian();
    }
    System.out.println("Reservoir stability: (mean,median,25,75) " + mean.getStandardDeviation() + ", " +
        median.getStandardDeviation() + ", " + q1.getStandardDeviation() + ", " + q3.getStandardDeviation());
  }
  
  private static void bernoulli(int total, int samples, int[] scrambled, Random rnd, double dmean[]) {
    RunningAverageAndStdDev mean = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev median = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q1 = new FullRunningAverageAndStdDev();
    RunningAverageAndStdDev q3 = new FullRunningAverageAndStdDev();
    double percent = ((double) samples)/total;
    for(int i = 0; i < ITERATIONS; i++) {
      OnlineSummarizer tracker = new OnlineSummarizer();
      Sampler<Integer> sampler = new BernoulliSampler<Integer>(percent, rnd);
      stability(scrambled, sampler, total, samples, tracker);
      mean.addDatum(tracker.getMean());
      median.addDatum(tracker.getMedian());
      q1.addDatum(tracker.getQuartile(1));
      q3.addDatum(tracker.getQuartile(3));
      dmean[i] = tracker.getMedian();
    }
    System.out.println("Bernoulli stability: (mean,median,25,75) " + (1.0*mean.getStandardDeviation()) + ", " +
        (1.0*median.getStandardDeviation()) + ", " + (1.0*q1.getStandardDeviation()) + ", " + (1.0*q3.getStandardDeviation()));
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
    int r = 0;
    while(it.hasNext()) {
      r = it.next();
      tracker.add(r);
    }
  }
  
  private static Random getRnd() {
    return new Random();
  }
  
}
