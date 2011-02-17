package org.apache.mahout.cf.taste.neighborhood;

import java.util.BitSet;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;

/*
 * Dense implementation of N-dimensional hash
 */

public class DenseHash extends Hash {
  final int[] hashes;
  int lod;
  
  private long lodMask;
  int code = 0;
  
  public DenseHash(int[] hashes) { 
    this(hashes, 0);
  }
  
  public DenseHash(int[] hashes, int lod) {
    this.hashes = hashes; // duplicate(hashes);
    setLOD(lod);
    setValues(hashes);
  }
  
  public int getLOD() {
    return lod;
  }
  
  public void setLOD(int lod) {
    this.lod = lod;
    long mask = 0;
    long x = 0;
    while(x < lod) {
      mask |= (1L << x);
      x++;
    }
    this.lodMask = mask;
  }
  
//  @Override
//  public int[] getHashes() {
//    return hashes;
//  }
  
  private void setValues(int[] hashes) {
    long sum = 0;
    for(int i = 0; i < hashes.length; i++) {
      long val = ((long) hashes[i]) & ~lodMask;
      if (hashes[i] != 0) 
        sum += (1 + val) * (i + 1) * getDimensions();
    }
    super.indexes = sum;
  }
  
//  // Has to match SparseHash formula
//  @Override
//  public int hashCode() {
//    if (this.code == 0) {
//      long bits = 0;
//      for(int i = 0; i < hashes.length; i++) {
//        long val = ((long) hashes[i]) & ~lodMask;
//        bits += val + val * i;
//      }
//      bits += (lod + 1) * 13 * getDimensions();
//      this.code = (int) ( bits ^ (bits >> 32));
//    }
//    return code;
//  }
//  
//  @SuppressWarnings("unchecked")
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (obj.getClass() == SparseHash.class) 
//      return ((SparseHash) obj).equalsDense(this);
//    Hash otherH = (Hash) obj;
//    if (lod != otherH.getLOD())
//      return false;
//    if (getDimensions() != otherH.getDimensions())
//      return false;
//    if (hashCode() != otherH.hashCode())
//      return false;
//    DenseHash other = (DenseHash) obj;
//    int[] myHashes = hashes;
//    int[] otherHashes = other.hashes;
//    for(int i = 0; i < myHashes.length; i++) {
//      long myVal = ((long) myHashes[i]) & ~lodMask;
//      long otherval = ((long) otherHashes[i]) & ~lodMask;
//      if (myVal != otherval)
//        return false;
//    };
//     
//    return true;
//  }
  
  //  // sort by coordinates in order
  //  @Override
  //  public int compareTo(Hash<T> other) {
  //    for(int i = 0; i < hashes.length; i++) {
  //      if ((hashes[i] & ~lodMask) > (other.hashes[i] & ~lodMask))
  //        return 1;
  //      else if ((hashes[i] & ~lodMask) < (other.hashes[i] & ~lodMask))
  //        return -1;
  //    };
  //    if (lod > other.lod)
  //      return 1;
  //    else if (lod < other.lod)
  //      return -1;
  //    else
  //      return 0;
  //  }
  //  
  @Override
  public String toString() {
    String x = "{";
    for(int i = 0; i < hashes.length; i++) {
      x = x + (hashes[i] & ~lodMask) + ",";
    }
    return x + "}";
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
  public boolean contains(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer getValue(int index) {
    throw new UnsupportedOperationException();

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