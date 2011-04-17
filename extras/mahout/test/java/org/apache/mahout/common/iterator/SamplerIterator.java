package org.apache.mahout.common.iterator;

import java.util.Iterator;

import org.apache.mahout.math.stats.Sampler;

public class SamplerIterator<T> implements Iterator<T> {
  final Sampler<T> sampler;

  public SamplerIterator(Sampler<T> sampler) {
    this.sampler = sampler;
  }
  
  @Override
  public boolean hasNext() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public T next() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub
    
  }

}
