package lsh.core;

import java.util.Arrays;

/*
 * Vertex Transitive projection
 * 
 * Space-filling triangular simplices.
 */

public class VertexTransitiveHasher implements Hasher {
	double[] stretch;
	static final double S3 = Math.sqrt(3.0d);
	static final double MU = (1.0d - (1.0d/Math.sqrt(3.0d)))/2.0d;
	static final double SKEW = 0.8d;
	
	public VertexTransitiveHasher() {
	}
	
	public VertexTransitiveHasher(int dim, double stretch) {
		this.stretch = new double[dim];
		for(int i = 0; i < dim; i++) {
			this.stretch[i] = stretch;
		}
	}

	public VertexTransitiveHasher(double stretch[]) {
		this.stretch = stretch;
	}
	
	@Override
	public void setStretch(double[] stretch) {
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
		System.out.println("Hash:\t("+ values[0]+ "," + values[1]);
		System.out.println("\t->:\t("+ hashed[0]+ "," + hashed[1]);
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
			gp[i] = (gp[i] / S3 + musum)/ SKEW;
		}
	}

	/*
	@Override
	public int[] hashUp(double[] values) {
		double[] up = Arrays.copyOf(values, values.length);
		for(int i = 0; i < values.length; i++) {
			up[i] += stretch[i]/0.51;
		}
		System.out.print("Up");
		return hash(up);
	}	
	*/

	@Override
	public void unhash(int[] hash, double[] values) {
		double sum = 0;
		for(int i = 0; i < hash.length; i++) {
			values[i] = hash[i];
			sum += values[i];
		}	
		double musum = MU * sum;
		for (int i = 0; i < hash.length; i++) {
			values[i] = S3 * (values[i] - musum) * SKEW;
			values[i] *= stretch[i];
		}
	}

}
