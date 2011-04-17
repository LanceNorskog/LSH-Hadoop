package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
 * Basic Reservoir sampler.
 * Build a set of N samples. Randomly replace existing with new using increasing selectivity.
 * Sample set is always a linear distribution of current entire stream.
 */

public class ReservoirSampler<T> extends Sampler<T> {
  final int samples;
  List<T> stored;
  final Random rnd;
  int counter = 0;
  int rolling = 0;
  
  public ReservoirSampler(int samples, Random rnd) {
    stored = new ArrayList<T>(samples);
    this.samples = samples;
    this.rnd = rnd;
  }
  
  @Override
  public void addSample(T sample) {
    if (counter < samples) {
      stored.add(sample);
    } else {
      int index = rnd.nextInt(counter);
      if (index < samples) {
        stored.set(index, sample);
      }
    }
    counter++;
  }
  
  @Override
  public T getSample() {
    if (rolling >= counter)
      return null;
    T sample = stored.get(rolling);
    rolling++;
    if (rolling == samples)
      rolling = 0;
    return sample;
  }
  
  @Override
  public Iterator<T> getSamples(boolean flush) {
    Iterator<T> it = stored.iterator();
    if (flush) 
      stored = new ArrayList<T>(samples);
    return it;
  }
  
  @Override
  public void stop() {
    stored = null;
    
  }
  
}
