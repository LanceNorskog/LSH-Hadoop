package org.apache.mahout.math;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

/*
 * Superclass for value-generating Vectors: ReadOnlyVector, RandomVector
 * 
 * Assumes Dense case
 */

public abstract class ReadOnlyVector extends AbstractVector implements Vector {

  protected static final String CANNOT_SET_READ_ONLY_VECTOR = "Cannot set ReadOnlyVector";

  protected ReadOnlyVector(int size) {
    super(size);
  }

  public boolean isDense() {
    return true;
  }

  public boolean isSequentialAccess() {
    return false;
  }

  public void setQuick(int index, double value) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(double value) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(DoubleDoubleFunction f, double y) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(Vector other) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(Vector other, DoubleDoubleFunction function) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(DoubleFunction function) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public Vector assign(double[] values) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }

  @Override
  public Vector clone() {
    return this;
  }

  public Vector like() {
    return new DenseVector(this);
  }

  @Override
  protected Matrix matrixLike(int rows, int columns) {
    AbstractVector dense = new DenseVector(size());
    return dense.matrixLike(rows, columns);
  }
  
  public Iterator<Element> iterator() {
    return new AllIterator(this);
  }

  protected final class AllIterator implements Iterator<Element> {

    private final DenseElement element;

    protected AllIterator(ReadOnlyVector vector) {
      element = new DenseElement(vector);
      element.index = -1;
    }

    public boolean hasNext() {
      return element.index + 1 < size();
    }

    public Element next() {
      if (element.index + 1 >= size()) {
        throw new NoSuchElementException();
      } else {
        element.index++;
        return element;
      }
    }

    public void remove() {
      throw new UnsupportedOperationException(ReadOnlyVector.CANNOT_SET_READ_ONLY_VECTOR);
    }
  }

  protected final class DenseElement implements Element {

    int index;
    final Vector vector;

    protected DenseElement(Vector vector) {
      this.vector = vector;
    }

    public double get() {
      return vector.getQuick(index);
    }

    public int index() {
      return index;
    }

    public void set(double value) {
      throw new UnsupportedOperationException(ReadOnlyVector.CANNOT_SET_READ_ONLY_VECTOR);
    }
  }
}

