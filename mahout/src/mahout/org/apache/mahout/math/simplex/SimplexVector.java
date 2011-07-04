package org.apache.mahout.math.simplex;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

/*
 * Wrap N-dimensional Simplex as a vector. 
 * Could be write-through, but Simplex has to support this also.
 * Writing once at a time only works with OrthonormalHasher, not VertexTransitiveHasher
 */

public class SimplexVector<T> extends AbstractVector implements Vector {
  final Simplex<T> simplex;
  final Hasher hasher;
  Double factor;
  
  protected static final String CANNOT_SET_READ_ONLY_VECTOR = "Cannot set ReadOnlyVector";
  
  public SimplexVector(Simplex<T> simplex, Hasher hasher) {
    super(simplex.getDimensions());
    if (simplex.getFactor() == null) 
      throw new UnsupportedOperationException("");
    this.simplex = simplex;
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
    if (! simplex.containsValue(index))
      return 0;
    int[] ha = new int[1];
    ha[0] = simplex.getValue(index);
    double[] da = new double[1];
    hasher.unhashDense(ha, da, factor);
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
    
    protected AllIterator(SimplexVector<T> vector) {
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
    return simplex.getNumEntries();
  }
  
  @Override
  public Iterator<Element> iterateNonZero() {
    return new SimplexVectorIterator();
  }
  
  public Simplex<T> getSimplex() {
    return simplex;
  }
  
  class SimplexVectorIterator implements Iterator<Element> {
    int index = 0;
    final SimplexVectorElement el = new SimplexVectorElement(hasher);

    
    @Override
    public boolean hasNext() {
      return index < simplex.dimensions;
    }
    
    @Override
    public Element next() {
      double[] v = new double[1];
      int[] h = new int[1];
      el.index = index;
      h[0] = simplex.getValue(index);
      hasher.unhashDense(h, v, factor);
      el.value = v[0];
      index++;
      return el;
    }
    
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
      
    }
    
    class SimplexVectorElement implements Element {
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