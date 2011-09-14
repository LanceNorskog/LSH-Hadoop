package org.apache.mahout.math.stats;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.MahoutTestCase;
import org.junit.Test;


public class TestBernoulliSampler extends MahoutTestCase {
  boolean debug = true;
  
  /*
   * Check run with fixed random sequence
   */
  @Test
  public void testBasics() {
    Random rnd = RandomUtils.getRandom();
    Sampler samp = new BernoulliSampler<Integer>(50.0, rnd);
    samp.addSample(1);
    samp.addSample(2);
    samp.addSample(3);
    samp.addSample(3);
    samp.addSample(4);
    samp.addSample(5);
    Iterator<Integer> sit = samp.getSamples(false);
    int count = 0;
    while(sit.hasNext()) {
      Integer sample = sit.next();
      assertTrue("Wrong samples come out", sample <= 5);
      count++;
    }
    sit = samp.getSamples(true);
    count = 0;
    while(sit.hasNext()) {
      Integer sample = sit.next();
      assertTrue("Wrong samples come out", sample <= 5);
      count++;
    }
    sit = samp.getSamples(true);
    assertFalse("All samples should be flushed", sit.hasNext());
  }
  
  /*
   * Check run with fixed random sequence
   */
  @Test
  public void testDeterministic() {
    int N = 1000;
    double PERCENT = 20;
    Random rnd = RandomUtils.getRandom();
    Sampler<Integer> samp = fillUniqueSamples(N, PERCENT, rnd);
    Iterator<Integer> sit = samp.getSamples(true);
    int count = 0;
    int sum = 0;
    int sizeSum = 0;
    Set<Integer> intSet = new HashSet<Integer>();
    while(sit.hasNext()) {
      Integer sample = sit.next();
      sum += sample;
      count++;
      sizeSum += count;
      intSet.add(sample);
    }
    assertEquals("Samples roughly required: " + 199, 199, count);
    assertEquals("reservoir does not save correct values", 102595, sum);
  }

  /*
   * Several runs with different random numbers.
   */
  @Test
  public void testMultiple() {
    int N = 1000;
    int PERCENT = 20;
    Random rnd = RandomUtils.getRandom();
    for(int x = 0; x < 100; x++) {
      Sampler<Integer> samp = fillUniqueSamples(N, PERCENT, rnd);
      Iterator<Integer> sit = samp.getSamples(true);
      int count = 0;
      int sum = 0;
      int sizeSum = 0;
      while(sit.hasNext()) {
        Integer sample = sit.next();
        sum += sample;
        count++;
        sizeSum += count;
      }
      assertTrue("pass: " + x + ", reservoir sum is far too small", sum >= sizeSum);
      assertTrue("pass: " + x + ", reservoir sum is far too large", sum <= 127000);
    }
  }
  
  private Sampler<Integer> fillUniqueSamples(int N, double percent, Random rnd) {
    Sampler<Integer> samp = new BernoulliSampler<Integer>(percent, rnd);
    for(int i = 0; i < N; i++)
      samp.addSample(i);
    return samp;
  }
}

