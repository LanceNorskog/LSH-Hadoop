package lsh.mahout.core;

import java.util.Iterator;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

/*
 * Store a bit for whether a particular hash exists,
 * or a thing that gives quick lookup of neighbors.
 * 
 * For a hash, store 2 bits for each dimension for "+- exists".
 * 
 *  * NO! Given the unique long value trick, just have a map of Long->Long[+-n, long hash, ...]
 *  No LOD

 */
// dense first
public class SparseHashNeighbors {
  final int dimensions;
  // Ortho has 8 neighboring directions, Simplex has six
  final int directions;
  
  // map of neighbor lists, keyed by index + direction
  final FastByIDMap<NeighborList> neighborMap;
  // list of index + direction, keyed by Hash uniqueSum
  final FastByIDMap<Long> neighborSums;
  
  public SparseHashNeighbors(int dimensions, int directions, int size) {
    this.dimensions = dimensions;
    this.directions = directions;
    if (size == 0)
      size = 200;
    neighborMap = new FastByIDMap<NeighborList>(size);
    neighborSums = new FastByIDMap<Long>(size);
  }
  
  public NeighborList addHash(Hash h) {
    long uniqueSum = h.getUniqueSum();
    NeighborList nb = neighborMap.get(uniqueSum);
    if (null == nb) {
      nb = new NeighborList();
      neighborMap.put(uniqueSum, nb);
    }
    Iterator<Integer> it = h.iterator();
    while(it.hasNext()) {
      nb.setHash(dimensions, directions, h);
    }
    return nb;
  }
  
  public long[] getNeighborSums(Hash h, int position) {
    long[] uniqueSums = new long[directions];
    for(int d = 0; d < directions; d++) {
      uniqueSums[d] = Integer.MAX_VALUE;
//      Long index neighborSums.
      NeighborList nb = neighborMap.get(hashIndex(d, d));
      if (null != nb) {
        boolean b = nb.getNeighborExists(position, d);
        if (b) {
          //          long key = 
          //          uniqueSums[d] = 
        }
      }
      
    }
    return uniqueSums;
  }
  
  long hashIndex(int index, int direction) {
    return index * directions + direction;
  }
  
  /*
   * List of neighbors for one hash.
   * Sparse
   */
  class NeighborList {
    final FastByIDMap<Long> neighbors;
    
    public NeighborList() {
      this.neighbors = new FastByIDMap<Long>(dimensions);
    }
    
    boolean getNeighborExists(int index, int direction) {
      return neighbors.containsKey(hashIndex(index, direction));
    }
    
    Long getNeighborSum(int index, int direction) {
      Long l = neighbors.get(hashIndex(index, direction));
      return l;
    }
    
    boolean setHash(int position, int direction, Hash h) {
      long index = hashIndex(position, direction);
      Long hashKey = neighbors.get(index);
      boolean found = true;
      if (null != hashKey) {
        neighbors.put(index, hashKey);
        found = false;
      }
      
      return found;
    } 
    
  }
  
}