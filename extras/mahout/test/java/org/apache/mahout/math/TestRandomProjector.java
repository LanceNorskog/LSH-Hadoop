package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Test;

public class TestRandomProjector extends MahoutTestCase {
  
  @Test
  public void TestMatrix() {
    
  }
  
  @Test
  public void testVectorSmall() {
    double CLOSE = 0.01;
    double[] values = {1, 2, 3, 4, 0, 100, 101, 102, 103, 104};
    double[] values26 = {2.0,-4.0,12.0,-16.0,0,-1200.0,606.0,-408.0,206.0,208.0};
    double[] valuesPlusMinus = {-3.0, -6.0, -9.0, -12.0, 0, -100.0, -101.0,
        -510.0, -103.0, 104.0};
    Vector v = new DenseVector(values);
    RandomProjector rp = new RandomProjector2of6(0);
    Vector w = rp.times(v);
    for(int i = 0; i < 10; i++)
      assertEquals("2of6[" + i + "]", values26[i], w.get(i), EPSILON);
    
    rp = new RandomProjectorPlusMinus();
    w = rp.times(v);
    for(int i = 0; i < 10; i++)
      assertEquals("+1/-1[" + i + "]", valuesPlusMinus[i], w.get(i), EPSILON);
  }
  
  @Test
  public void testFactory() {
    assertTrue(RandomProjector.getProjector(false) instanceof RandomProjectorPlusMinus);
    assertTrue(RandomProjector.getProjector(true) instanceof RandomProjector2of6);
  }
  
  @Test
  public void testVector2of6Dense() {
    runMany(new RandomProjector2of6(), new RVectorDense(), "2of6: ");
  }
  
  @Test
  public void testVectorPlusMinusDense() {
    runMany(new RandomProjectorPlusMinus(), new RVectorDense(), "+1/-1: ");
  }
  
  @Test
  public void testVector2of6Sparse() {
    runMany(new RandomProjector2of6(), new RVectorSparse(), "2of6: ");
    runSparse(new RandomProjector2of6(), "2of6:");
  }
  
  private void runSparse(RandomProjector rp, String kind) {
    int large = 50;
    int[] index = {3, 15, 22};
    double[] orig = {8, 10, 43};
    double[] xformed = {16, 0, -258};
    Vector v = new RandomAccessSparseVector(large);
    for(int i = 0; i < index.length; i++) {
      v.setQuick(index[i], orig[i]);
    }
    Vector w = rp.times(v);
    int cursor = 0;
    for(int i = 0; i < large; i++) {
      double d = w.getQuick(i);
      if (cursor < index.length && i == index[cursor]) {
        double expected = xformed[cursor];
        assertEquals("index: " + i + "cursor: " + cursor, xformed[cursor], d, EPSILON);
        cursor++;
      } else {
        assertEquals("index: " + i, 0, d, Double.MIN_VALUE);
      }
    }
  }

  @Test
  public void testVectorPlusMinusSparse() {
    runMany(new RandomProjectorPlusMinus(), new RVectorSparse(), "+1/-1: ");
  }
  
  // test pairwise distance several times with different random numbers
  private void runMany(RandomProjector rp, RVector rv, String kind) {
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

abstract class RVector {
  abstract Vector randVector(int size, Random rnd, int sign);
}

class RVectorDense extends RVector {
  // create non-random vector with random perturbations
  Vector randVector(int size, Random rnd, int sign) {
    double[] values = new double[size];
    for(int i = 0; i < size; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}

class RVectorSparse extends RVector {
  // create non-random vector with random perturbations
  Vector randVector(int size, Random rnd, int sign) {
    double[] values = new double[size];
    for(int i = 0; i < size; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}

