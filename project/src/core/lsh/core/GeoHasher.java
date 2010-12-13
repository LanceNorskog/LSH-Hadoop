package lsh.core;

/*
 * 2D spherical projection for Spatial work
 */

public class GeoHasher implements Hasher {
    double UPPER = 180.0;
    double LOWER = -180.0;
    double CIRCLE = 360.0;
    double[] stretch;

    public GeoHasher(int dim, double stretch) {
	this.stretch = new double[dim];
	for(int i = 0; i < dim; i++)
	    this.stretch[i] = stretch;
    }

    public GeoHasher(double stretch[]) {
	this.stretch = stretch;
    }

    public GeoHasher() {
    }

    @Override
    public void setStretch(double[] stretch) {
	this.stretch = stretch;
    }

    @Override
    public int[] hash(double[] values) {
	int[] hashed = new int[values.length];
	for(int i = 0; i < hashed.length; i++) {
	    double d = values[i];
	    while (d > UPPER) {
		d -= CIRCLE;
	    }
	    while (d < LOWER) {
		d += CIRCLE;
	    }
	    hashed[i] = (int) Math.floor(d / stretch[i]);
	}
	return hashed;
    }

    @Override
    public void project(double[] values, double[] gp) {
	for(int i = 0; i < values.length; i++)
	    gp[i] = values[i] / stretch[i];
    }

    @Override
    public void unhash(int[] hash, double[] values) {
	for (int i = 0; i < hash.length; i++) {
	    values[i] = hash[i] * stretch[i];
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("Orthonormal Hasher: dim (" + stretch.length + ") stretch = [" + stretch[0]);
	for(int i = 1; i < stretch.length; i++) {
	    sb.append(',');
	    sb.append(stretch[i]);
	}
	sb.append("]");
	return sb.toString();
    }

}
