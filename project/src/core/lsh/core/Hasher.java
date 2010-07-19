package lsh.core;

/*
 * return a set of dimension d array indexes given a point.
 */

public interface Hasher {
	// project point to lower corner
	public int[] hash(double[] values);
	// project point to upper corner
	public int[] hashUp(double[] values);
	// project from corner to point
	public void unhash(int[] hash, double[] p);
	// increment hash code
	public int add(int hash, int c);
}
