package working;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.mahout.math.CardinalityException;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.Vector;

/*
 * Calculate the optimal (minimal RMS Error) rotation matrix for two matched lists of vectors
 * Works for all dimensions.
 * http://en.wikipedia.org/wiki/Kabsch_algorithm
 * 
 * Assume small dimensions. Rotating 100k dimension vectors is a bit silly.
 * And the covariance matrix is not tractable anyway.
 */

public class KabshRotation {
  private final int numVectors;
  private final int dimensions;
  private final List<Vector> aVecs;
  private final List<Vector> bVecs;
  private final Matrix rotation;
  
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
      List<Vector> aTmp = new ArrayList<Vector>();
      List<Vector> bTmp = new ArrayList<Vector>();
      translateVectors(first, aTmp);
      translateVectors(second, bTmp);
      normalizeVectors(aTmp, aVecs);
      normalizeVectors(bTmp, bVecs);
    } else {
      this.aVecs = first;
      this.bVecs = second;
    }
    Matrix left = getBaseMatrix(aVecs).transpose();
    Matrix right = getBaseMatrix(bVecs);
    Matrix covariance = left.times(right);
    SingularValueDecomposition svd = new SingularValueDecomposition(covariance);
    double covarDet = covariance.determinant();
    double sign = Math.signum(covarDet);
    Matrix mSign = new DenseMatrix(dimensions, dimensions);
    for(int d = 0; d < dimensions -1; d++)
      mSign.set(d,  d, 1);
    mSign.set(dimensions -1, dimensions -1, sign);
    this.rotation = svd.getV().transpose().times(mSign).times(svd.getU());
  }
  
  private void checkDimensions(List<Vector> vecs) {
    for(Vector v: vecs) {
      if (v.size() != dimensions) 
        throw new CardinalityException(dimensions, v.size());
    }
  }

  private void translateVectors(List<Vector> from, List<Vector> to) {
    Vector centroid = getCentroid(from);
    for(Vector v: from) {
      Vector xlated = v.minus(centroid);
      to.add(xlated);
    }
  }
  
  private void normalizeVectors(List<Vector> from, List<Vector> to) {
    double maxNorm = getMaxNorm(from);
    for(Vector v: from) {
      Vector normal = v.divide(maxNorm);
      to.add(normal);
    }
  }
  
  private Vector getCentroid(List<Vector> from) {
    Vector sum = new DenseVector(dimensions);
    for(Vector v: from) {
      v.addTo(sum);
    }
    Vector centroid = sum.divide(numVectors);
    return centroid;
  }
  
  private double getMaxNorm(List<Vector> from) {
    double max = Double.MIN_VALUE;
    for(Vector v: from) {
      double norm = v.getLengthSquared();
      max = Math.max(max, norm);
    }
    return Math.sqrt(max);
  }
  
  // and back comes our old friend the VectorList matrix
  Matrix getBaseMatrix(List<Vector> vecs) {
    Matrix m = new DenseMatrix(numVectors, dimensions);
    for(int r = 0; r < numVectors; r++) {
      Vector v = vecs.get(r);
      for(int c = 0; c < dimensions; c++) {
        m.set(r, c, v.get(c));
      }
    }
    return m;
  }
  
  static public void main(String[] args) {
    List<Vector> aVecs = new ArrayList<Vector>();
    List<Vector> bVecs = new ArrayList<Vector>();
    double[] left1 = {2, 3};
    double[] left2 = {3, 4};
    double[] left3 = {-1, -9};
    aVecs.add(new DenseVector(left1));
    aVecs.add(new DenseVector(left2));
    aVecs.add(new DenseVector(left3));
    double[] right1 = {9,14};
    double[] right2 = {6,3};
    double[] right3 = {-2,-18};
    bVecs.add(new DenseVector(right1));
    bVecs.add(new DenseVector(right2));
    bVecs.add(new DenseVector(right3));
    KabshRotation kr = new KabshRotation(aVecs, bVecs, true);
    // kr.aVecs and kr.bVecs are both translated to their centroids, and normalized
    for(int i = 0; i < 3; i++) {
      Vector a = kr.aVecs.get(i);
      Vector b = kr.bVecs.get(i);
      Vector v2 = kr.rotation.times(a);
      Vector delta = v2.minus(b);
      System.out.print("Rotate #" + i + " -> " + a.toString());
      System.out.println(" To " + b.toString());
      System.out.println("Delta #" + i + " -> " + delta.toString());
    }
  }
}
