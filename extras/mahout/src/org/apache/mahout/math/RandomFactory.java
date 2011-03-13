package org.apache.mahout.math;

import java.util.Random;

/*
 * Problem: a series of random generators could have correlations.
 * This class generates a series of unique random number generators of various
 * distributions that are guaranteed to have unique seeds.
 */

public class RandomFactory {

  public enum Distribution { LINEAR, GAUSSIAN};

  private int randomSeed;

  public RandomFactory(int randomSeed) {
    this.randomSeed = randomSeed;
  }

  public Random getRandom(int cardinality, Distribution distribution) {
    int seed = nextSeed(cardinality);
    switch(distribution)  {
    case LINEAR: return new Random(seed);
    case GAUSSIAN: return new RandomGaussian(seed);
    }
    Random rnd = new Random(seed);
    return rnd;
  }

  private int nextSeed(int cardinality) {
    int seed = randomSeed; 
    randomSeed = randomSeed + cardinality;
    return seed;
  }

}

 class RandomGaussian extends Random {
   
   public RandomGaussian(int seed) {
    super(seed);
  }

  @Override
   public double nextDouble() {
     return nextGaussian();
   }
 }