package org.apache.mahout.cf.taste.neighborhood;

import java.util.BitSet;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;

/*
 * Dense implementation of N-dimensional hash
 */

public class DenseHash extends Hash {
  final int[] hashes;
  
  public DenseHash(int[] hashes) { 
    this(hashes, 0);
  }
  
  public DenseHash(int[] hashes, int lod) {
    this.hashes = hashes; // duplicate(hashes);
    setLOD(lod);
    populateHashes();
  }
  
  protected void populateHashes() {
    long sum = 0;
    for(int i = 0; i < hashes.length; i++) {
      int hash = hashes[i];
      long singleHash = getSingleHash(i, hash);
      sum += singleHash;
    }
    super.setIndexes(sum);
  }

  @Override
  public String toString() {
    String x = "{{";
    for(int i = 0; i < hashes.length - 1; i++) {
      x = x + hashes[i] + ",";
    }
    return x + hashes[hashes.length - 1] + "}, LOD:" + getLOD() + ",code=" + indexes +"}";
  }
  
  @Override
  public int getDimensions() {
    return hashes.length;
  }
  
  @Override
  public int getNumEntries() {
    return hashes.length;
  }

  @Override
  public boolean containsValue(int index) {
    return (index >= 0 && index < getDimensions());
  }

  @Override
  public Integer getValue(int index) {
    return hashes[index];

  }
  
  @Override
  public void setValue(int index, int hash) {
    int oldValue = hashes[index];
    int newValue = hash;
    super.changeIndexes(index, oldValue, newValue);
    hashes[index] = hash;
  }

//  @Override
//  public Integer next(int index) {
//    if (index < getDimensions() - 1)
//      return index + 1;
//    else
//      return null;
//  }

  @Override
  public Iterator<Integer> iterator() {
    ArrayIterator ait = new ArrayIterator(hashes);
    // magic!
    return ait;
  }

//  @Override
//  public void setBits(Hash other, BitSet bs) {
//    throw new UnsupportedOperationException();
//    
//  }
//
//  @Override
//  public void setBits(Hash other, FastIDSet fs) {
//    throw new UnsupportedOperationException();
//    
//  }
  
  //  @Override
  //  public int compareTo(Object o) {
  //    // TODO Auto-generated method stub
  //    return 0;
  //  }
  
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