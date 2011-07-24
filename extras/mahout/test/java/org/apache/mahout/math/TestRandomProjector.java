package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Test;

public class TestRandomProjector extends MahoutTestCase {
  static int[] index = {3, 15, 22};
  static double[] orig = {8, 10, 43};
  static double[] xformed_2of6 = {10, -33, 53};
  static double[] xformed_plusminus = {-41,-45,-61};
  static double[] xformed_java = {184.00089446441018, 325.77106038862394, 627.7714781075734};
  
  public void testFactory() {
    assertTrue(RandomProjector.getProjector(false) instanceof RandomProjectorPlusMinus);
    assertTrue(RandomProjector.getProjector(true) instanceof RandomProjector2of6);
  }
  
  @Test
  public void testVector2of6() {
    runPairwise(new RandomProjector2of6(0), new Perturbed(), "2of6 pairwise: ");
    runModes(new RandomProjector2of6(0), xformed_2of6, true, "2of6 sparse:");
    runModes(new RandomProjector2of6(0), xformed_2of6, false, "2of6 dense:");
  }
  
  @Test
  public void testVectorPlusMinus() {
    runPairwise(new RandomProjectorPlusMinus(0), new Perturbed(), "+1/-1 pairwise: ");
    runModes(new RandomProjectorPlusMinus(0), xformed_plusminus, true, "+1/-1 sparse: ");
    runModes(new RandomProjectorPlusMinus(0), xformed_plusminus, false, "+1/-1 dense: ");
  }
  
  @Test
  public void testVectorPlusJDK() {
    runPairwise(new RandomProjectorJava(0), new Perturbed(), "JDK pairwise: ");
    runModes(new RandomProjectorJava(0), xformed_java, true, "JDK sparse: ");
    runModes(new RandomProjectorJava(0), xformed_java, false, "JDK dense: ");
  }
  
  private void runModes(RandomProjector rp, double[] xformed, boolean sparse, String kind) {
    int large = 50;
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
  private void runPairwise(RandomProjector rp, Perturbed rv, String kind) {
    Random datagen = RandomUtils.getRandom(0);
    checkPairwiseDistancesDense(rp, rv, datagen, kind + "dense #" + 1);
    checkPairwiseDistancesDense(rp, rv, datagen, kind + "dense #" + 2);
    checkPairwiseDistancesDense(rp, rv, datagen, kind + "dense #" + 3);
    checkPairwiseDistancesDense(rp, rv, datagen, kind + "dense #" + 4);
    checkPairwiseDistancesSparse(rp, rv, datagen, kind + "sparse #" + 1);
    checkPairwiseDistancesSparse(rp, rv, datagen, kind + "sparse #" + 2);
    checkPairwiseDistancesSparse(rp, rv, datagen, kind + "sparse #" + 3);
    checkPairwiseDistancesSparse(rp, rv, datagen, kind + "sparse #" + 4);
  }
  
  void checkPairwiseDistancesDense(RandomProjector rp, Perturbed rv, Random rnd, String kind) {
    int large = 200;
    int small = 20;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    Vector v1 = rv.newPerturbed(large, rnd, 1);
    Vector v2 = rv.newPerturbed(large, rnd, -1);
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1, small);
    Vector w2 = rp.times(v2, small);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    v1 = rv.newPerturbed(large, rnd, 1000);
    v2 = rv.newPerturbed(large, rnd, -1000);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1, small);
    w2 = rp.times(v2, small);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
//    assertEquals(kind, 1.0, ratio1/ratio2, close);
  }
  
  void checkPairwiseDistancesSparse(RandomProjector rp, Perturbed rv, Random rnd, String kind) {
    int huge = 100000;
    int large = 200;
    int small = 20;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    int[] indexes = getIndexes(large, huge, rnd);
    Vector p1 = rv.newPerturbed(large, rnd, 1);
    Vector p2 = rv.newPerturbed(large, rnd, -1);
    Vector v1 = getScattered(huge, rnd, indexes, p1);
    Vector v2 = getScattered(huge, rnd, indexes, p2);
    
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1, small);
    Vector w2 = rp.times(v2, small);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    p1 = rv.newPerturbed(large, rnd, 1000);
    p2 = rv.newPerturbed(large, rnd, -1000);
    v1 = getScattered(huge, rnd, indexes, p1);
    v2 = getScattered(huge, rnd, indexes, p2);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1, small);
    w2 = rp.times(v2, small);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
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
  
}
 class Perturbed {
  // create non-random vector with random perturbations
  Vector newPerturbed(int size, Random rnd, int sign) {
    double[] values = new double[size];
    for(int i = 0; i < size; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}

