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
  int lod = -1;
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
    setIndexes();
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
    for(int i = 0; i < index; i++) {
      sparseHashes[i] = values.get(i);
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
        sum += (((long) hashes[i]) & ~lodMask) * (i + 1) * getDimensions();
        index++;
      } else
        skip++;
    }
    super.indexes = sum;
  }
  
  private void setIndexes() {
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    long sum = 0;
    int dim = getDimensions();
    while(it.hasNext()) {
      long index = it.nextLong();
      Integer i = sparseHashKeys.get(index);
      i.hashCode();
      long value = (((long) sparseHashes[(int) i]) & ~lodMask) * (index + 1) * dim;
      sum += value;
    }
    this.indexes = sum;
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
  
}

