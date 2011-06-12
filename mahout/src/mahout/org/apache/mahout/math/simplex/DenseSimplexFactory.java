package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 * 
 * A simplex has a base hash and a set of bits describing the surrounding dimensions
 * This creates the base hash. SimplexIterator handles the surrounding points.
 */

//  Need to hash and unhash values one at a time. Need to support sparse vectors.
//  Need to split this:
//  1) prepare stretched musum
//  2) project individual values

public class DenseSimplexFactory extends SimplexFactory {
  static PairComparator sorter = new PairComparator();
  final Hasher hasher;
  final Vector stretch;
  final double gridSize;
  
  public DenseSimplexFactory(Hasher hasher, double gridSize){
    this.hasher = hasher;
    this.stretch = null;
    this.gridSize = gridSize;
  }
  
  public DenseSimplexFactory(Hasher hasher, Vector stretch){
    this.hasher = hasher;
    this.stretch = stretch;
    this.gridSize = -1;
  }
  
  @Override
  public Simplex<?> hash(Vector v) {
    Object label = null;
    if (v instanceof NamedVector) {
      label = ((NamedVector) v).getName();
    }
    int size = v.size();
    double[] da = new double[size];
    int[] ia = new int[size];
    hasher.hashDense(da, ia);
    boolean[] ba = new boolean[size];
    Simplex<?> s = new Simplex<Object>(ia, ba, label);
    return s;
  }
  
  @Override
  public Vector unhash(Simplex<?> s) {
    // TODO Auto-generated method stub
    return null;
  }  
  
}
