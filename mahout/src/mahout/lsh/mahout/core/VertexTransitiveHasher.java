package lsh.mahout.core;

/*
 * Vertex Transitive projection
 * 
 * Space-filling triangular simplices.
 */

public class VertexTransitiveHasher extends Hasher {
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
  public void hash(double[] values, int[] hashed) {
    double[] projected = new double[values.length];
    project(values, projected);
    for(int i = 0; i < projected.length; i++) {
      hashed[i] = (int) (projected[i]);
    }
  }

  
  // input space to hashed space
  protected void project(double[] values, double[] gp) {
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
  
  // hashed space to input space
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
  
}
