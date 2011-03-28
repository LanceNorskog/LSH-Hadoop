/**
 * 
 */
package org.apache.mahout.math.quantize;

import java.util.Iterator;

/**
 * Quantize a continuously-valued item to a discrete set of values.
 * 
 * Unless otherwise specified, values will be replaced by the 
 * lowest-valued matching quantization.
 * 
 * Quantizers must implement "T quantize(T value)", nothing more
 * For a fixed set, iterators should walk the entire set.
 * For "infinite" sets, iterator is up to the implementor
 * 
 * All values returned are immutable- do not change them.
 */
public abstract class Quantizer<T> {
  
  /*
   * Return quantized value
   * This is the only required method
   */
  public abstract T quantize(T value);
  
  /*
   * Quantize and copy
   * Not required.
   */
  public void quantize(T value, T target) {
    throw new UnsupportedOperationException();
  }
  
 /*
  * Return Iterators of all members of discrete set
   * Not required.
  */
  public Iterator<T> getMatches() {
    throw new UnsupportedOperationException();
  }
  
  /*
   * Return Iterator<T> of "nearest" matches to given value.
   * Where "nearest" can mean anything.
   * Factor can be a count, a maximum distance, whatever is appropriate
   * Not required.
   */
  public Iterator<T> getNearest(T value, Double factor) {
    throw new UnsupportedOperationException();
  }

  /*
   * Return ID of quantized value
   * Not required.
   */
  public long toID(T value) {
    throw new UnsupportedOperationException();
  }

  /*
   * Return Iterable list of Long-valued IDs for discrete set
   * Not required.
   */
  public Iterator<Long> getIDs() {
    throw new UnsupportedOperationException();
  }

  /*
   * Return matcher for ID
   * Not required.
   */
  T toValue(long value) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public int hashCode() {
    throw new UnsupportedOperationException();
  }
}
