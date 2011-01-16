package org.apache.mahout.math;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;

/*
 * Vector that always returns the same value.
 * Useful for zero/unit vectors.
 * Superclass for other prefab-value Vectors.
 * 
 * like() method can create any Vector class
 */

public class FixedValueVector extends ReadOnlyVector implements Vector {

  final double value;
  
  public FixedValueVector(int size, double value) {
    super(size);
    this.value = value;
  }

  public int getNumNondefaultElements() {
    return value == 0.0 ? 0 : size();
  }

   public Iterator<Element> iterateNonZero() {
    if (value == 0.0) {
      return new ZeroIterator();
    } else {
      return new AllIterator(this);
    }
  }

  public Iterator<Element> iterator() {
    return new AllIterator(this);
  }

  @Override
  public double get(int index) {
    if (index < 0 || index >= size())
      throw new IndexException(index, size());
    return value;
  }

  public double getQuick(int index) {
    return value;
  }

  protected final class ZeroIterator implements Iterator<Element> {

    public boolean hasNext() {
      return false;
    }

    public Element next() {
      throw new UnsupportedOperationException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }


}


