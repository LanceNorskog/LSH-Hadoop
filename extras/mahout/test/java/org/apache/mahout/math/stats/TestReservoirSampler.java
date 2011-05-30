package org.apache.mahout.math.stats;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.MahoutTestCase;
import org.junit.Test;


public class TestReservoirSampler extends MahoutTestCase {
  boolean debug = true;
  
  /*
   * Check run with fixed random sequence
   */
  @Test
  public void testBasics() {
    Random rnd = RandomUtils.getRandom();
    Sampler<Integer> samp = new ReservoirSampler<Integer>(10, rnd);
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
    int SIZE = 200;
    Random rnd = RandomUtils.getRandom();
    Sampler<Integer> samp = fillUniqueSamples(N, SIZE, rnd);
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
    assertTrue("Samples required: " + SIZE, count == SIZE);
    assertEquals("reservoir does not save correct values", 24234, sum);
    assertTrue("reservoir samples must all be unique", intSet.size() == SIZE);
  }

  /*
   * Several runs with different random numbers.
   */
  @Test
  public void testMultiple() {
    int N = 1000;
    int SIZE = 200;
    Random rnd = RandomUtils.getRandom();
    for(int x = 0; x < 100; x++) {
      Sampler<Integer> samp = fillUniqueSamples(N, SIZE, rnd);
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
//      System.out.println("pass: " + x + ", sum: " + sum);
      assertTrue("pass: " + x + ", reservoir sum is far too small", sum >= sizeSum);
      assertTrue("pass: " + x + ", reservoir sum is far too large", sum <= 26000);
    }
  }
  
  private Sampler<Integer> fillUniqueSamples(int N, int SIZE, Random rnd) {
    Sampler<Integer> samp = new ReservoirSampler<Integer>(SIZE, rnd);
    for(int i = 0; i < N; i++)
      samp.addSample(i);
    return samp;
  }
}

