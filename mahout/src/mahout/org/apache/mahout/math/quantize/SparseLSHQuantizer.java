/**
 * 
 */
package org.apache.mahout.math.quantize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lsh.mahout.core.Hasher;
import lsh.mahout.core.OrthonormalHasher;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

/**
 * Version for sparse vectors.
 * Must be iterable.
 * 
 * Very important: this does not work with VertexTransitive, only Orthonormal.
 * Orthonormal projects to the nearest floor, but VT does a sum on the whole vector.
 * It will need an Element iterator
 */

public class SparseLSHQuantizer extends Quantizer<Vector> {
  static SparsePairComparator sorter = new SparsePairComparator();

  final Hasher hasher;
  
  public SparseLSHQuantizer(Hasher hasher) {
    this.hasher = hasher;
  }

  @Override
  public Vector quantize(Vector v) {
    Vector q = v.like();
    Iterator<Element> it = v.iterateNonZero();
    int[] hashed = new int[1];
    double[] clipped = new double[1];
    while(it.hasNext()) {
      Element e = it.next();
      clipped[0] = e.get();
      hasher.hash(clipped, hashed);
      hasher.unhash(hashed, clipped);
      q.set(e.index(), clipped[0]);
    }
    return q;
  }  

  /*
   * Return points in integer grid space.
   */
  public int[] getHash(Vector v) {
    double[] values = new double[v.size()];
    for(int index = 0; index < v.size(); index++) {
      values[index] = v.get(index);
    }
    int[] hashed = new int[v.size()];
    hasher.hash(values, hashed);
    return hashed;
  }  
  
  @Override 
  public Iterator<Vector> getNearest(Vector v) {
    List<int[]> nabes = proximity_hashes(v);
    return new UnhashIterator(hasher, nabes);
  }

  private List<int[]> proximity_hashes(Vector v) {
    int[] this_hash = getHash(v);
    List<int[]> hashes = new ArrayList<int[]>();
    add_hash(hashes, this_hash);
    List<Integer> sorted_coords = sort_as_perm(subtract(v, this_hash));
    int dimensions = v.size();
    for(int index = 0; index < dimensions; index++) {
      Integer inner = sorted_coords.get(index);
      int q = this_hash[inner];
      this_hash[inner] = q + 1;
      add_hash(hashes, this_hash);
    }
    return hashes;
  }

  private Vector subtract(Vector v, int[] this_hash) {
    Vector out = v.like();
    for(int i = 0; i < this_hash.length; i++) {
      out.set(i, v.get(i) - this_hash[i]);
    }
    return out;
  }

  // return indexes of values sorted in reverse
  private List<Integer> sort_as_perm(Vector v) {
    Iterator<Element> vIt = v.iterator();
    List<SparsePair> pairs = new ArrayList<SparsePair>();
    int index = 0;
    while(vIt.hasNext()) {
      Element e = vIt.next();
      SparsePair p = new SparsePair(index, e.get());
      pairs.add(p);
      index++;
    }
    // sort by value highest to lowest, preserving order of indexes
    Collections.sort(pairs, sorter);
    List<Integer> indexes = new ArrayList<Integer>();
    for(SparsePair p: pairs) {
      indexes.add(new Integer(p.index));
    }
    return indexes;
  }

  private void add_hash(List<int[]> hashes, int[] this_hash) {
    int[] copy = Arrays.copyOf(this_hash, this_hash.length);
    hashes.add(copy);
  }

  static public void main(String[] args) {
    SparseLSHQuantizer vlq = new SparseLSHQuantizer(new OrthonormalHasher());
    double[] v1data = {1.2, 2.9};
    Vector v1 = new DenseVector(v1data);
    Vector q1 = vlq.quantize(v1);

    double[] v2data = {0.9, 2.2};
    Vector v2 = new DenseVector(v2data);
    Vector q2 = vlq.quantize(v2);
  
    Iterator<Vector> nabes = vlq.getNearest(v1);
  
    printVectors(v1, nabes);
    nabes = vlq.getNearest(v2);
    printVectors(v2, nabes);
    
    double[] v3data = {0.9, 2.2, 3.9};
    Vector v3d = new DenseVector(v3data);
    nabes = vlq.getNearest(v3d);
    printVectors(v3d, nabes);

  }

  private static void printVectors(Vector v1, Iterator<Vector> nabes) {
    System.out.println("Neighbors of: " + v1.toString());
    
    while(nabes.hasNext()) {
      Vector nabe = nabes.next();
      printVector(nabe);
    }
  }
  
  static void printVector(Vector v) {
    System.out.print("\t{" + v.size() + ":");
    for(int i = 0; i < v.size(); i++) {
      Double d = v.get(i);
      System.out.print(d.toString() + ",");
    }
    System.out.println("}");
  }

}

class SparsePair {
  final int index;
  final double value;
  
  public SparsePair(int index, double value) {
    this.index = index;
    this.value = value;
  }
}

class SparsePairComparator implements Comparator<SparsePair> {
  
  // sorts from highest to lowest

  @Override
  public int compare(SparsePair a, SparsePair b) {
    if (a.value < b.value)
      return 1;
    else if (a.value > b.value)
      return -1;
    else
      return 0;
  }
}

class SparseUnhashIterator implements Iterator<Vector> {
  final Hasher hasher;
  final List<int[]> nabes;
  final int dimensions;
  final int length;
  int index;
  
  SparseUnhashIterator(Hasher hasher, List<int[]> nabes) {
    this.hasher = hasher;
    this.nabes = nabes;
    this.dimensions = nabes.get(0).length;
    this.length = nabes.size();
    index = 0;
  }

  @Override
  public boolean hasNext() {
    return index < length;
  }

  @Override
  public Vector next() {
    if (index >= length)
      throw new IllegalStateException("No next vector");
    int[] hash = nabes.get(index);
    double unhashed[] = new double[dimensions];
    this.hasher.unhash(hash, unhashed);
    Vector v = new DenseVector(unhashed);
    index++;
    return v;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub
    
  }
  
}
