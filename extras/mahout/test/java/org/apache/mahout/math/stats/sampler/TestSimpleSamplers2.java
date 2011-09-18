package org.apache.mahout.math.stats.sampler;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.MahoutTestCase;
import org.junit.Test;


public class TestSimpleSamplers extends MahoutTestCase {
  boolean debug = true;
  
  /*
   * Check run with fixed random sequence
   */
  @Test
  public void testBernoulli1() {
    Random rnd = RandomUtils.getRandom();
    BernoulliSampler samp = new BernoulliSampler(0.3, rnd);
    assertFalse(samp.isSampled());
    assertTrue(samp.isSampled());
    assertFalse(samp.isSampled());
    assertTrue(samp.isSampled());
    assertTrue(samp.isSampled());
    assertTrue(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());
    assertFalse(samp.peek());

    samp.pushback(true);
    assertTrue(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    assertTrue(samp.isSampled());
  }
  
  @Test
  public void testNth() {
    NthSampler samp = new NthSampler(6, 0);
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    assertTrue(samp.isSampled());
    assertFalse(samp.isSampled());
    assertFalse(samp.isSampled());
    
  }
}

