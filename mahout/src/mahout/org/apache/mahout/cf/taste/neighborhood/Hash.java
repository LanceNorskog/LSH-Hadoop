package org.apache.mahout.cf.taste.neighborhood;

import java.util.Comparator;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail and generic payload
 * identity functions use corner and level-of-detail
 * Probably LOD will be managed outside.
 */

class Hash<T> implements Comparable<Hash<T>>, Cloneable{
  final int[] hashes;
  final int lod;
  final T payload;
  int code = 0;

  public Hash(int[] hashes, T payload) { 
    this(hashes, 0, payload);
  }

  public Hash(int[] hashes, int lod, T payload) {
    this.hashes = hashes;
    this.lod = lod;
    this.payload = payload;
  }

  public T getPayload() {
    return payload;
  }

  @Override
  public int hashCode() {
    if (this.code == 0) {
      int code = 0;
      for(int i = 0; i < hashes.length; i++) {
        Integer val = hashes[i];
        code += val.hashCode();
        code *= 13;
        code = code << 4;
      }
      code += lod * 13;
      this.code = code;
    }

    return code;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    Hash<T> other = (Hash<T>) obj;
    if (lod != other.lod)
      return false;
    if (code > 0 && other.code > 0 && code != other.code)
      return false;
    for(int i = 0; i < hashes.length; i++) {
      if (hashes[i] != other.hashes[i])
        return false;
    };
    return true;
  }

  // sort by coordinates in order
  @Override
  public int compareTo(Hash<T> o) {
    for(int i = 0; i < hashes.length; i++) {
      if (hashes[i] > o.hashes[i])
        return 1;
      else if (hashes[i] < o.hashes[i])
        return -1;
    };
    if (lod > o.lod)
      return 1;
    else if (lod < o.lod)
      return -1;
    else
      return 0;
  }

  @Override
  public String toString() {
    String x = "{";
    for(int i = 0; i < hashes.length; i++) {
      x = x + hashes[i] + ",";
    }
    return x + "}";
  }

  @Override
  protected Object clone() {
    int[] v = new int[hashes.length];
    for(int i = 0; i < hashes.length; i++) {
      v[i] = hashes[i];
    }
    return new Hash<T>(v, lod, payload);
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