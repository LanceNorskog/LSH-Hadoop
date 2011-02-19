package org.apache.mahout.cf.taste.neighborhood;

import java.util.Iterator;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail
 * Maintains unique sum of all dimensional values,
 * which means that available "headroom" for hashes 
 * shrinks with # of dimensions
 */

public abstract class Hash { 
  // sum of all (index * value@index);
  // unique per hash
  protected long indexes;
  private int lod;
  private long lodMask;
  
  protected void copy(Hash other) {
    other.indexes = indexes;
    other.lod = lod;
    other.lodMask = lodMask;
  }
  public abstract int getDimensions();
  public abstract int getNumEntries();
//  public abstract void setBits(Hash other, BitSet bs);
//  public abstract void setBits(Hash other, FastIDSet fs);
  /*
   * return null if index does not exist
   */
  public abstract Integer getValue(int index);
  public abstract void setValue(int index, int hash);
  public abstract boolean containsValue(int index);
//  public abstract Integer next(int index);
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
    if (indexes != other.indexes)
      this.hashCode();
    return indexes == other.indexes;
  }
  
  public final int hashCode() {
    return (int) (indexes ^ (indexes >> 32));
  }
  
  protected final long getSingleHash(int index, int hash) {
    if (hash == 0)
      return 0;
    long val = ((long) hash);
    long value =  val * (index + 1) * getDimensions();
    return value;
  }
  
  protected final void setIndexes(long indexes) {
    this.indexes = indexes;
  }
  
  protected final void changeIndexes(int index, int oldHash, int newHash) {
    long oldSingle = getSingleHash(index, oldHash);  
    long newSingle = getSingleHash(index, newHash);  
    this.indexes -= oldSingle;
    this.indexes += newSingle;
  }
}
