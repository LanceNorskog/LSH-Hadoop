package org.apache.mahout.cf.taste.impl.model;

import java.util.Iterator;

import lsh.core.Lookup;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

public class LPIL implements LongPrimitiveIterator {
  //	final public Lookup lookup;
  final Iterator<Long> itl;
  int i = 0;

  public LPIL(Iterator it) {
    this.itl = it;
  }

  @Override
  public long nextLong() {
    Long id = itl.next();
    return id;
  }

  @Override
  public long peek() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void skip(int n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNext() {
    return itl.hasNext();
  }

  @Override
  public Long next() {
    return this.nextLong();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();

  }

}
