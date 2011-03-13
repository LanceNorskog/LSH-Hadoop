package lsh.mahout.core;

/*
 * Vertex Transitive projection
 * 
 * Space-filling triangular simplices.
 */

public class VertexTransitiveHasher implements Hasher {
    public double[] stretch;
    final int dimensions;
    static final double S3 = Math.sqrt(3.0d);
    static final double MU = (1.0d - (1.0d/Math.sqrt(3.0d)))/2.0d;

    public VertexTransitiveHasher() {
	dimensions = 0;
    }

    public VertexTransitiveHasher(int dim, Double stretch) {
	dimensions = dim;
	if (null != stretch) {
	    this.stretch = new double[dim];
	    for(int i = 0; i < dim; i++) {
		this.stretch[i] = stretch;
	    }
	}
    }

    public VertexTransitiveHasher(double stretch[]) {
	dimensions = stretch.length;
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
	return hashed;
    }

    @Override
    public void project(double[] values, double[] gp) {
	double sum = 0.0d;
	for(int i = 0; i < gp.length; i++) {
	    if (null != stretch) {
		gp[i] = values[i] / stretch[i];
	    } else {
		gp[i] = values[i];		
	    }
	    sum += gp[i];
	}
	double musum = MU * sum;
	for(int i = 0; i < gp.length; i++) {
	    gp[i] = (gp[i] / S3 + musum);
	}
    }

    @Override
    public void unhash(int[] hash, double[] values) {
	double sum = 0.0;
	for(int i = 0; i < hash.length; i++) {
	    sum += hash[i];
	}
	sum = sum / (1.0 / S3 + MU * hash.length); 
	for(int i = 0; i < hash.length; i++) {
	    if (null != stretch) {
		values[i] = S3 * (hash[i] -  MU * sum * stretch[i]);
	    } else {
		values[i] = S3 * (hash[i] -  MU * sum);
	    }
	}
    }
    
    @Override
    public String toString() {
	return null;
    }

    static int size = 100;

    static public void main(String[] args) {
	VertexTransitiveHasher vth = new VertexTransitiveHasher(size, 0.02);
	double[][] orig = fillOrig();
	for(int i = 0; i < 4; i++) {
	    double[] o = orig[i].clone();
	    int[] corners = vth.hash(o);
	    double[] unhash = new double[size];
	    vth.unhash(corners, unhash);
	    System.out.println("hashes: ");
	    for(int j = 0; j < size; j++) {
		System.out.println("\t" + orig[i][j] + ", " + corners[j] + ", " + unhash[j]);
	    }
	}


    }

    private static double[][] fillOrig() {
	double[][] values = new double[4][size];
	double d = size;
	for(int i = 0; i < 4; i++) {
	    for(int j = 0; j < size; j++)
		values[i][j] = (i*d + j + 1)/100.0;
	}
	return values;
    }

}
