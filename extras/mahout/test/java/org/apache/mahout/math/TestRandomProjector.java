package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Test;

public class TestRandomProjector extends MahoutTestCase {
  static int[] index = {3, 15, 22};
  static double[] orig = {8, 10, 43};
  static double[] xformed_2of6 = {16, 0, -258};
  static double[] xformed_plusminus = {25,-41,-25};
  static double[] xformed_java = {100.78128815655742, 100.78128722061669, 105.70396068105019};
  
  @Test
  public void TestMatrix() {
    
  }
  
  public void testFactory() {
    assertTrue(RandomProjector.getProjector(1,1,1,false) instanceof RandomProjectorPlusMinus);
    assertTrue(RandomProjector.getProjector(1,1,1,true) instanceof RandomProjector2of6);
  }
  
  @Test
  public void testVector2of6() {
    runPairwise(new RandomProjector2of6(3, 3, 0), new RVector(), "2of6 pairwise: ");
    runModes(new RandomProjector2of6(50, 50, 0), xformed_2of6, true, "2of6 sparse:");
    runModes(new RandomProjector2of6(50, 50, 0), xformed_2of6, false, "2of6 dense:");
  }
  
  @Test
  public void testVectorPlusMinus() {
    runPairwise(new RandomProjectorPlusMinus(3, 3, 0), new RVector(), "+1/-1 pairwise: ");
    runModes(new RandomProjectorPlusMinus(3, 3, 0), xformed_plusminus, true, "+1/-1 sparse: ");
    runModes(new RandomProjectorPlusMinus(3, 3, 0), xformed_plusminus, false, "+1/-1 dense: ");
  }
  
  @Test
  public void testVectorPlusJDK() {
    runPairwise(new RandomProjectorJava(50, 50, 0), new RVector(), "JDK pairwise: ");
    runModes(new RandomProjectorJava(3, 3, 0), xformed_java, true, "JDK sparse: ");
    runModes(new RandomProjectorJava(3, 3, 0), xformed_java, false, "JDK dense: ");
  }
  
  private void runModes(RandomProjector rp, double[] xformed, boolean sparse, String kind) {
    int large = 50;
    Vector v = sparse ? new RandomAccessSparseVector(large) : new DenseVector(large);
    for(int i = 0; i < index.length; i++) {
      v.setQuick(index[i], orig[i]);
    }
    Vector w = rp.times(v);
//    Vector w2 = rp.times(v);
//    Vector w3 = rp.times(v);
//    assertTrue(w.equals(w2));
//    assertTrue(w2.equals(w3));
    for(int i = 0; i < w.size(); i++) {
      double d = w.getQuick(i);
        double expected = xformed[i];
        assertEquals(kind + "index: " + i , expected, d, EPSILON);
    }
  }
  
  // test pairwise distance several times with different random numbers
  private void runPairwise(RandomProjector rp, RVector rv, String kind) {
    Random datagen = RandomUtils.getRandom(0);
    checkPairwiseDistances(rp, rv, datagen, kind + "check #" + 1);
    checkPairwiseDistances(rp, rv, datagen, kind + "check #" + 2);
    checkPairwiseDistances(rp, rv, datagen, kind + "check #" + 3);
    checkPairwiseDistances(rp, rv, datagen, kind + "check #" + 4);
  }
  
  void checkPairwiseDistances(RandomProjector rp, RVector rv,Random rnd, String kind) {
    int large = 200;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    Vector v1 = rv.randVector(large, rnd, 1);
    Vector v2 = rv.randVector(large, rnd, -1);
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1);
    Vector w2 = rp.times(v2);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    v1 = rv.randVector(large, rnd, 1000);
    v2 = rv.randVector(large, rnd, -1000);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1);
    w2 = rp.times(v2);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
    assertEquals(kind, 1.0, ratio1/ratio2, close);
  }
  
}
 class RVector {
  // create non-random vector with random perturbations
  Vector randVector(int size, Random rnd, int sign) {
    double[] values = new double[size];
    for(int i = 0; i < size; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}

