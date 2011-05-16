package org.apache.mahout.randomvectorspace;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;

/*
 * Contain a bitmask. Encodes an LSH mask. Supports sparse and dense masks.
 */

public class RVS {
  final FastIDSet bitSet;
  
  public RVS(int size) {
    bitSet = new FastIDSet(size);
  }
  
  public void setBit(int index) {
    bitSet.add(index);
  }
  
  public void clearBit(int index) {
    bitSet.remove(index);
  }
  
  public void getBit(int index) {
    bitSet.add(index);
  }
  
  int hamming(RVS other) {
    int delta = bitSet.intersectionSize(other.bitSet);
    return delta;
  }
  
  @Override
  public String toString() {
    return bitSet.toString();
  }
}
