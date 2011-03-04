package lsh.mahout.core;

import java.util.Iterator;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

/*
 * Track connections to neighboring hashes.
 * Keeps Hash but should not need to.
 */
// dense first
public class DenseHashNeighbors {
  // length of Hash vector
  final int dimensions;
  // Ortho has 8 neighboring directions, Simplex has six
  final int directions; 
  // map of neighbor lists, keyed by index + direction
  // center is the first in the list
  final NeighborList[] neighborMap;
  // list of index + direction, keyed by Hash uniqueSum
//  final long[] neighborSums;
  
  public DenseHashNeighbors(int dimensions, int directions, int size) {
    this.dimensions = dimensions;
    this.directions = directions;
    if (size == 0)
      size = 200;
    neighborMap = new NeighborList[size * directions];
  }
  
  // cross-knit neighbor lists
  // yes, this is O(N*D)
  public void addHash(Hash h) {
    for(int p = 0; p < dimensions; p++) {
      NeighborList center = neighborMap[centerIndex(p)];
      if (null == center) {
        center = new NeighborList(this);
        neighborMap[centerIndex(p)] = center;
        //        for(int d = 0; d < directions; d++) {
        //          NeighborList backPointer = new NeighborList(this);
        //        }
      }
      for(int d = 0; d < directions; d++) {
        NeighborList nb = neighborMap[hashIndex(p, d)];
        if (null == nb) {
          continue;
        }
        // rotate halfway round for reverse pointer
        int rotated = ((d + directions/2) % directions) + 1;
        nb.setHash(p, rotated, h);
        Hash z = nb.getNeighbor(p, d);
        center.setHash(p, d, z);
      }
    }
  }
  
  public Hash[] getNeighbors(Hash h, int position) {
    Hash[] hashes = new Hash[directions];
    NeighborList nb = neighborMap[centerIndex(position)];
    for(int d = 0; d < directions; d++) {
      hashes[d] = nb.getNeighbor(position, d);
    }
    return hashes;
  }
  
  private int centerIndex(int position) {
    return position * (directions + 1);
  }
  
  private int hashIndex(int position, int direction) {
    return position * (directions + 1) + (direction + 1);
  }
}

/*
 * List of neighbors for one hash.
 * Dense
 */
class NeighborList {
  final DenseHashNeighbors dhn;
  final Hash[] neighbors;
  
  public NeighborList(DenseHashNeighbors dhn) {
    this.dhn = dhn;
    this.neighbors = new Hash[dhn.dimensions * dhn.directions];
  }
  
  Hash getNeighbor(int index, int direction) {
    Hash h = neighbors[neighborIndex(index, direction)];
    return h;
  }
  
  boolean setHash(int position, int direction, Hash h) {
    int index = neighborIndex(position, direction);
    Hash z = neighbors[index];
    neighbors[index] = h;
    return null != z;
  } 
  
  private int neighborIndex(int position, int direction) {
    return position * dhn.directions + direction;
  }
  
  @Override
  public String toString() {
    String x = "[";
    for(int p = 0; p < dhn.dimensions; p++) {
      boolean found = false;
      for (int d = dhn.directions; d < dhn.directions; d++) {
        if (null != neighbors[neighborIndex(p, d)])
          found = true;
      }
      if (found) {
        x += "{" + p + ":{";
        for (int d = dhn.directions; d < dhn.directions; d++) {
          if (null != neighbors[neighborIndex(p, d)]) {
            x += "[" + d + "]";
          }
        }
        
      }
    }
    return x + "]";
  }
  
}