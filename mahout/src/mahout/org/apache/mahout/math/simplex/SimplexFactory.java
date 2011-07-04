package org.apache.mahout.math.simplex;

import org.apache.mahout.math.Vector;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 * 
 * A simplex has a base hash and a set of bits describing the surrounding dimensions
 * This creates the base hash. SimplexIterator handles the surrounding points.
 */

public abstract class SimplexFactory<T> {
//  static PairComparator sorter = new PairComparator();
  
  // project point to "lower" corner
  public abstract Simplex<T> hash(Vector v);
}

