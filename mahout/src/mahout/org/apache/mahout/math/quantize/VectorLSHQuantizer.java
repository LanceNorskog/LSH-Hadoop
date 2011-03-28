/**
 * 
 */
package org.apache.mahout.math.quantize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lsh.mahout.core.Hasher;
import lsh.mahout.core.OrthonormalHasher;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;

/**
 * Tyler Neylon's LSH quantizer and neighbor iteration algorithm.
 * http://www.siam.org/proceedings/soda/2010/SODA10_094_neylont.pdf
 *
 * Iteration algorithm slices N-dimensions space into hyper-tetrahedra
 * in of hypercubes. 
 * 
 * Each hypercube has N ^ D neighboring points, 
 * where N is the length of one side of the enclosing grid. 
 * Iterating  * (N + 1)
 *
 * Each simplex has D * (N + 1) neighbors.
 * 
 * Only implements DenseVector
 */


public class VectorLSHQuantizer extends Quantizer<Vector> {
  static PairComparator sorter = new PairComparator();
  static double EPSILON = 0.0000001;

  Hasher hasher = new OrthonormalHasher(); // until fully debugged!


  
  public VectorLSHQuantizer() {
    ;
  }

  @Override
  public Vector quantize(Vector v) {
    Vector q = new DenseVector(v.size());
    double[] values = new double[v.size()];
    double[] hashed = new double[v.size()];
    for(int index = 0; index < q.size(); index++) {
      values[index] = v.get(index);
    }
    hasher.project(values, hashed);
    Vector out = new DenseVector(hashed);
    return out;
  }  
  
  @Override 
  public Iterator<Vector> getNearest(Vector v, Double factor) {
    List<Vector> nabes = proximity_hashes(v);
    
    return nabes.iterator();
  }

  private List<Vector> proximity_hashes(Vector v) {
    Vector this_hash = quantize(v);
    List<Vector> hashes = new ArrayList<Vector>();
    add_hash(hashes, this_hash);
    List<Integer> sorted_coords = sort_as_perm(this_hash.minus(v));
    int dimensions = v.size();
    for(int index = 0; index < dimensions; index++) {
      double q = this_hash.get(sorted_coords.get(index));
      this_hash.set(sorted_coords.get(index), q + 1.0);
      add_hash(hashes, this_hash);
    }
    return hashes;
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

  private void add_hash(List<Vector> hashes, Vector hash) {
    Vector copy = hash.like();
    hashes.add(copy);
  }

  static public void main(String[] args) {
    VectorLSHQuantizer vlq = new VectorLSHQuantizer();
    double[] v1data = {1.2, 2.9};
    Vector v1 = new DenseVector(v1data);
    
    Vector vq1 = vlq.quantize(v1);
  
    Iterator<Vector> nabes = vlq.getNearest(v1, null);
  
    System.out.println("Neighbors of: " + v1.toString());
    
    while(nabes.hasNext()) {
      Vector nabe = nabes.next();
      System.out.println("\t" + nabe);
      
    }
  
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

