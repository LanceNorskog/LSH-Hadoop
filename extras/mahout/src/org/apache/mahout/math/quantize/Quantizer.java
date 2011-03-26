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
 * Quantizers with fixed sets should implement iterators and itemIDs.
 */
public abstract class Quantizer<T> {
  
  /*
   * Required
   */
  // Return quantized value
  public abstract T quantize(T value);
  
  // Return ID of quantized value
  public long toID(T value) {
    throw new UnsupportedOperationException();
  }

  // Iterable list of all members of discrete set
  public Iterator<Long> getIDs() {
    throw new UnsupportedOperationException();
  }

  // Return value for ID
  T toValue(long value) {
    throw new UnsupportedOperationException();
  }
}
