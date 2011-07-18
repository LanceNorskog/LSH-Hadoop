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
    double[] values = {1, 2, 3, 4, 5, 100, 101, 102, 103, 104};
    double[] values26 = {2.0, -8.0, 36.0, -64.0, 0, -120000.0, 61206.0,
        -41616.0, 21218.0, 21632.0};
    double[] valuesPlusMinus = {-3.0, -6.0, -9.0, -12.0, 15.0, -100.0, -101.0,
        -510.0, -103.0, 104.0};
    Vector v = new DenseVector(values);
    RandomProjector rp = new RandomProjector2of6(0);
    Vector w = rp.times(v);
    for(int i = 0; i < 10; i++)
      assertEquals("2of6[" + i + "]", w.get(i), values26[i], EPSILON);
    
    rp = new RandomProjectorPM_murmur();
    w = rp.times(v);
    for(int i = 0; i < 10; i++)
      assertEquals("+1/-1[" + i + "]", w.get(i), valuesPlusMinus[i], EPSILON);
  }
  
  @Test
  public void testVectorAchDense() {
    runMany(new RandomProjector2of6(), "2of6: ");
  }

  @Test
  public void testVectorPlusMinusDense() {
    runMany(new RandomProjectorPM_murmur(), "+1/-1: ");
  }

  // test pairwise distance several times with different random numbers
  private void runMany(RandomProjector rp, String kind) {
    Random datagen = RandomUtils.getRandom(0);
    checkPairwiseDistances(rp, datagen, kind + "check #" + 1);
    checkPairwiseDistances(rp, datagen, kind + "check #" + 2);
    checkPairwiseDistances(rp, datagen, kind + "check #" + 3);
    checkPairwiseDistances(rp, datagen, kind + "check #" + 4);
  }
  
  void checkPairwiseDistances(RandomProjector rp, Random rnd, String kind) {
    int large = 200;
    double close = 0.1; // very large epsilon
    
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    Vector v1 = denseRandom(large, rnd, 1);
    Vector v2 = denseRandom(large, rnd, -1);
    double origDistance = measure.distance(v1, v2);
    Vector w1 = rp.times(v1);
    Vector w2 = rp.times(v2);
    double projectedDistance = measure.distance(w1, w2);
    double ratio1 = projectedDistance / origDistance;
    v1 = denseRandom(large, rnd, 1000);
    v2 = denseRandom(large, rnd, -1000);
    origDistance = measure.distance(v1, v2);
    w1 = rp.times(v1);
    w2 = rp.times(v2);
    projectedDistance = measure.distance(w1, w2);
    double ratio2 = projectedDistance / origDistance;
    System.out.println(kind + " r1 v.s. r2: " + (ratio1/ratio2));
    assertEquals(kind, 1.0, ratio1/ratio2, close);
  }
  
  // create non-random vector with random perturbations
  Vector denseRandom(int large, Random rnd, int sign) {
    double[] values = new double[large];
    for(int i = 0; i < large; i++) {
      values[i] = rnd.nextDouble() + sign * i;
    }
    return new DenseVector(values);
  }
}
