package lsh.mahout.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;


/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 */

//  Need to hash and unhash values one at a time. Need to support sparse vectors.
//  Need to split this:
//  1) prepare stretched musum
//  2) project individual values

public abstract class Hasher {
  static PairComparator sorter = new PairComparator();

	// set grid size with precision
	public abstract void setStretch(double[] stretch);
	// project point to lower corner
	public abstract void hash(double[] values, int[] hashed);
	// project from corner to point
	public abstract void unhash(int[] hash, double[] p);
	// iterate grid points
	public Iterator<int[]> iter(double[] values) {
    List<int[]> nabes = proximity_hashes(values);
    return nabes.iterator();
	}
	
  private List<int[]> proximity_hashes(double[] values) {
    int dimensions = values.length;
    int[] this_hash = new int[dimensions]; 
    hash(values, this_hash);
    List<int[]> hashes = new ArrayList<int[]>();
    add_hash(hashes, this_hash);
    List<Integer> sorted_coords = sort_as_perm(subtract(values, this_hash));
    for(int index = 0; index < dimensions; index++) {
      Integer inner = sorted_coords.get(index);
      int q = this_hash[inner];
      this_hash[inner] = q + 1;
      add_hash(hashes, this_hash);
    }
    return hashes;
  }

  private double[] subtract(double[] values, int[] this_hash) {
    int dimensions = values.length;
    double[] copy = Arrays.copyOf(values, dimensions);
    for(int i = 0; i < this_hash.length; i++) {
      copy[i] =  values[i] - this_hash[i];
    }
    return copy;
  }

  // return indexes of values sorted in reverse
  private List<Integer> sort_as_perm(double[] values) {
    List<Pair> pairs = new ArrayList<Pair>();
    int dimensions = values.length;
    int index = 0;
    for(int i = 0; i < dimensions; i++) {
      Pair p = new Pair(i, values[i]);
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

