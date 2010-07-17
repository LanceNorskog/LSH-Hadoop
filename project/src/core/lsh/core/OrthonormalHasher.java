package lsh.core;

import java.util.Arrays;

/*
 * Orthonormal projection
 * 
 * Hash point to nearest grid corner.
 * stretch of 0.5 means grid to 0.5 instead of 1.0
 */

public class OrthonormalHasher implements Hasher {
	final double[] stretch;
	
	public OrthonormalHasher(int dim, double stretch) {
		this.stretch = new double[dim];
		for(int i = 0; i < dim; i++)
			this.stretch[i] = stretch;
	}

	public OrthonormalHasher(double stretch[]) {
		this.stretch = stretch;
	}

	@Override
	public int[] hash(double[] values) {
		int[] hashed = new int[values.length];
		for(int i = 0; i < hashed.length; i++) {
			hashed[i] = (int) Math.floor(values[i] / stretch[i]);
		}
		System.out.println("Hash:\t("+ values[0]+ "," + values[1]);
		System.out.println("\t->:\t("+ hashed[0]+ "," + hashed[1]);
		return hashed;
	}

	@Override
	public int[] hashUp(double[] values) {
		double[] up = Arrays.copyOf(values, values.length);
		for(int i = 0; i < values.length; i++) {
			up[i] += stretch[i]/0.51;
		}
		System.out.print("Up");
		return hash(up);
	}
	
	@Override
	public void unhash(int[] hash, double[] values) {
		for (int i = 0; i < hash.length; i++) {
			values[i] = hash[i] * stretch[i];
		}
	}

	@Override
	public int add(int hash, int c) {
		return hash + c;
	}


}
