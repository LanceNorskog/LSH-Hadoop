/*package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;



 * Quantize an N-dimensional vector using either rectangles or triangles
 * 
 * A simplex has a base hash and a set of bits describing the surrounding dimensions
 * This creates the base hash. SimplexIterator handles the surrounding points.
 

//  Need to hash and unhash values one at a time. Need to support sparse vectors.
//  Need to split this:
//  1) prepare stretched musum
//  2) project individual values

public abstract class Hasher {
	// project point to lower corner
	public abstract void hashDense(double[] values, int[] hashed);
	// project from lower corner back to point
	public abstract void unhashDense(int[] hash, double[] p);
}
*/