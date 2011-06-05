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

public class ReservoirSampler<T> extends Sampler<T> {
  final int length;
  List<T> stored;
  final Random rnd;
  long nextIndex;
  long counter = 1;

  public ReservoirSampler(int length, Random rnd) {
    stored = new ArrayList<T>(length);
    this.length = length;
    this.rnd = rnd;
    counter = 1;   // don't fill at first
    stage();
  }
  
  @Override
  public void addSample(T sample) {
    if (counter <= length) {
      stored.add(sample);
    } else if (check()){
      stored.set((int) nextIndex, sample);
      stage();
    }
    counter++;
  }
  
  @SuppressWarnings("unchecked")   // there's some way to do genericized empty list
  @Override
  public Iterator<T> getSamples(boolean flush) {
    if (null == stored)
      return (Iterator<T>) Collections.emptyList().iterator();
    Iterator<T> it = stored.iterator();
    if (flush) 
      stored = new ArrayList<T>(length);
    return it;
  }
  
  @Override
  public void stop() {
    stored = null;
  }
  
  @Override
  public boolean isSampled(T sample) {
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
