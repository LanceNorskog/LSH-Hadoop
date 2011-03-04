package lsh.mahout.core;

import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;

/*
 * Dense implementation of N-dimensional hash
 * Supports mutability
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
    return x + hashes[hashes.length - 1] + "}, LOD:" + getLOD() + ",code=" + getUniqueSum() +"}";
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

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Integer> iterator() {
    ArrayIterator ait = new ArrayIterator(hashes);
    // magic!
    return (Iterator<Integer>) ait;
  }

}
