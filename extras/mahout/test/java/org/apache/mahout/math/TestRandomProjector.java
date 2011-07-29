package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Test;

public class TestRandomProjector extends MahoutTestCase {
  static int[] index = {3, 15, 22, 23, 24, 25, 62, 63, 64, 65};
  static double[] orig = {8, 10, 43, 2, 3, 4, 100, 101, 102, 103};
  static double[] xformed_2of6 = {-5, -13, 116};
  static double[] xformed_plusminus = {266, 262, -156};
  static double[] xformed_jdk = {1133.759463713984, 1345.1046648866545, 1415.3709508101479};
  
  public void testFactory() {
    assertTrue(RandomProjector.getProjector() instanceof RandomProjectorPlusMinus);
    assertTrue(RandomProjector.getProjector() instanceof RandomProjector2of6);
  }
  
  @Test
  public void testVector2of6() {
    runPairwise(new RandomProjector2of6(0), "2of6 pairwise: ");
    runModes(new RandomProjector2of6(0), xformed_2of6, true, "2of6 sparse:");
    runModes(new RandomProjector2of6(0), xformed_2of6, false, "2of6 dense:");
  }
  
  @Test
  public void testVectorPlusMinus() {
    runPairwise(new RandomProjectorPlusMinus(0), "+1/-1 pairwise: ");
    runModes(new RandomProjectorPlusMinus(0), xformed_plusminus, true, "+1/-1 sparse: ");
    runModes(new RandomProjectorPlusMinus(0), xformed_plusminus, false, "+1/-1 dense: ");
  }
  
  @Test
  public void testVectorPlusJDK() {
    runPairwise(new RandomProjectorJDK(0), "JDK pairwise: ");
    runModes(new RandomProjectorJDK(0), xformed_jdk, false, "JDK dense: ");
    runModes(new RandomProjectorJDK(0), xformed_jdk, true, "JDK sparse: ");
  }
  
  private void runModes(RandomProjector rp, double[] xformed, boolean sparse, String kind) {
    int large = 100;
    Vector v = sparse ? new RandomAccessSparseVector(large) : new DenseVector(large);
    for(int i = 0; i < index.length; i++) {
      v.setQuick(index[i], orig[i]);
    }
    Vector w = rp.times(v, xformed.length);
//    System.out.println("w = " + w.toString());
    for(int i = 0; i < w.size(); i++) {
      double d = w.getQuick(i);
      double expected = xformed[i];
      assertEquals(kind + "index: " + i , expected, d, EPSILON);
    }
  }
  
  // test pairwise distance several times with different random numbers
  private void runPairwise(RandomProjector rp, String kind) {
    Random datagen = RandomUtils.getRandom(0);
    checkPairwiseDistancesDense(rp, datagen, kind + "dense #" + 1);
    checkPairwiseDistancesDense(rp, datagen, kind + "dense #" + 2);
    checkPairwiseDistancesDense(rp, datagen, kind + "dense #" + 3);
    checkPairwiseDistancesDense(rp, datagen, kind + "dense #" + 4);
    checkPairwiseDistancesSparse(rp, datagen, kind + "sparse #" + 1);
    checkPairwiseDistancesSparse(rp, datagen, kind + "sparse #" + 2);
    checkPairwiseDistancesSparse(rp, datagen, kind + "sparse #" + 3);
    checkPairwiseDistancesSparse(rp, datagen, kind + "sparse #" + 4);
  }
  
  void checkPairwiseDistancesDense(RandomProjector rp, Random rnd, String kind) {
    int large = 200;
    int small = 20;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    Vector v1 = newPerturbed(large, rnd, 1);
    Vector v2 = newPerturbed(large, rnd, -1);
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1, small);
    Vector w2 = rp.times(v2, small);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    v1 = newPerturbed(large, rnd, 1000);
    v2 = newPerturbed(large, rnd, -1000);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1, small);
    w2 = rp.times(v2, small);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    //    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
    assertEquals(kind, 1.0, ratio1/ratio2, close);
  }
  
  void checkPairwiseDistancesSparse(RandomProjector rp, Random rnd, String kind) {
    int huge = 100000;
    int large = 200;
    int small = 20;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    int[] indexes = getIndexes(large, huge, rnd);
    Vector p1 = newPerturbed(large, rnd, 1);
    Vector p2 = newPerturbed(large, rnd, -1);
    Vector v1 = getScattered(huge, rnd, indexes, p1);
    Vector v2 = getScattered(huge, rnd, indexes, p2);
    
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1, small);
    Vector w2 = rp.times(v2, small);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    p1 = newPerturbed(large, rnd, 1000);
    p2 = newPerturbed(large, rnd, -1000);
    v1 = getScattered(huge, rnd, indexes, p1);
    v2 = getScattered(huge, rnd, indexes, p2);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1, small);
    w2 = rp.times(v2, small);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    //    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
    assertEquals(kind, 1.0, ratio1/ratio2, close);
  }
  
  private int[] getIndexes(int large, int huge, Random rnd) {
    int[] indexes = new int[large];
    for(int i = 0; i < large; i++)
      indexes[i] = rnd.nextInt(huge);
    return indexes;
  }
  
  private Vector getScattered(int huge, Random rnd, int[] indexes, Vector p) {
    Vector v = new RandomAccessSparseVector(huge);
    for(int i = 0; i < p.size(); i++) {
      v.set(indexes[i], p.get(i));
    }
    return v;
  }
  
  // create non-random vector with random perturbations
  static Vector newPerturbed(int size, Random rnd, int sign) {
    double[] values = new double[size];
    for(int i = 0; i < size; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}

