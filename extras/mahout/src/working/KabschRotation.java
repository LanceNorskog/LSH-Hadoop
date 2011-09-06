package working;

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
  
  private static Vector getCentroid(List<Vector> from) {
    Vector sum = new DenseVector(from.get(0).size());
    for(Vector v: from) {
      v.addTo(sum);
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
    
    Vector centroid = KabschRotation.getCentroid(aVecs);
    for(int i = 0; i < 5; i++) {
      
    }
    
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
    // kr.aVecs and kr.bVecs are both translated to their centroids, and normalized
    System.out.println("Rotate all left set vectors and compare to right set. Deltas should be 0.00001 or smaller.");
//    for(int i = 0; i < kr.numVectors; i++) {
//      Vector a = kr.aVecs.get(i);
//      Vector b = kr.bVecs.get(i);
//      Vector v2 = kr.getRotation().times(a);
//      Vector delta = v2.minus(b);
//      System.out.print("Rotate #" + i + " -> " + a.toString());
//      System.out.println(" To " + b.toString());
//      System.out.println("Delta #" + i + " -> " + delta.toString());
//      System.out.println();
//    }
  }

  private static double[] rotate(Rotation mangler, double[] left1) {
    Vector3D out = mangler.applyTo(new Vector3D(left1[0], left1[1], left1[2]));
    return new double[]{out.getX(), out.getY(), out.getZ()};
  }
}
