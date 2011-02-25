package lsh.mahout.core;

/*
 * Quantize an N-dimensional vector using either rectangles or triangles
 */

public interface Hasher {
	// set grid size with precision
	public void setStretch(double[] stretch);
	// project point to lower corner
	public int[] hash(double[] values);
	// project point to grid space
	public void project(double[] values, double[] gp);
	// project from corner to point
	public void unhash(int[] hash, double[] p);
}
