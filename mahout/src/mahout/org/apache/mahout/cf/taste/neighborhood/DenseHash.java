package org.apache.mahout.cf.taste.neighborhood;

/*
 * Simplex box, keyed by "lower-left" corner
 * Includes Level Of Detail and generic payload
 * identity functions use corner and level-of-detail
 * Probably LOD will be managed outside.
 */

public class DenseHash<T> extends Hash<T> {
  final int[] hashes;
  int lod;
  
  private int lodMask;
  final T payload;
  int code = 0;
  
  public DenseHash(int[] hashes, T payload) { 
    this(hashes, 0, payload);
  }
  
  public DenseHash(int[] hashes, int lod, T payload) {
    this.hashes = hashes; // duplicate(hashes);
    setLOD(lod);
    this.payload = payload;
   }
  
  private int[] duplicate(int[] hashes2) {
    int[] dup = new int[hashes2.length];
    for(int i = 0; i < hashes2.length; i++) {
      dup[i] = hashes2[i];
    }
    return dup;
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
    return hashes;
  }
  
  public T getPayload() {
    return payload;
  }
  
  @Override
  public int hashCode() {
//    if (this.code == 0) {
      int code = 0;
      for(int i = 0; i < hashes.length; i++) {
        int val = hashes[i] & ~lodMask;
        code += ((hashes[i] & ~lodMask) + i) * i;
      }
      code += lod * 13 * hashes.length;
      this.code = code;
      //      System.out.println(code);
//    }
    
    return code;
  }
  
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
    for(int i = 0; i < hashes.length; i++) {
      if ((hashes[i] & ~lodMask) != (otherHashes[i] & ~lodMask))
        return false;
    };
    return true;
  }
  
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
  protected Object clone() {
    int[] v = new int[hashes.length];
    for(int i = 0; i < hashes.length; i++) {
      v[i] = hashes[i];
    }
    return new DenseHash<T>(v, lod, payload);
  }
  
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