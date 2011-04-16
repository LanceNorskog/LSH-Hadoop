package lsh.mahout.core2;

import java.util.Arrays;
import java.util.Iterator;


import org.apache.commons.collections.iterators.ArrayIterator;

/*
 * Hashed values, stored in hash space.
 * Both dense and sparse supported. 
 * support sparse by adding map->index
 * 
 * Does not support Level-Of-Detail
 * needs both hash point and plus/minus for each dimension
 * 
 * Why is this mutable?
 */

public class Simplex {
  public int[] base;
  int dimensions;
  
  public Simplex(int size) {
    dimensions = size;
  }
  
  public Simplex(int[] hash) {
    this.base = hash;
    dimensions = hash.length;
  }

  public void setValues(int[] values) {
    this.base = values;
  }
  
  public int[] getValues() {
    return this.base;
  }
  
  public int getValue(int index) {
    // null exception? why, yes!
    return base[index];
  }
  
  public void setValue(int index, int value) {
    if (null == base)
      base = new int[dimensions];
    base[index] = value;
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
    return Arrays.hashCode(base) ^ (dimensions * 17);
  }
  
  @Override
  public boolean equals(Object obj) {
    Simplex other = (Simplex) obj;
    boolean same = Arrays.equals(base, other.base);
    return dimensions == other.dimensions && same;
  }


   
}
