package org.apache.mahout.cf.taste.neighborhood;

import java.util.ArrayList;
import java.util.Arrays;
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
  // (external index -> local nonZero array)
  FastByIDMap<Integer> sparseHashKeys;
  // local array - THIS IS NOT SORTED!
  int[] sparseHashes;
  int dimensions;
  
  public SparseHash(int[] hashes) { 
    this(hashes, 0);
  }
  
  public SparseHash(int[] hashes, int lod) {
    setHashes(hashes);
    setLOD(lod);
  }
  
  public SparseHash(SparseHash sp, int lod) {
    this.sparseHashes = sp.sparseHashes;
    this.sparseHashKeys = sp.sparseHashKeys;
    setLOD(lod);
    this.dimensions = sp.dimensions;
    populateHashes();
  }
  
  public SparseHash(Hasher hasher, Iterator<Element> el, int dimensions, int lod) {
    setValues(hasher, el);
    this.dimensions = dimensions;
    setLOD(lod);
//    populateHashes();
  }
  
  private void setValues(Hasher hasher, Iterator<Element> el) {
    sparseHashKeys = new FastByIDMap<Integer>();
    List<Integer> keys = new ArrayList<Integer>(dimensions);
    List<Integer> values = new ArrayList<Integer>(dimensions);
    int nonZero = 0;
    double[] d = new double[1];
    int[] h = null;
    
    while(el.hasNext()) {
      Element e = el.next();
      sparseHashKeys.put(e.index(), new Integer(nonZero));
      keys.add(e.index());
      d[0] = e.get();
      h = hasher.hash(d);  
      values.add(h[0]);
      nonZero++;
    } 
    long sum = 0;
    sparseHashes = new int[nonZero];
    for(int index = 0; index < nonZero; index++) {
      sparseHashes[index] = values.get(index);
      long value = getSingleHash(keys.get(index), values.get(index));
      sum += value;
    }
    super.setIndexes(sum);
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
    long sum = 0;
    for(int i = 0; i < dimensions; i++) {
      if (hashes[i] != 0) {
        sparseHashKeys.put(i, new Integer(index));
        sparseHashes[index] = hashes[i];
        long value = getSingleHash(i, hashes[i]);
        sum += value;
        index++;
      } 
    }
    super.setIndexes(sum);
  }
  
  protected void populateHashes() {
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    long sum = 0;
    int dim = getDimensions();
    while(it.hasNext()) {
      long index = it.nextLong();
      Integer nonZero = sparseHashKeys.get(index);
      nonZero.hashCode();
      long value = getSingleHash((int) index, sparseHashes[(int) nonZero]);
      sum += value;
    }
    super.setIndexes(sum);
  }
  
  @Override
  public String toString() {
    String x = "{";
    LongPrimitiveIterator it = sparseHashKeys.keySetIterator();
    while(it.hasNext()) {
      long nonZero = it.nextLong();
      int index = sparseHashKeys.get(nonZero);
      x += "(" + nonZero + "," + sparseHashes[index] + "),";
    }
    return x + ": LOD=" + getLOD() + ",code=" + indexes + "}";
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
  public boolean containsValue(int index) {
    boolean found = sparseHashKeys.containsKey(index);
    return found;
  }
  
  @Override
  public Integer getValue(int index) {
    Integer nonZero = sparseHashKeys.get(index);
    if (null == nonZero)
      return null;
    int value = sparseHashes[nonZero];
    return value;
  }
  
  // have to handle adding new entry to array
  @Override
  public void setValue(int index, int hash) {
    int oldHash = 0;
    if (sparseHashKeys.containsKey(index)) {
      int nonZero = sparseHashKeys.get(index);
      oldHash = sparseHashes[nonZero];
      sparseHashes[nonZero] = hash;
    } else {
      int nonZero = sparseHashes.length;
      int[] newHashes = Arrays.copyOf(sparseHashes, nonZero + 1);
      newHashes[nonZero] = hash;
      sparseHashes = newHashes;
      sparseHashKeys.put(index, nonZero);
    }
    super.changeIndexes(index, oldHash, hash);
    
  }
  
  
  @Override
  public Iterator<Integer> iterator() {
    // TODO Auto-generated method stub
    return new SparseHashIterator(this);
  }
  
}

class SparseHashIterator implements Iterator<Integer> {
  final LongPrimitiveIterator it;
  final int[] sparseHashes;
  
  public SparseHashIterator(SparseHash sparseHash) {
    it = sparseHash.sparseHashKeys.keySetIterator();
    sparseHashes = sparseHash.sparseHashes;
  }
  
  @Override
  public boolean hasNext() {
    return it.hasNext();
  }
  
  @Override
  public Integer next() {
    long index = it.next();
    return (int) index;
  }
  
  @Override
  public void remove() {
    
  }
  
  //  public double value(int index) {
  //    return sparseHashes[index];
  //  }
  
}


