package org.apache.mahout.cf.taste.neighborhood;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail and generic payload
 * identity functions use corner and level-of-detail
 * equals & hashCode match for any two hashes at given level of detail
 */

public abstract class Hash<T> implements  Cloneable{ // Comparable<Hash<T>>,
  abstract public int[] getHashes();  // ewwwwww - only for equals()
  public abstract T getPayload();
  public abstract void setLOD(int lod);
  public abstract int getLOD();
  public abstract int getDimensions();
  public abstract int getNumEntries();
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