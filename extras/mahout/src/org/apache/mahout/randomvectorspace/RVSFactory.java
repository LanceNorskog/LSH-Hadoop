package org.apache.mahout.randomvectorspace;

import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.RandomVector;
import org.apache.mahout.math.Vector;

/*
 * Random Vector Space: generate a set of random vectors, defining a space.
 * Quantize a vector by taking the cosine distance to each random vector.
 * Save the sign of the cosine distance. 
 * 
 * Where the hell did I find this?
 */

public class RVSFactory  {
  
  private int nVectors;
  private int dimensions;
  private int seed;
  private Random rnd;
  private RandomVector[] space;
  private DistanceMeasure measure = new CosineDistanceMeasure();
  
  public RVSFactory(int nVectors, int dimensions, int seed) throws Exception {
    if (nVectors > 63) {
      throw new Exception("Up to 63 random dimensions supported");
    }
    this.nVectors = nVectors;
    this.dimensions = dimensions;
    this.seed = seed;
    this.rnd = RandomUtils.getRandom(seed);
    space = new RandomVector[nVectors];
    for(int i = 0; i < nVectors; i++) {
      space[i] = new RandomVector(dimensions, seed + i * dimensions, false);
    }
  }
  
  public RVS quantize(Vector value) {
    RVS position = new RVS();
    char mask = 0;
    for(int i = 0; i < nVectors; i++) {
      double c = measure.distance(value, space[i]);
      if (c >= 0)
        mask |= 1<<i;
    }
    position.bitSet = mask;
    return position;
  }
   
   static public void main(String[] args) {
     
   }


  
}
