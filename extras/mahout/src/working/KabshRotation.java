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
  public KabshRotation(List<Vector> first, List<Vector> second, boolean translate, boolean normalize) {
    if (first.size() != second.size() || first.size() < 2)
      throw new UnsupportedOperationException("KabshRotation: lists of points must be the same length");
    numVectors = first.size();
    dimensions = first.get(0).size();
    checkDimensions(first);
    checkDimensions(second);
    List<Vector> aFinal = first;
    List<Vector> bFinal = second;
    if (translate) {
      List<Vector> aTmp = new ArrayList<Vector>(numVectors);
      List<Vector> bTmp = new ArrayList<Vector>(numVectors);
      translateVectors(aFinal, aTmp);
      translateVectors(bFinal, bTmp);
      aFinal = aTmp;
      bFinal = bTmp;
    }
    if (normalize) {
      List<Vector> aTmp = new ArrayList<Vector>(numVectors);
      List<Vector> bTmp = new ArrayList<Vector>(numVectors);
      normalizeVectors(aFinal, aTmp);
      normalizeVectors(bFinal, bTmp);
      aFinal = aTmp;
      bFinal = bTmp;
    } 
    this.aVecs = aFinal;
    this.bVecs = bFinal;
    Matrix left = getBaseMatrix(aVecs).transpose();
    Matrix right = getBaseMatrix(bVecs);
    Matrix covariance = left.times(right);
    double covarDet = covariance.determinant();
    SingularValueDecomposition svd = new SingularValueDecomposition(covariance);
    System.out.println("Covariance Determinant: " + covarDet);
    double sign = Math.signum(covarDet);
    Matrix mSign = new DenseMatrix(dimensions, dimensions);
    for(int d = 0; d < dimensions -1; d++)
      mSign.set(d,  d, 1.0);
    mSign.set(dimensions -1, dimensions -1, sign);
    // Note: this matches covariance matrix within 0.00000001, SVD and "times order" is correct
    Matrix c = svd.getU().times(svd.getS()).times(svd.getV().transpose());
    this.rotation = svd.getV().times(mSign).times(svd.getU().transpose());
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
    double[] left1 = {2, 3, 1};
    double[] left2 = {3, 4, 5};
    double[] left3 = {-1, -9, 3};
    double[] left4 = {-2, -9, 12};
    double[] left5 = {-3, -9, -9};
    aVecs.add(new DenseVector(left1));
    aVecs.add(new DenseVector(left2));
    aVecs.add(new DenseVector(left3));
    aVecs.add(new DenseVector(left4));
    aVecs.add(new DenseVector(left5));
    double[] right1 = {9,14,12};
    double[] right2 = {6,3,8};
    double[] right3 = {-2,-18,-3};
    double[] right4 = {-1,-18,-4};
    double[] right5 = {-0,-18,-5};
    bVecs.add(new DenseVector(right1));
    bVecs.add(new DenseVector(right2));
    bVecs.add(new DenseVector(right3));
    bVecs.add(new DenseVector(right4));
    bVecs.add(new DenseVector(right5));
    KabshRotation kr = new KabshRotation(aVecs, bVecs, true, false);
    // kr.aVecs and kr.bVecs are both translated to their centroids, and normalized
    System.out.println("Rotate all left set vectors and compare to right set. Deltas should be 0.00001 or smaller.");
    for(int i = 0; i < kr.numVectors; i++) {
      Vector a = kr.aVecs.get(i);
      Vector b = kr.bVecs.get(i);
      Vector v2 = kr.rotation.times(a);
      Vector delta = v2.minus(b);
      System.out.print("Rotate #" + i + " -> " + a.toString());
      System.out.println(" To " + b.toString());
      System.out.println("Delta #" + i + " -> " + delta.toString());
      System.out.println();
    }
  }
}
