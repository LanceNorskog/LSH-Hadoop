package org.apache.mahout.cf.taste.neighborhood;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.lucene.util.BitVector;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;

/*
 * Store a bit for whether a particular hash exists. 
 * Allow quick lookup of neighbors.
 * 
 * intersects[dimension] -> bitset of whether there is a hash at this dim x that dim.
 * 
 */

public class DenseHashIndex {
  int dimensions;
//  List<Integer> offsets;
  FastByIDMap<FastIDSet> intersects;
    
  public DenseHashIndex(int dimensions) {
    this.dimensions = dimensions;
//    offsets = new ArrayList<Integer>(dimensions);
    intersects = new FastByIDMap<FastIDSet>(dimensions);
  }
  
  public boolean checkAndSetHash(Hash h, Hash other) {
    boolean set = checkHash(h);
    if (set)
      return true;
    for(int i = 0; i < dimensions; i++) {
      FastIDSet bits = intersects.get(i);
      if (null == bits) {
        bits = new FastIDSet(dimensions);
        intersects.put(i, bits);
      }
      h.setBits(other, bits);
    }
    return false;
  }

  public boolean checkHash(Hash h) {
    // TODO Auto-generated method stub
    return false;
  }
}
