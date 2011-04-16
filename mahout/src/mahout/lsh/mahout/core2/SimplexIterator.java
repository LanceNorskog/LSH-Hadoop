/**
 * 
 */
package lsh.mahout.core2;

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
 * Given a Vector, iterate the containing hash points.
 * Given D dimensions, there are D+1 containing points
 * 
 * Dense for now
 */


public class SimplexIterator implements Iterator<Simplex> {
  static PairComparator sorter = new PairComparator();
  final Hasher hasher;
  final int dimensions;
  Vector v;
  List<int[]> nabes;
  int index = 0;  
  
  public SimplexIterator(Hasher hasher, Vector v) {
    this.hasher = hasher;
    dimensions = v.size();
    nabes = proximity_hashes(v);
    this.v = v;
  }

  @Override
  public boolean hasNext() {
    if (null == nabes) {
      nabes = proximity_hashes(v);
      v = null;
    }
    return index <= dimensions;
  }

  @Override
  public Simplex next() {
    if (! hasNext())
      throw new IllegalStateException("No next vector");
    int[] hash = nabes.get(index);
    index++;
   return new Simplex(hash);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  /*
   * Return points in integer grid space.
   * Need entire vector for VertexTransitive hashing
   */
  public int[] getHash(Vector v) {
    double[] values = new double[v.size()];
    int[] hashed = new int[v.size()];
    for(int index = 0; index < v.size(); index++) {
      values[index] = v.get(index);
    }
    hasher.hash(values, hashed);
    return hashed;
  }  
  
  // build full list of neighbors
  // this code is a transcription of python code
  // needs refactoring for good Java style
  // and for iterative generation instead of prebuilt list

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
    List<Pair> pairs = new ArrayList<Pair>();
    int index = 0;
    while(vIt.hasNext()) {
      Element e = vIt.next();
      Pair p = new Pair(index, e.get());
      pairs.add(p);
      index++;
    }
    // sort by value highest to lowest, preserving order of indexes
    Collections.sort(pairs, sorter);
    List<Integer> indexes = new ArrayList<Integer>();
    for(Pair p: pairs) {
      indexes.add(new Integer(p.index));
    }
    return indexes;
  }

  private void add_hash(List<int[]> hashes, int[] this_hash) {
    int[] copy = Arrays.copyOf(this_hash, this_hash.length);
    hashes.add(copy);
  }

 }

class Pair {
  final int index;
  final double value;
  
  public Pair(int index, double value) {
    this.index = index;
    this.value = value;
  }
}

class PairComparator implements Comparator<Pair> {
  
  // sorts from highest to lowest

  @Override
  public int compare(Pair a, Pair b) {
    if (a.value < b.value)
      return 1;
    else if (a.value > b.value)
      return -1;
    else
      return 0;
  }
}

