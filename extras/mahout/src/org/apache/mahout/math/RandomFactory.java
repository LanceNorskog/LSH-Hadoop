package org.apache.mahout.math;

import java.util.Date;
import java.util.Random;

/*
 * Generate random objects using one seed stream to avoid correlations between objects.
 * Otherwise vector[2] is the same seed as matrix[1,1].
 */
public class RandomFactory {
  private long currentSeed;
  private final Random rnd;

  public RandomFactory() {
    this(new Date().getTime());
  }

  public RandomFactory(long seed) {
    rnd = new Random(seed);
    currentSeed = seed + 1;
  }

  public long nextSeed() {
    return ++currentSeed;
  }

  public long nextSeed(int size) {
    currentSeed += size;
    return currentSeed;
  }

  public long nextLong() {
    return rnd.nextLong();
  }

  public double nextDouble() {
    return rnd.nextDouble();
  }

  public double nextGaussian() {
    return rnd.nextGaussian();
  }

  /*
   * normalize and clamp to 0 -> 1 to match nextDouble()
   */
  public double nextGaussian01() {
    while(true) {
      double d = rnd.nextGaussian()/6;
      if (d > 0.5 || d < -0.5) {
        continue;
      }
      return d + 0.5;
    }
  }

  public Random getRandom() {
    return new Random(nextSeed());
  }

  public Vector getVector(int size, int mode) {
    long seed = nextSeed(size);
    return new RandomVector(size, seed, 1, mode);
  }

  public Matrix getMatrix(int rows, int columns, int mode, Matrix cache) {
    long seed = nextSeed(rows * columns);
    Matrix m = new RandomMatrix(rows, columns, seed, mode, cache);
    return m;
  }

  public void setSeed(long seed) {
    currentSeed = seed;
  }

  public void resetSeed() {
    currentSeed = new Date().getTime();
  }
}
