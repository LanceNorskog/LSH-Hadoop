package working;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.mahout.math.CardinalityException;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

/*
 * Calculate the optimal (minimal RMS Error) rotation matrix for two matched lists of vectors
 * Works for all dimensions.
 * http://en.wikipedia.org/wiki/Kabsch_algorithm
 */

public class KabshRotation {
  private final int numVectors;
  private final int dimensions;
  private final List<Vector> aVecs;
  private final List<Vector> bVecs;
  
  // two sets of points. May require translation to centroid
  public KabshRotation(List<Vector> first, List<Vector> second, boolean translate) {
    if (first.size() != second.size() || first.size() < 2)
      throw new UnsupportedOperationException("KabshRotation: lists of points must be the same length");
    numVectors = first.size();
    dimensions = first.get(0).size();
    checkDimensions(first);
    checkDimensions(second);
    if (translate) {
      this.aVecs = new ArrayList<Vector>(first.size());
      this.bVecs = new ArrayList<Vector>(first.size());
      translateVectors(first, aVecs);
      translateVectors(second, bVecs);
    } else {
      this.aVecs = first;
      this.bVecs = second;
    }
    
  }
  
  private void checkDimensions(List<Vector> vecs) {
    for(Vector v: vecs) {
      if (v.size() != dimensions) 
        throw new CardinalityException(dimensions, v.size());
    }
    
  }

  private void translateVectors(List<Vector> from, List<Vector> to) {
    Vector centroid = getCentroid(from);
    centroid.toString();
    for(Vector v: from) {
      Vector xlated = v.minus(centroid);
      to.add(xlated);
    }
  }
  
  private Vector getCentroid(List<Vector> from) {
    double small[] = new double[dimensions];
    Arrays.fill(small, -Double.MAX_VALUE);
    double big[] = new double[dimensions];
    Arrays.fill(big, Double.MAX_VALUE);
    Vector min = new DenseVector(big);
    Vector max = new DenseVector(small);
    
    for(Vector v: from) {
      for(int d = 0; d < dimensions; d++) {
        min.set(d, Math.min(min.get(d), v.get(d)));
        max.set(d, Math.max(max.get(d), v.get(d)));
      }    
    }
    Vector centroid = min.minus(max).times(-0.5);
    return centroid;
  }
  
  static public void main(String[] args) {
    List<Vector> left = new ArrayList<Vector>();
    List<Vector> right = new ArrayList<Vector>();
    double[] left1 = {2, 3};
    double[] left2 = {3, 4};
    double[] left3 = {-1, -9};
    left.add(new DenseVector(left1));
    left.add(new DenseVector(left2));
    left.add(new DenseVector(left3));
    double[] right1 = {9,14};
    double[] right2 = {6,3};
    double[] right3 = {-2,-18};
    right.add(new DenseVector(right1));
    right.add(new DenseVector(right2));
    right.add(new DenseVector(right3));
    KabshRotation kr = new KabshRotation(left, right, true);
    Vector c = kr.getCentroid(left);
    c = kr.getCentroid(right);
    c.hashCode();
  }
}
