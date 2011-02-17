package org.apache.mahout.cf.taste.neighborhood;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lsh.core.Hasher;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.math.Vector.Element;

/*
 * Sparse implementation of N-dimensional hash.
 */

public class SparseHash extends Hash {
  
  static Map<Hash,int[]> hashCache = new HashMap<Hash, int[]>();
  //  final int[] hashes;
  FastByIDMap<Integer> sparseHashKeys;
  int[] sparseHashes;
  int lod;
  private long lodMask;
  int code = 0;
  int dimensions;
  
  public SparseHash(int[] hashes) { 
    this(hashes, 0);
  }
  
  public SparseHash(int[] hashes, int lod) {
    setHashes(hashes);
    setLOD(lod);
//    this.hashes = hashes;
  }
  
  public SparseHash(SparseHash sp, int lod) {
    this.sparseHashes = sp.sparseHashes;
    this.sparseHashKeys = sp.sparseHashKeys;
    setLOD(lod);
    this.dimensions = sp.dimensions;
    setIndexes();
//    if (null != sp.hashes)
//      hashes = sp.hashes;
  }
  
  public SparseHash(Hasher hasher, Iterator<Element> el, int dimensions, int lod) {
    setValues(hasher, el);
    this.dimensions = dimensions;
    this.lod = lod;
    setLOD(lod);
  }
  
  private void setValues(Hasher hasher, Iterator<Element> el) {
    sparseHashKeys = new FastByIDMap<Integer>();
    List<Integer> values = new ArrayList<Integer>(dimensions);
    int index = 0;
    double[] d = new double[1];
    int[] h = null;
    
    while(el.hasNext()) {
      Element e = el.next();
      sparseHashKeys.put(e.index(), new Integer(index));
      d[0] = e.get();
      h = hasher.hash(d);  
      values.add(h[0]);
      index++;
    } 
    sparseHashes = new int[index];
    long sum = 0;
    for(int i = 0; i < index; i++) {
      sparseHashes[i] = values.get(i);
      sum += (1 + (sparseHashes[i] & ~lodMask)) * (i + 1) * getDimensions();
    }
  }
  
  private void setHashes(int[] hashes) {
    dimensions = hashes.length;
    int nonZero = 0;
    for(int i = 0; i < dimensions; i++) {
      if (hashes[i] != 0)
        nonZero++;
    }
    sparseHashKeys = new FastByIDMap<Integer>();
    sparseHashes = new int[nonZero];
    int index = 0;
    int skip = 0;
    long sum = 0;
    for(int i = 0; i < dimensions; i++) {
      if (hashes[i] != 0) {
        sparseHashKeys.put(i, new Integer(index));
        sparseHashes[index] = hashes[i];
        sum += (1 + (hashes[i] & ~lodMask)) * (i + 1) * getDimensions();
        index++;
      } else
        skip++;
    }
    super.indexes = sum;
  }
  
  private void setIndexes() {
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    long sum = 0;
    while(it.hasNext()) {
      long index = it.nextLong();
      int i = sparseHashKeys.get(index);
      int dim = getDimensions();
      long value = (1 + (sparseHashes[(int) index] & ~lodMask)) * (i + 1) * dim;
      sum += value;
    }
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
//    ((Object) null).hashCode();
//    if (null == hashes) {
//      int[] myHashes = new int[dimensions];
//      LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
//      while(it.hasNext()) {
//        long x = it.next();
//        int index = sparseHashKeys.get(x);
//        myHashes[(int) x] = sparseHashes[index];
//      }
//      hashes = myHashes;
//    }
//    return hashes;
//  }
  
  /*
   * Has to match DenseHash formula
   */
//  @Override
//  public int hashCode() {
//    if (this.code == 0) {
//      long bits = 0;
//      int index = 0;
//      LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
//      while(it.hasNext()) {
//        int offset = (int) it.nextLong();
//        long val = ((long) sparseHashes[index]) & ~lodMask;
//        bits += val + val * offset;
//        index++;
//      }
//      bits += (lod + 1) * 13 * getDimensions();
//      this.code = (int) ( bits ^ (bits >> 32));
//    }
//    
//    return this.code;
//  }
//  
//  @SuppressWarnings("unchecked")
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    Hash otherH = (Hash) obj;
//    if (lod != otherH.getLOD())
//      return false;
//    if (getDimensions() != otherH.getDimensions())
//      return false;
//    if (hashCode() != obj.hashCode())
//      return false;
//    if (obj.getClass() == DenseHash.class)
//      return this.equalsDense((DenseHash) obj);
//    SparseHash other = (SparseHash) obj;
//    boolean finished = false;
//    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
//    LongPrimitiveIterator itOther = other.sparseHashKeys.keySetIterator();
//    int current = (int) it.nextLong();
//    int currentOther = (int) itOther.nextLong();
//    int index = 0;
//    int indexOther = 0;
//    while(! finished) {
//      long value = (((long) sparseHashes[index]) & ~lodMask); 
//      long valueOther = (((long) other.sparseHashes[indexOther]) & ~lodMask);
//      if (current < currentOther) {
//        if (value != 0) {
//          return false; 
//        } 
//        if (!it.hasNext())
//          return false;
//        current = (int) it.nextLong();
//        index++;
//      } else if (current > currentOther) {
//        if (valueOther != 0)
//          return false;
//        if (! itOther.hasNext())
//          return false;
//        currentOther = (int) itOther.nextLong();
//        indexOther++;
//      } else if (current == currentOther) {
//        if (value != valueOther)
//          return false;
//        if (!it.hasNext() && !itOther.hasNext())
//          return true;
//        else if (!it.hasNext() || !itOther.hasNext())
//          return false;
//        current = (int) it.nextLong();
//        index++;
//        currentOther = (int) itOther.nextLong();
//        indexOther++;
//      }
//    }
//    return !(it.hasNext() || itOther.hasNext());
//  }
  
//   boolean equalsDense(DenseHash other) {
//    int[] myHashes = this.getHashes();
//    int[] otherHashes = other.getHashes();
//    for(int i = 0; i < myHashes.length; i++) {
//      long myVal = ((long) myHashes[i]) & ~lodMask;
//      long otherval = ((long) otherHashes[i]) & ~lodMask;
//      if (myVal != otherval)
//        return false;
//    };
//    return true;
//  }
  
  
  /*
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    Hash<T> other = (Hash<T>) obj;
    if (lod != other.getLOD())
      return false;
    
    //    if ((null == payload && null != other.payload) || (null != payload && null == other.payload))
    //      return false;
    //    if (null != payload && !payload.equals(other.payload))
    //      return false;
    int[] otherHashes = other.getHashes();
    //    if (null == otherHashes)
    //      this.hashCode();
    int[] myHashes = this.getHashes();
    //    if (null == hashes)
    //      this.hashCode();
    if (hashes == otherHashes)
      return true;
    lpi = 
    for(int i = 0; i < myHashes.length; i++) {
      long myVal = ((long) myHashes[i]) & ~lodMask;
      long otherval = ((long) otherHashes[i]) & ~lodMask;
      if (myVal != otherval)
        return false;
    };
    return true;
  }
   */
  
  
  @Override
  public String toString() {
    String x = "{";
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    while(it.hasNext()) {
      long key = it.nextLong();
      int index = sparseHashKeys.get(key);
      x += "(" + index + "," + sparseHashes[index] + "),";
    }
    return x + ": LOD=" + lod + "}";
  }
  
  @Override
  public int getNumEntries() {
    return sparseHashes.length;
  }
  
  @Override
  public int getDimensions() {
    return dimensions;
  }

  @Override
  public boolean contains(int index) {
    boolean found = sparseHashKeys.containsKey(index);
    return found;
  }

   @Override
  public Integer getValue(int index) {
    Integer value = sparseHashKeys.get(index);
    return value;
  }

//  @Override
//  public void setBits(Hash other, BitSet bs) {
//    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
//    while (it.hasNext()) {
//      int index = (int) it.nextLong();
//      bs.set(index);
//    }
//  }

//  @Override
//  public void setBits(Hash other, FastIDSet fs) {
//    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
//    while (it.hasNext()) {
//      int index = (int) it.nextLong();
//      Integer value = other.getValue(index);
//      if (null == value || ((value & ~lodMask) != (getValue(index) & ~lodMask)))
//          continue;
//      fs.add(index);
//    }
//  }
  
}

