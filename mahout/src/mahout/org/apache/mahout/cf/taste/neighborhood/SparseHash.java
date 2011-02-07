package org.apache.mahout.cf.taste.neighborhood;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail and generic payload
 * identity functions use corner and level-of-detail
 * Probably LOD will be managed outside.
 */

public class SparseHash<T> extends Hash<T> {
//  final int[] hashes;
  FastByIDMap<Integer> sparseHashKeys;
  int[] sparseHashes;
  int lod;
  private int lodMask;
  final T payload;
  int code = 0;
  int dimensions;
  
  public SparseHash(int[] hashes, T payload) { 
    this(hashes, 0, payload);
  }
  
  public SparseHash(int[] hashes, int lod, T payload) {
    setHashes(hashes);
    setLOD(lod);
    this.payload = payload;
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
    int last = 0;
    for(int i = 0; i < dimensions; i++) {
      if (hashes[i] != 0) {
        sparseHashKeys.put(i, new Integer(index));
        sparseHashes[index] = hashes[i];
        index++;
        last = i;
      } else
        skip++;
    }
    hashCode();
  }

  public int getLOD() {
    return lod;
  }
  
  public void setLOD(int lod) {
    this.lod = lod;
    int mask = 0;
    int x = 0;
    while(x < lod) {
      mask |= (1 << x);
      x++;
    }
    this.lodMask = mask;
  }
  
  public int[] getHashes() {
    int[] hashes = new int[dimensions];
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    while(it.hasNext()) {
      long x = it.next();
      int index = sparseHashKeys.get(x);
      hashes[(int) x] = sparseHashes[index];
    }
    return hashes;
  }
  
  public T getPayload() {
    return payload;
  }
  
  @Override
  public int hashCode() {
    if (this.code == 0) {
      int code = 0;
      for(int i = 0; i < sparseHashes.length; i++) {
        code += sparseHashes[i] * i;
      }
      code += lod * 13 * sparseHashes.length;
//      if (null != payload)
//        code ^= payload.hashCode();
      this.code = code;
    }
    
    return code;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    SparseHash<T> other = (SparseHash<T>) obj;
    if (lod != other.lod)
      return false;
    if (code > 0 && other.code > 0 && code != other.code)
      return false;
    if ((null == payload && null != other.payload) || (null != payload && null == other.payload))
      return false;
    if (null != payload && !payload.equals(other.payload))
      return false;
    if (sparseHashes.length != other.sparseHashes.length)
      return false;
    for(int i = 0; i < sparseHashes.length; i++) 
      if ((sparseHashes[i] & ~lodMask) != (other.sparseHashes[i] & ~lodMask))
        return false;
    if (!sparseHashKeys.equals(other.sparseHashKeys))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    String x = "{";
    x += sparseHashKeys.toString() + "->";
    for(int i = 0; i < sparseHashes.length; i++) {
      x = x + (sparseHashes[i] & ~lodMask) + ",";
    }
    return x + "}";
  }
  
}

