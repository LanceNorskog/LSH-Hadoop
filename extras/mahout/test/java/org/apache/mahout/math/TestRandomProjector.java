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
  public void TestVectorSmall() {
    Vector v = new DenseVector(10);
    v.set(0, 3);
    v.set(1, 3);
    v.set(2, 3);
    v.set(3, 3);
    v.set(4, 3);
    v.set(5, 3);
    RandomProjector rp = new RandomProjectorLinear();
    Vector w = rp.times(v);
    w.hashCode();
  }
  
  @Test
  public void TestVectorLarge() {
    RandomProjectorConcept rp = null;
    runAlg(new RandomProjectorJDK(), "JDK: ");
    runAlg(new RandomProjectorLinear(), "Mersenne Twister: ");
    runAlg(new RandomProjectorPlusminus(), "+1/-1 (MurmurHash): ");
    runAlg(new RandomProjectorSqrt3(), "Sqrt3 (MurmurHash): ");
    runAlg(new RandomProjectorAch(), "Sqrt3 (optimized MmH): ");
    runAlg(new RandomProjectorPM_murmur(), "+1/-1 (optimized MmH): ");
    System.out.println("\t all times in ms");
  }

  private void runAlg(RandomProjector rp, String kind) {
    long time;
    Random datagen = RandomUtils.getRandom(0);
    time = checkRatios(rp, datagen);
    time += checkRatios(rp, datagen);
    time += checkRatios(rp, datagen);
    time += checkRatios(rp, datagen);
    System.out.println(kind + time);
  }
  
  long checkRatios(RandomProjector rp, Random rnd) {
    long time = 0;
    int large = 200;
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double[] d1 = new double[large];
    double[] d2 = new double[large];
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
    System.out.println("r1 v.s. r2: " + (ratio2/ratio1));
//    assertEquals(1.0, ratio2/ratio1, 0.1);
    return time;
  }
  
}


class RandomProjectorJDK extends RandomProjector {
  protected Random rnd = new MurmurHashRandom(0);
  
  double sumRow(int r, int len, double d) {
    double sum = 0;
    for(int i = 0; i < len; i ++) {
      sum += d * rnd.nextDouble();
    }
    return sum;
  }
}

class RandomProjectorLinear extends RandomProjector {
  protected Random rnd = new Random(0);
  
  double sumRow(int r, int len, double d) {
    Random rnd = org.apache.mahout.common.RandomUtils.getRandom(r * 100000);
    double sum = 0;
    for(int i = 0; i < len; i ++) {
      sum += d * rnd.nextDouble();
    }
    return sum;
  }
}


class RandomProjectorSqrt3 extends RandomProjector {
  protected Random rnd = new MurmurHashRandom(0);
  
  double sumRow(int r, int len, double d) {
    rnd.setSeed(r * 100000);
    double sum = 0;
    for(int i = 0; i < len; i ++) {
      int x = rnd.nextInt(6);
      if (x == 0)
        sum += d;
      else if (x == 1)
        sum -= d;
    }
    return sum * Math.sqrt(3);
  }
  
}

class RandomProjectorPlusminus extends RandomProjector {
  protected Random rnd = new MurmurHashRandom(0);
  
  double sumRow(int r, int len, double d) {
    rnd.setSeed(r * 100000);
    double sum = 0;
    for(int i = 0; i < len; i ++) {
      int x = rnd.nextInt(2);
      if (x == 0)
        sum += d;
      else if (x == 1)
        sum -= d;
    }
    return sum;
  }
  
}
