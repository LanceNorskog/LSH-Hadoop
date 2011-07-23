package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Benchmark RandomProjector algorithms, including simple versions.
 */

public class RandomProjectorBenchmark {
  
  public static void main(String[] args) {
    RandomProjectorBenchmark benchmark = new RandomProjectorBenchmark();
    benchmark.benchmarkAll();
  }
  
  public void benchmarkAll() {
    runAlg(new RandomProjectorJDK(), "JDK: ");
    runAlg(new RandomProjectorLinear(), "Mersenne Twister: ");
    runAlg(new RandomProjectorPlusminus(), "+1/-1 (MurmurHash): ");
    runAlg(new RandomProjectorSqrt3(), "Sqrt3 (MurmurHash): ");
    runAlg(new RandomProjector2of6(), "Sqrt3 (optimized MmH): ");
    runAlg(new RandomProjectorPlusMinus(), "+1/-1 (optimized MmH): ");
    System.out.println("\t all times in ms");
  }

  private void runAlg(RandomProjector rp, String kind) {
    long time;
    Random datagen = RandomUtils.getRandom(0);
    time = projectVector(rp, datagen);
    time += projectVector(rp, datagen);
    time += projectVector(rp, datagen);
    time += projectVector(rp, datagen);
    System.out.println(kind + time);
  }
  
  long projectVector(RandomProjector rp, Random rnd) {
    int large = 2000;
    
    double[] values = new double[large];
    for(int i = 0; i < large; i++) {
      values[i] = rnd.nextDouble();
    }
    Vector v1 = new DenseVector(values);
    long start = System.currentTimeMillis();
    rp.times(v1);
    return System.currentTimeMillis() - start;
  }
  
}

/*
 * These are simple implementations using various random algorithms.
 */

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

class RandomProjectorPM extends RandomProjector {
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

