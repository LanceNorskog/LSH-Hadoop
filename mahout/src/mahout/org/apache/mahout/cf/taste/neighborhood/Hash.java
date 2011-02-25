package org.apache.mahout.cf.taste.neighborhood;

import java.util.Iterator;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail
 * Maintains unique sum of all dimensional values,
 * which means that available "headroom" for hashes 
 * shrinks with # of dimensions
 * 
 * Supports mutability of Level Of Detail, and individual values
 * Operates in Hash space only (Integer now, may be shorts later)
 * 
 * Mutability of values is optional for subclasses.
 * 
 * All Hash subclasses with the same data must match- matching is implemented here.
 * 
 * TODO: sortable hashes? By dimension?
 * TODO: Writable
 * TODO: remove Taste library uses, or move FastID stuff into main Mahout
 */

public abstract class Hash { 
  // sum of all (index * value@index);
  // unique per hash
  protected long uniqueSum;
  private int lod;
  private long lodMask;
  private int lodShift;
  
  protected void copy(Hash other) {
    other.uniqueSum = uniqueSum;
    other.lod = lod;
    other.lodMask = lodMask;
  }
  public abstract int getDimensions();
  public abstract int getNumEntries();
  /*
   * return null if index does not exist
   */
  public abstract Integer getValue(int index);
  public abstract void setValue(int index, int hash);
  public abstract boolean containsValue(int index);
  // iterate index of active values
  public abstract Iterator<Integer> iterator();
  
  /*
   * Level Of Detail and individual hash values are mutable
   */
  public final int getLOD() {
    return lod;
  }
  
  public final void setLOD(int lod) {
    this.lod = lod;
    long mask = 0;
    long x = 0;
    while(x < lod) {
      mask |= (1L << x);
      x++;
    }
    this.lodMask = mask;
  }
  
  /*
   * Common Hash value management: sum all ints into a long.
   * TODO: Figure out maximum dimensions. May have to limit range of hashes to shorts?
   */
  public final boolean equals(Object obj) {
    Hash other = (Hash) obj;
    return uniqueSum == other.uniqueSum;
  }
  
  public final int hashCode() {
    return (int) (uniqueSum ^ (uniqueSum >> 32));
  }
  
  protected final long getSingleHash(int index, int hash) {
    if (hash == 0)
      return 0;
    long val = ((long) hash) >> this.lod;
    long value =  val * (index + 1) * getDimensions();
    return value;
  }
  
  protected final void setIndexes(long indexes) {
    this.uniqueSum = indexes;
  }
  
  protected final void changeIndexes(int index, int oldHash, int newHash) {
    long oldSingle = getSingleHash(index, oldHash);  
    long newSingle = getSingleHash(index, newHash);  
    this.uniqueSum -= oldSingle;
    this.uniqueSum += newSingle;
  }
}
