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
  final private double gridsize;
  final private int dimensions;
  
  public OrthonormalHasher(int dimensions, double gridsize) {
    this.dimensions = dimensions;
    this.gridsize = gridsize;
  }
  
  @Override
  public void hashDense(double[] values, int[] hashed) {
    for(int i = 0; i < values.length; i++) {
      hashed[i] = (int) Math.floor(values[i] / gridsize);
    }
  }
  
  @Override
  public void unhashDense(int[] hash, double[] values) {
    for (int i = 0; i < hash.length; i++) {
      values[i] = hash[i] * gridsize;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Orthonormal Hasher(dense only): dim=" + dimensions);
    if (gridsize != 1.0) {
      sb.append('[' + gridsize + ']');
    }
    return sb.toString();
  }
  
  
  
  
}
