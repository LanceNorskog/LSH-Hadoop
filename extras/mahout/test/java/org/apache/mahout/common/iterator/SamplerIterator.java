package org.apache.mahout.common.iterator;

import java.util.Iterator;

import org.apache.mahout.math.stats.Sampler;

// bogus: Sampler interface cannot supply enough data to make this useful.
public class SamplerIterator<T> implements Iterator<T> {
  final Iterator<T> iter;

  public SamplerIterator(Sampler<T> sampler) {
    this.iter = sampler.getSamples(true);
  }
  
  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public T next() {
    return iter.next();
  }

  @Override
  public void remove() {
    iter.remove();
  }

}
