package org.apache.mahout.cf.taste.neighborhood;

import java.util.Comparator;

class Hash implements Comparable<Hash>, Cloneable{
  final int[] hashes;
  final int lod;
  int code = 0;

  public Hash(int[] hashes) {
    this.hashes = hashes;
    this.lod = 0;
  }

  public Hash(int[] hashes, int lod) {
    this.hashes = hashes;
    this.lod = lod;
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
      this.code = code;
    }

    return code;
  }

  @Override
  public boolean equals(Object obj) {
    Hash other = (Hash) obj;
    return compareTo(other) == 0;
  }

  // sort by coordinates in order
  @Override
  public int compareTo(Hash o) {
    for(int i = 0; i < hashes.length; i++) {
      if (hashes[i] < o.hashes[i])
        return 1;
      else if (hashes[i] > o.hashes[i])
        return -1;
    };
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
    return new Hash(v);
  }

}

/* only compare values at index */
class HashSingleComparator implements Comparator<Hash>{
  final int index;

  public HashSingleComparator(int index) {
    this.index = index;
  }

  @Override
  public int compare(Hash o1, Hash o2) {
    if (o1.hashes[index] < o2.hashes[index])
      return 1;
    else if (o1.hashes[index] > o2.hashes[index])
      return -1;
    else
      return 0;
  }


}