package org.apache.mahout.randomvectorspace;

import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

/*
 * Contain a bitmask. Encodes an LSH mask. Supports sparse and dense masks.
 */

public class RVS {
  final int dims;
  final int size;
  final FastIDSet bitSet;
  
  public RVS(int dims, int size) {
    this.dims = dims;
    this.size = size;
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
    int count = 0;//bitSet.intersectionSize(other.bitSet);
    LongPrimitiveIterator iter = bitSet.iterator();
    LongPrimitiveIterator otherIter = bitSet.iterator();
    Long me = null;
    Long him = null;
    while(iter.hasNext() && otherIter.hasNext()) {
      if (null == me)
        me = iter.next();
      if (null == him)
        him = otherIter.next();
      if (me >= dims || him >= dims)
        throw new ArrayIndexOutOfBoundsException((int) Math.max(me, him)); 
      if (me == him) {
        count++;
        me = null;
        him = null;
      } else if (me > him) {
        him = null;
      } else {
        me = null;
      }
    }
    return dims - count;
  }
  
  @Override
  public String toString() {
    return bitSet.toString();
  }
}
