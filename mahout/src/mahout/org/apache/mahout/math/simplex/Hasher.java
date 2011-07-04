
package org.apache.mahout.math.simplex;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 * 
 * A simplex has a base hash and a set of bits describing the surrounding dimensions
 * This creates the base hash. SimplexIterator handles the surrounding points.
 
*/

public abstract class Hasher {
  // magical per-vector value- VertexTransitive needs a sum of all values
  public abstract Double getFactor(double[] values);
	// project point to lower corner
	public abstract void hashDense(double[] values, int[] hashed, Double factor);
	// project from lower corner back to point
	public abstract void unhashDense(int[] hash, double[] p, Double factor);
}
