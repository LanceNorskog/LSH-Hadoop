package org.apache.mahout.math.simplex;

import java.util.Arrays;
import java.util.Iterator;


import org.apache.commons.collections.iterators.ArrayIterator;

/*
 * Hashed values, stored in hash space.
 * Dense only
 * No knowledge of Vectors. Simple data structure.
 * 
 * Contains the base hash, and optionally the list of neighbor simplexes.
 * Without that list, only represents the lowest corner of a rectangular hypersolid.
 * 
 * boolean[] is probably not packed. "Tomorrow is another day."
 * "factor" is value owned by the hasher. VertexTransitive needs the sum of all values.
 */

public class Simplex<T> {
  final T label;
  public final int[] base;
  public final boolean[] neighbors;
  public final int dimensions;
  final Double factor;
  
  private Simplex(int dimensions, boolean hasNeighbors, T label, Double factor) {
    this.dimensions = dimensions;
    base = new int[dimensions];
    neighbors = hasNeighbors ? new boolean[dimensions] : null;
    this.label = label;
    this.factor = factor;
  }
  
  public Simplex(int[] hash, boolean[] neighbors, Double factor, T label) {
    this.base = hash;
    dimensions = hash.length;
    this.neighbors = neighbors;
    this.label = label;
    this.factor = factor;
  }
  
  public boolean hasNeighbors() {
    return neighbors != null;
  }
  
  /*
   * Get i'th neighbor Simplex. It has no neighbor descriptor or label.
   */
  public Simplex<T> getNeighbor(int index) {
    int[] nabeHash = new int[dimensions];
    for(int i = 0; i < dimensions; i++) {
      nabeHash[i] = neighbors[i] ? base[i] + 1 : base[i];
    }
    Simplex<T> nabe = new Simplex<T>(nabeHash, null, factor, null);
    return nabe;
  }

  public int[] getValues() {
    return this.base;
  }
  
  public int getValue(int index) {
    // null exception? why, yes!
    return base[index];
  }
  
  public Double getFactor() {
    return factor;
  }
  
  public int getDimensions() {
    return dimensions;
  }

  public boolean containsValue(int index) {
    return (index >= 0 && index < dimensions);
  }
  
  public int getNumEntries() {
    return dimensions;
  }

  @SuppressWarnings("unchecked")
  public Iterator<Integer> iterateValues() {
     ArrayIterator ait = new ArrayIterator(base);
    // magic! I did not know this was possible!
    return (Iterator<Integer>) ait;
  }

  // big trouble ahead- dense and sparse should be equal if equal values
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(base) ^ (dimensions * 17) ^ (neighbors != null ? Arrays.hashCode(neighbors) : 0xffffffff)
      ^ (label != null ? label.hashCode() : 0x77777777);
  }
  
  @Override
  public boolean equals(Object obj) {
    Simplex<T> other = (Simplex<T>) obj;
    // TODO: no, have to compare contents
    if (label != other.label)
      return false;
    if (dimensions != other.dimensions)
      return false;
    if (! Arrays.equals(base, other.base))
      return false;
    if (! Arrays.equals(neighbors, other.neighbors))
      return false;
    return dimensions == other.dimensions;
  }
   
}
