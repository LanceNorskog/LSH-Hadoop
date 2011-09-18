package org.apache.mahout.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.mahout.math.CardinalityException;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.Vector;

/*
 * Calculate the optimal (minimal RMS Error) rotation matrix for 
 * two matched lists of vectors. 
 * Works for all dimensions.
 * http://en.wikipedia.org/wiki/Kabsch_algorithm
 * http://cnx.org/content/m11608/latest/#MatrixAlignment
 * 
 * Assume small dimensions. Rotating 100k dimension vectors is a bit silly.
 * And the SVD of the covariance matrix is not tractable anyway.
 * 
 * Algorithm stolen from molecule-matching world for visualization projects.
 */

public class KabschRotation {
  private final int numVectors;
  private final int dimensions;
  private final Matrix rotation;
  
  // two sets of points. May require translation to centroid
  public KabschRotation(List<Vector> first, List<Vector> second, boolean translate) {
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
      centerVectors(aFinal, aTmp);
      centerVectors(bFinal, bTmp);
      aFinal = aTmp;
      bFinal = bTmp;
    }
    Matrix left = getBaseMatrix(aFinal).transpose();
    Matrix right = getBaseMatrix(bFinal);
    Matrix covariance = left.times(right);
    double covarDet = covariance.determinant();
    SingularValueDecomposition svd = new SingularValueDecomposition(covariance);
    double sign = Math.signum(covarDet);
    Matrix mSign = new DenseMatrix(dimensions, dimensions);
    for(int d = 0; d < dimensions -1; d++)
      mSign.set(d,  d, 1.0);
    mSign.set(dimensions -1, dimensions -1, sign);
    this.rotation = svd.getV().times(mSign).times(svd.getU().transpose());
  }
  
  public Matrix getRotation() {
    return rotation;
  }

  public static void centerVectors(List<Vector> from, List<Vector> to) {
    Vector centroid = getCentroid(from);
    for(Vector v: from) {
      Vector xlated = v.minus(centroid);
      to.add(xlated);
    }
  }
  
  private void checkDimensions(List<Vector> vecs) {
    for(Vector v: vecs) {
      if (v.size() != dimensions) 
        throw new CardinalityException(dimensions, v.size());
    }
  }
  
  public static Vector getCentroid(List<Vector> from) {
    Vector sum = new DenseVector(from.get(0).size());
    for(Vector v: from) {
      sum = sum.plus(v);
    }
    Vector centroid = sum.divide(from.size());
    return centroid;
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
  
}
