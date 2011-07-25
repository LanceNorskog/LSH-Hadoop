package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;

/*
 * Benchmark RandomProjector algorithms, including simple versions.
 */

public class RandomProjectorBenchmark {
  static int LARGE = 1000;
  static int SPARSE = 10;
  
  public static void main(String[] args) {
    RandomProjectorBenchmark benchmark = new RandomProjectorBenchmark();
    benchmark.benchmarkAll();
  }
  
  public void benchmarkAll() {
    if (LARGE < 1000)
      runAlg(new RandomProjectorMersenne(0), "Mersenne: ");
    runAlg(new RandomProjectorJDK(0), "JDK (optimized MmH): ");
    runAlg(new RandomProjector2of6(0), "2of6 (optimized MmH): ");
    runAlg(new RandomProjectorPlusMinus(0), "+1/-1 (optimized MmH): ");
    System.out.println("\t all times in ms");
  }

  private void runAlg(RandomProjector rp, String kind) {
    long time;
    Random datagen = RandomUtils.getRandom(0);
    time = projectVectorDense(rp, datagen);
    time += projectVectorDense(rp, datagen);
    time += projectVectorDense(rp, datagen);
    time += projectVectorDense(rp, datagen);
    System.out.println(kind + "dense: " + time);
    time = projectVectorSparse(rp, datagen);
    time += projectVectorSparse(rp, datagen);
    time += projectVectorSparse(rp, datagen);
    time += projectVectorSparse(rp, datagen);
    System.out.println(kind + "sparse: " + time);
  }
  
  long projectVectorDense(RandomProjector rp, Random rnd) {
    double[] values = new double[LARGE];
    for(int i = 0; i < LARGE; i++) {
      values[i] = rnd.nextDouble();
    }
    Vector v1 = new DenseVector(values);
    long start = System.currentTimeMillis();
    rp.times(v1, 10);
    return System.currentTimeMillis() - start;
  }
  
  long projectVectorSparse(RandomProjector rp, Random rnd) {
    double[] values = new double[SPARSE];
    for(int i = 0; i < SPARSE; i++) {
      values[i] = rnd.nextDouble();
    }
    Vector v1 = new RandomAccessSparseVector(LARGE);
    for(int i = 0; i < SPARSE; i++) {
      v1.setQuick(rnd.nextInt(LARGE), values[i]);
    }
    long start = System.currentTimeMillis();
    Vector v = rp.times(v1, LARGE);
//    System.out.println("Density: " + v.getNumNondefaultElements());
    return System.currentTimeMillis() - start;
  }
  
}

