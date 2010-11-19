package org.apache.mahout.math;

import java.util.Date;
import java.util.Random;

/*
 * Generate random objects using one seed stream to avoid correlations between objects.
 * Otherwise vector[2] is the same seed as matrix[1,1].
 */
public class RandomFactory {
  long currentSeed;
  Random rnd;
  
  RandomFactory() {
    this(new Date().getTime());
  }
  
  RandomFactory(long seed) {
    currentSeed = seed;
    rnd = new Random(seed);
  }

  long nextSeed() {
    return ++currentSeed;
  }
  
  long nextSeed(int size) {
    currentSeed += size;
    return currentSeed;
  }
  
  long nextLong() {
    return 0;
  }
  
  double nextDouble() {
    return 0;
  }
  
  double nextGaussian() {
    return 0;
  }
  
  Random getRandom() {
    return new Random(nextSeed());
  }
  
  Vector getVector(int size, int mode) {
    currentSeed += size;
    return new RandomVector(size, currentSeed, 1, mode);
  }
  
  Matrix getMatrix(int rows, int columns, int mode) {
    currentSeed += rows * columns;
    Matrix m = new RandomMatrix(rows, columns, currentSeed, mode);
    return m;
  }
  
}
