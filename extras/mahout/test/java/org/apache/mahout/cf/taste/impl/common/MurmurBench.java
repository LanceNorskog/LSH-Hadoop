package org.apache.mahout.cf.taste.impl.common;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.MurmurHashRandom;

public class MurmurBench {
  static int HUGE = 1000000;
  static int count = 0;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    double[] big = new double[HUGE];
    // prewarm caches
    for(int i = 0; i < HUGE; i++)
      big[i] = i;
    for(int i = 0; i < HUGE; i++)
      count += big[i];
    Random rnd = new Random(0);
    Random mm = new MurmurHashRandom(0);
    Random mersenne = RandomUtils.getRandom(0);
    check("JDK", big, rnd);
    check("MurmurHash", big, mm);
    check("MersenneTwister", big, mersenne);
  }

  private static void check(String kind, double[] big, Random rnd) {
    long last = System.currentTimeMillis();
    for(int i = 0; i < HUGE; i++)
      big[(i % 1) == 0 ? i : (HUGE -1) -i] = rnd.nextDouble();
    long bench = System.currentTimeMillis() - last;
    System.out.println(kind + ": ms=" + bench + ", stdev=" + stdev(big));
  }

  private static double stdev(double[] big) {
    RunningAverageAndStdDev full = new FullRunningAverageAndStdDev();
    for(int i = 0; i < big.length; i++) {
      full.addDatum(big[i]);
    }
    return full.getStandardDeviation();
  }
  
}
