package working;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;


public class TestKabschRotation extends TestCase {
  static public double EPSILON = 1.0e10;

/*  
 * Generate pair of test vector sets by creating "left" set and rotating  
 * it using commons-math 3d vector rotator to create "right" set.
 * Generate Kabsch rotator from left & rotated, 
 * Compare Kabsch rotator's output with "right" vectors.
 * Left vectors and output of 3d vector rotator are centered to 0; Kabsch requires this.
 *
 * Deltas should be EPSILON or smaller. They are around 1e15.
*/
  
  public void test3d_5vectors() {
    List<Vector> tmp = new ArrayList<Vector>();
    List<Vector> aVecs = new ArrayList<Vector>();
    List<Vector> bVecs = new ArrayList<Vector>();
    double[] left1 = {2, 3, 1};
    double[] left2 = {3, 4, 5};
    double[] left3 = {-1, -9, 3};
    double[] left4 = {-2, -9, 12};
    double[] left5 = {-3, -9, -9};
    
    tmp.add(new DenseVector(left1));
    tmp.add(new DenseVector(left2));
    tmp.add(new DenseVector(left3));
    tmp.add(new DenseVector(left4));
    tmp.add(new DenseVector(left5));
    KabschRotation.centerVectors(tmp, aVecs);
    
    Vector3D axis = new Vector3D(0.7, 0.3, 0.2);
    Rotation mangler = new Rotation(axis, 0.7);
    
    double[] right1 = rotate(mangler, left1);
    double[] right2 = rotate(mangler, left2);
    double[] right3 = rotate(mangler, left3);
    double[] right4 = rotate(mangler, left4);
    double[] right5 = rotate(mangler, left5);

    tmp.clear();
    tmp.add(new DenseVector(right1));
    tmp.add(new DenseVector(right2));
    tmp.add(new DenseVector(right3));
    tmp.add(new DenseVector(right4));
    tmp.add(new DenseVector(right5));
    KabschRotation.centerVectors(tmp, bVecs);
    
    KabschRotation kr = new KabschRotation(aVecs, bVecs, false);
    for(int i = 0; i < aVecs.size(); i++) {
      Vector a = aVecs.get(i);
      Vector b = bVecs.get(i);
      Vector v2 = kr.getRotation().times(a);
      compareVectors(b, v2);
    }
  }

  private void compareVectors(Vector a, Vector b) {
    for(int d = 0; d < a.size(); d++) 
      assertEquals(a.get(d), b.get(d), EPSILON);
  }

  private static double[] rotate(Rotation mangler, double[] left1) {
    Vector3D out = mangler.applyTo(new Vector3D(left1[0], left1[1], left1[2]));
    return new double[]{out.getX(), out.getY(), out.getZ()};
  }
  

}
