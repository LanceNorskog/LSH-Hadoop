package lsh.core;

import java.util.Arrays;

/*
 * Vertex Transitive projection
 * 
 * Space-filling triangular simplices.
 */

public class VertexTransitiveHasher implements Hasher {
	public double[] stretch;
	private int dim;
	static final double S3 = Math.sqrt(3.0d);
	static final double MU = (1.0d - (1.0d/Math.sqrt(3.0d)))/2.0d;
	
	public VertexTransitiveHasher() {
	}
	
	public VertexTransitiveHasher(int dim, double stretch) {
		this.dim = dim;
		this.stretch = new double[dim];
		for(int i = 0; i < dim; i++) {
			this.stretch[i] = stretch;
		}
	}

	public VertexTransitiveHasher(double stretch[]) {
		this.dim = stretch.length;
		this.stretch = stretch;
	}
	
	@Override
	public void setStretch(double[] stretch) {
		this.dim = stretch.length;
		this.stretch = stretch;
	}

	@Override
	public int[] hash(double[] values) {
		double[] projected = new double[values.length];
		project(values, projected);
		int[] hashed = new int[values.length];
		for(int i = 0; i < projected.length; i++) {
			hashed[i] = (int) (projected[i]);
		}
//		System.out.println("Hash:\t("+ values[0]+ "," + values[1]);
//		System.out.println("\t->:\t("+ hashed[0]+ "," + hashed[1]);
		return hashed;
	}

	public void project(double[] values, double[] gp) {
		double sum = 0.0d;
		for(int i = 0; i < gp.length; i++) {
			gp[i] = values[i] / stretch[i];
			sum += gp[i];
		}
		double musum = MU * sum;
		for(int i = 0; i < gp.length; i++) {
			gp[i] = (gp[i] / S3 + musum);
		}
	}

	// TODO: THIS IS WRONG! get tyler to tell me how to do it right
	@Override
	public void unhash(int[] hash, double[] values) {
		double sum = 0.0;
		for(int i = 0; i < hash.length; i++) {
			sum += hash[i];
		}
		sum = sum / (1.0 / S3 + MU * hash.length); 
		for(int i = 0; i < hash.length; i++) {
			values[i] = S3 * (hash[i] -  MU * sum);
		}
//		double sum = 0;
//		for(int i = 0; i < hash.length; i++) {
//			values[i] = hash[i];
//			sum += values[i];
//		}	
//		double musum = MU * sum;
//		for (int i = 0; i < hash.length; i++) {
//			values[i] = S3 * (values[i] - musum);
//			values[i] *= stretch[i];
//		}
	}

	static public void main(String[] args) {
		VertexTransitiveHasher vth = new VertexTransitiveHasher(3, 1.0);
		double[][] orig = fillOrig();
		for(int i = 0; i < 4; i++) {
			double[] o = orig[i].clone();
			int[] corners = vth.hash(o);
			double[] unhash = new double[3];
			vth.unhash(corners, unhash);
			System.out.println("hashes: ");
			for(int j = 0; j < 3; j++) {
				System.out.println("\t" + orig[i][j] + ", " + corners[j] + ", " + unhash[j]);
			}
		}
			
			
	}

	private static double[][] fillOrig() {
		double[][] values = new double[4][3];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++)
				values[i][j] = i*4 + j;
		}
		return values;
	}
	
}
