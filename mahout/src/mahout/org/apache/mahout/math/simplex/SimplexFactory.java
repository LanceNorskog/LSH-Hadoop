package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.math.Vector;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 * 
 * A simplex has a base hash and a set of bits describing the surrounding dimensions
 * This creates the base hash. SimplexIterator handles the surrounding points.
 */

//  To support sparse vectors, VertexTransitive has to make two passes:
//  1) prepare stretched musum
//  2) project individual values

public abstract class SimplexFactory {
  static PairComparator sorter = new PairComparator();
  
  // project point to lower corner
  public abstract Simplex<?> hash(Vector v);
  // project from corner to point
  public abstract Vector unhash(Simplex<?> s);
}

