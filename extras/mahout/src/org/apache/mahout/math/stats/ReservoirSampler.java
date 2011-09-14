package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
 * Basic Reservoir sampler.
 * Build a set of N samples. Randomly replace existing with new using increasing selectivity.
 * Sample set is always a linear distribution of current entire stream.
 * Distribution can change while iterating. 
 */

public class ReservoirSampler extends Sampler{
  final int length;
  final Random rnd;
  long nextIndex;
  long counter = 1;

  public ReservoirSampler(int length, Random rnd) {
    this.length = length;
    this.rnd = rnd;
    counter = 1;   // don't fill at first
    stage();
  }
  
  @Override
  protected boolean sample() {
      boolean val = check();
      stage();
      return val;
  }
  
  private boolean check() {
    return (nextIndex < length);
  }
  
  private void stage() {
    long x = rnd.nextLong();
    nextIndex = Math.abs(x) % counter; 
  }
  
}
