package org.apache.mahout.math.simplex;

/*
 * Orthonormal projection
 * 
 * Hash point to nearest grid corner.
 * stretch of 0.5 means grid to 0.5 instead of 1.0
 * 
 * Without a stretch, no dependence on dimensions.
 * TODO: make stretch sparse with a default value
 */

public class OrthonormalHasher extends Hasher {
  double[] stretch;
  int dimensions;
  
  public OrthonormalHasher(int dim, Double stretch) {
    dimensions = dim;
    this.stretch = new double[dim];
    for(int i = 0; i < dim; i++) {
      this.stretch[i] = stretch;
    }
  }
  
  public OrthonormalHasher(double stretch[]) {
    this.dimensions = stretch.length;
    this.stretch = stretch;
  }
  
  public OrthonormalHasher() {
    dimensions = 0;
  }
  
  @Override
  public void hashDense(double[] values, int[] hashed) {
    for(int i = 0; i < values.length; i++) {
      if (null != stretch) {
        hashed[i] = (int) Math.floor(values[i] / stretch[i]);
      } else {
        hashed[i] = (int) Math.floor(values[i]);
      } 
    }
  }
  
  @Override
  public void unhashDense(int[] hash, double[] values) {
    for (int i = 0; i < hash.length; i++) {
      if (null != stretch) 
        values[i] = hash[i] * stretch[i];
      else
        values[i] = hash[i];
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Orthonormal Hasher: dim=" + dimensions);
    if (null != stretch) {
      sb.append("[");
      for(int i = 0; i < stretch.length; i++) {
        sb.append(stretch[i]);
        sb.append(',');
      }
      sb.setLength(sb.length() - 1);
      sb.append("]");
    }
    return sb.toString();
  }
  
  
  
  
}
