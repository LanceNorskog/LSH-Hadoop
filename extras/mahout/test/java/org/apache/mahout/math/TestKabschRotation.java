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
import org.junit.Test;

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

public class TestKabschRotation extends MahoutTestCase {
  
  @Test
  public void testKR() {
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
    
    Vector centroid = KabschRotation.getCentroid(aVecs);
    Vector3D axis = new Vector3D(0.7, 0.3, 0.2);
    Rotation mangler = new Rotation(axis, 0.7);
    
    double[] right1 = rotate(mangler, left1);
    double[] right2 = rotate(mangler, left2);
    double[] right3 = rotate(mangler, left3);
    double[] right4 = rotate(mangler, left4);
    double[] right5 = rotate(mangler, left5);

    bVecs.add(new DenseVector(right1));
    bVecs.add(new DenseVector(right2));
    bVecs.add(new DenseVector(right3));
    bVecs.add(new DenseVector(right4));
    bVecs.add(new DenseVector(right5));
    
    KabschRotation kr = new KabschRotation(aVecs, bVecs, true);
    List<Vector> aVecsCentered = new ArrayList<Vector>();
    KabschRotation.centerVectors(aVecs, aVecsCentered );
    List<Vector> bVecsCentered = new ArrayList<Vector>();
    KabschRotation.centerVectors(bVecs, bVecsCentered );
    for(int i = 0; i < 5; i++) {
      Vector a = aVecsCentered.get(i);
      Vector b = bVecsCentered.get(i);
      Vector v2 = kr.getRotation().times(a);
      Vector delta = v2.minus(b);
      double norm = delta.norm(1);
      assertEquals(norm, 0.0, 0.0000000000001);
    }
  }

  static double[] rotate(Rotation mangler, double[] left1) {
    Vector3D out = mangler.applyTo(new Vector3D(left1[0], left1[1], left1[2]));
    return new double[]{out.getX(), out.getY(), out.getZ()};
  }
}
