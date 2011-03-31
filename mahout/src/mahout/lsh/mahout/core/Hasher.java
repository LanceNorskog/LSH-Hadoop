package lsh.mahout.core;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 */

public abstract class Hasher {
	// set grid size with precision
	public abstract void setStretch(double[] stretch);
	// project point to lower corner
	public abstract void hash(double[] values, int[] hashed);
	// project from corner to point
	public abstract void unhash(int[] hash, double[] p);
}
