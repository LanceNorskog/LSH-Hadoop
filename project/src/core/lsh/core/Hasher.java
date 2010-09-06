package lsh.core;

/*
 * return a set of dimension d array indexes given a point.
 */

public interface Hasher {
	public void setStretch(double[] stretch);
	// project point to lower corner
	public int[] hash(double[] values);
	// project point to grid space
	public void project(double[] values, double[] gp);
	// project from corner to point
	public void unhash(int[] hash, double[] p);
}
