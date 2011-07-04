package org.apache.mahout.math.simplex;

/*
 * Vertex Transitive projection
 * 
 * Space-filling triangular simplices (hyper-tetrahedra).
 * Formula courtesy of Tyler Neylon.
 */

public class VertexTransitiveHasher extends Hasher {
  final int dimensions;
  final double gridsize;
  static final double S3 = Math.sqrt(3.0d);
  static final double MU = (1.0d - (1.0d/Math.sqrt(3.0d)))/2.0d;
  
  public VertexTransitiveHasher(int dimensions, Double gridsize) {
    this.dimensions = dimensions;
    this.gridsize = gridsize;
  }
  
  @Override
  public void hashDense(double[] values, int[] hashed, Double factor) {
    double[] projected = new double[values.length];
    project(values, projected, factor);
    for(int i = 0; i < projected.length; i++) {
      hashed[i] = (int) (projected[i]);
      // -0.5 rounds to 0, but we want it to round "negative"
      if (projected[i] < 0)
        hashed[i]--;
    }
  }
  
  // input space to hashed space
  protected void project(double[] values, double[] gp, double factor) {
    double musum = MU * factor;
    for(int i = 0; i < gp.length; i++) {
      gp[i] = ((values[i] / gridsize) / S3 + musum);
    }
  }
  
  // input space to hashed space
  @Override
  public Double getFactor(double[] values) {
    double sum = 0.0d;
    for(int i = 0; i < values.length; i++) {
      double x = values[i] / gridsize;   
      sum += x;
    }
    return sum;
  }
  
  // hashed space to input space
  @Override
  public void unhashDense(int[] hash, double[] values, Double factor) {
    double sum = factor / (1.0 / S3 + MU * hash.length); 
    for(int i = 0; i < hash.length; i++) {
      values[i] = S3 * (hash[i] -  MU * sum * gridsize);
    }
  }
  
  @Override
  public String toString() {
    return "dimensions=" + dimensions + ",gridsize=" + gridsize;
  }

}
