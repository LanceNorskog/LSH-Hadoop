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
  long indexes;
  
//  abstract public int[] getHashes();  // ewwwwww - only for equals(). and distance
  public abstract void setLOD(int lod);
  public abstract int getLOD();
  public abstract int getDimensions();
  public abstract int getNumEntries();
//  public abstract void setBits(Hash other, BitSet bs);
//  public abstract void setBits(Hash other, FastIDSet fs);
  public abstract Integer getValue(int index);
  public abstract boolean contains(int index);
  public abstract Integer next(int index);
  public abstract Iterator<Integer> iterator();
  
  public final boolean equals(Object obj) {
    Hash other = (Hash) obj;
    if (indexes != other.indexes)
      this.hashCode();
    return indexes == other.indexes;
  }
  
  public final int hashCode() {
    return (int) (indexes ^ (indexes >> 32));
  }
}

/* only compare values at index */
//class HashSingleComparator implements Comparator<Hash>{
//  final int index;
//
//  public HashSingleComparator(int index) {
//    this.index = index;
//  }
//
//  @Override
//  public int compare(Hash o1, Hash o2) {
//    if (o1.hashes[index] < o2.hashes[index])
//      return 1;
//    else if (o1.hashes[index] > o2.hashes[index])
//      return -1;
//    else
//      return 0;
//  }
//
//
//}