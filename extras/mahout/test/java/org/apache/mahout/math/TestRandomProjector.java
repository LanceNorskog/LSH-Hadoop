package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Test;

public class TestRandomProjector extends MahoutTestCase {
  
  @Test
  public void TestMatrix() {
    
  }
  
  @Test
  public void TestVectorSmall() {
    Vector v = new DenseVector(10);
    v.set(0, 3);
    v.set(1, 3);
    v.set(2, 3);
    v.set(3, 3);
    v.set(4, 3);
    v.set(5, 3);
    RandomProjectorConcept rp = new RandomProjectorLinear();
    Vector w = rp.times(v);
    w.hashCode();
  }
  
  @Test
  public void TestVectorLarge() {
    RandomProjectorConcept rp = null;
    long time;
    rp = new RandomProjectorJDK();
    time = checkRatios(rp);
    System.out.println("JDK: " + time);
    rp = new RandomProjectorLinear();
    time = checkRatios(rp);
    System.out.println("Mersenne Twister: " + time);
    rp = new RandomProjectorPlusminus();
    time = checkRatios(rp);
    System.out.println("+1/-1 (MurmurHash): " + time);
    rp = new RandomProjectorSqrt3();
    time = checkRatios(rp);
    System.out.println("Sqrt3 (MurmurHash): " + time);
    rp = new RandomProjectorAch();
    time = checkRatios(rp);
    System.out.println("Sqrt3 (optimized MmH): " + time);
    System.out.println("\t all times in ms");
  }
  
  long checkRatios(RandomProjectorConcept rp) {
    long time = 0;
    int large = 1000;
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double[] d1 = new double[large];
    double[] d2 = new double[large];
    Random rnd = new Random(0);
    for(int i = 0; i < large; i++) {
      d1[i] = rnd.nextDouble();
      d2[i] = rnd.nextDouble();
    }
    Vector v1 = new DenseVector(d1);
    Vector v2 = new DenseVector(d2);
    double cosreal1 = measure.distance(v1, v2);
    long start = System.currentTimeMillis();
    Vector w1 = rp.times(v1);
    Vector w2 = rp.times(v2);
    time += (System.currentTimeMillis() - start);
    double cosrp1 = measure.distance(w1, w2);
    double ratio1 = cosrp1 / cosreal1;
    for(int i = 0; i < large; i++) {
      d1[i] = rnd.nextDouble();
      d2[i] = rnd.nextDouble();
    }
    v1 = new DenseVector(d1);
    v2 = new DenseVector(d2);
    double cosreal2 = measure.distance(v1, v2);
    start = System.currentTimeMillis();
    w1 = rp.times(v1);
    w2 = rp.times(v2);
    time += (System.currentTimeMillis() - start);
    double cosrp2 = measure.distance(w1, w2);
    double ratio2 = cosrp2 / cosreal2;
//    System.out.println("r1 v.s. r2: " + ratio1 + "," + ratio2);
    double maxRatio = Math.max(ratio1, ratio2);
    assertEquals(ratio1/maxRatio, ratio2/maxRatio, 0.1);
    return time;
  }
  
}
