package org.apache.mahout.math.stats;

import java.util.Iterator;

/*
 * Provide sampling decisions. No attempt to store samples.
 */

public class Sampler2<T> implements Iterator<T> {
  
  final Iterator<T> source;
  
  public Sampler2(Iterator<T> source) {
    this.source = source;
  }
  
  public Iterator<T> getBernoulliIterator() {
    return null;
  }
  
  public Iterator<T> getBernoulliIterator(SampleFunction<T> func) {
    return null;
  }
  
  public Iterator<T> getReservoirIterator() {
    return null;
  }
  
  public Iterator<T> getReservoirIterator(SampleFunction<T> func) {
    return null;
  }
  
  public Iterator<T> getSliceSampleIterator
  
}
