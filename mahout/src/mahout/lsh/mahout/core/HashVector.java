package lsh.mahout.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lsh.core.Hasher;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

/*
 * Wrap N-dimensional Hash as a vector. 
 * Could be write-through, but Hash has to support this also.
 */

public  class HashVector extends AbstractVector implements Vector {
  final Hash hash;
  final Hasher hasher;
  
  protected static final String CANNOT_SET_READ_ONLY_VECTOR = "Cannot set ReadOnlyVector";
  
  protected HashVector(Hash hash, Hasher hasher) {
    super(hash.getDimensions());
    this.hash = hash;
    this.hasher = hasher;
  }
  
  public boolean isDense() {
    return hash.getClass() == DenseHash.class;
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
    
    protected AllIterator(HashVector vector) {
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
      throw new UnsupportedOperationException(HashVector.CANNOT_SET_READ_ONLY_VECTOR);
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
      throw new UnsupportedOperationException(HashVector.CANNOT_SET_READ_ONLY_VECTOR);
    }
  }
  
  @Override
  public int getNumNondefaultElements() {
    return hash.getNumEntries();
  }
  
  @Override
  public Iterator<Element> iterateNonZero() {
    return new HashVectorIterator();
  }
  
  public Hash getHash() {
    return hash;
  }
  
  class HashVectorIterator implements Iterator<Element> {
    private Iterator<Integer> it = hash.iterator();
    final HashVectorElement el = new HashVectorElement(hasher);
    double[] v = new double[1];
    int[] h = new int[1];
    
    @Override
    public boolean hasNext() {
      return it.hasNext();
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
      // TODO Auto-generated method stub
      
    }
    
    class HashVectorElement implements Element {
      int index;
      double value;
      final Hasher hasher;
      
      HashVectorElement(Hasher hasher) {
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