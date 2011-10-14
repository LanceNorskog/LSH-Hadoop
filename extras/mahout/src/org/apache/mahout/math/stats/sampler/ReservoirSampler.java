package org.apache.mahout.math.stats.sampler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
 * Basic Reservoir sampler.
 * Build a set of N samples. Randomly replace existing with new using increasing selectivity.
 * Sample set is always a linear distribution of current entire stream.
 * Distribution can change while iterating. 
 */

public class ReservoirSampler<T> {
  final private int length;
  private List<T> stored;
  final private Random rnd;
  private long nextIndex;
  private long counter = 1;
  
  public ReservoirSampler(int length, Random rnd) {
    this.length = length;
    stored = new ArrayList<T>(length);
    this.rnd = rnd;
    stage();
    for(int i = 0; i < length; i++)
      stored.add(null);
  }
  
  public void addSample(T sample) {
    if (counter <= length) {
      stored.add(sample);
    } else if (check(sample)){
      stored.set((int) nextIndex, sample);
      stage();
    }
    counter++;
  }
  
  public Iterator<T> getSamples(boolean flush) {
    Iterator<T> it = stored.iterator();
    if (flush) 
      stored = new ArrayList<T>(length);
    return it;
  }
  
  private boolean check(T sample) {
    return (nextIndex < length);
  }
  
  private void stage() {
    long x = rnd.nextLong();
    while (x == 0)
      x = rnd.nextLong();
    nextIndex = Math.abs(x) % counter; 
  }
  
}
