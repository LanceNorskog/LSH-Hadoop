package org.apache.mahout.math.quantize;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lsh.mahout.core.Hasher;
import lsh.mahout.core2.Simplex;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;


/*
 * Wrap N-dimensional Simplex as a vector. 
 * Could be write-through, but Simplex has to support this also.
 * 
 * Should not have Dense v.s. Sparse problems. That should be independent.
 */

public  class SimplexVector extends AbstractVector implements Vector {
  final Simplex<?> hash;
  final Hasher hasher;
  
  protected static final String CANNOT_SET_READ_ONLY_VECTOR = "Cannot set ReadOnlyVector";
  
  protected SimplexVector(Simplex<?> hash, Hasher hasher) {
    super(hash.getDimensions());
    this.hash = hash;
    this.hasher = hasher;
  }
  
  public boolean isDense() {
    return true;
  }
  
  public boolean isSequentialAccess() {
    return false;
  }
  
  @Override
  public void setQuick(int index, double value) {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  @Override
  public double getQuick(int index) {
    if (! hash.containsValue(index))
      return 0;
    int[] ha = new int[1];
    ha[0] = hash.getValue(index);
    double[] da = new double[1];
    hasher.unhash(ha, da);
    return da[0];
  }
  
  @Override
  public Vector clone() {
    throw new UnsupportedOperationException(CANNOT_SET_READ_ONLY_VECTOR);
  }
  
  public Vector like() {
    return new DenseVector(this);
  }
  
  @Override
  protected Matrix matrixLike(int rows, int columns) {
    return new DenseMatrix(rows, columns);
  }
  
  public Iterator<Element> iterator() {
    return new AllIterator(this);
  }
  
  protected final class AllIterator implements Iterator<Element> {
    
    private final DenseElement element;
    
    protected AllIterator(SimplexVector vector) {
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
      throw new UnsupportedOperationException(SimplexVector.CANNOT_SET_READ_ONLY_VECTOR);
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
      throw new UnsupportedOperationException(SimplexVector.CANNOT_SET_READ_ONLY_VECTOR);
    }
  }
  
  @Override
  public int getNumNondefaultElements() {
    return hash.getNumEntries();
  }
  
  @Override
  public Iterator<Element> iterateNonZero() {
    return new SimplexVectorIterator();
  }
  
  public Simplex<?> getSimplex () {
    return hash;
  }
  
  class SimplexVectorIterator implements Iterator<Element> {
    private Iterator<Integer> it = hash.iterateValues();
    final SimplexVectorElement el = new SimplexVectorElement(hasher);
    double[] v = new double[1];
    int[] h = new int[1];
    
    @Override
    public boolean hasNext() {
      return it.hasNext();
    }
    
    public boolean containsValue(int index) {
      return true;
    }
    
    public int getDimensions() {
      return hash.getDimensions();
    }
    
    @Override
    public Element next() {
      Integer index = it.next();
      el.index = index;
      h[0] = hash.getValue(index);
      hasher.unhash(h, v);
      el.value = v[0];
      return el;
    }
    
    @Override
    public void remove() {
      throw new IllegalStateException();
      
    }
    
    class SimplexVectorElement implements Vector.Element {
      int index;
      double value;
      final Hasher hasher;
      
      SimplexVectorElement(Hasher hasher) {
        this.hasher = hasher;
      }
      
      public double get() {
        return value;
      }
      
      public int index() {
        return index;
      }
      
      public void set(double value) {
        ;
      }
    }
    
  }
  
}