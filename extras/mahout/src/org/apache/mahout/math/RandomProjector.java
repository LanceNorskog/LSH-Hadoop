/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.math;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.Vector.Element;

/**
 * Random Projector: efficient implementation of projecting a vector or matrix with a random matrix.  
 * 
 * Determinism contract:
 *     1) the same seed does the same operation.
 *     2) the same value is returned for sparse and dense inputs 
 *      
 * These classes use a wonderful result from:
 * 
 *    Database-friendly random projections: Johnson-Lindenstrauss with binary coins
 *      Achlioptas, 2001
 *    http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.84.4546&rep=rep1&type=pdf
 * 
 * @inproceedings{ author = {Dimitris Achlioptas}, title = {Database-friendly
 * random projections}, booktitle = {Symposium on Principles of Database
 * Systems}, year = {2001}, pages = {274--281}, doi = {10.1145/375551.375608},
 * masid = {133250} }
 *     
 * These classes use MurmurHash directly to generate multiple random values.
 * Three implementations:
 *     2of6: uses the +1,-1,0,0,0,0 result in Achlioptas.
 *         Used for Dense as it generates 64 values per batch
 *     PlusMinus: uses the +1/-1 result in Achlioptas.
 *         Used for Sparse as it many more 0-valued outputs
 *     JDK: uses the Java JDK random generator as a reference implementation.
 *         Just for experiments.
 *         
 *  The sparse implementation caches the positions and values.
 */

public abstract class RandomProjector {
  
  static public RandomProjector getProjector(boolean sparse) {
    int seed = RandomUtils.getRandom().nextInt();
    return getProjector(seed, sparse); 
  }
  
  static public RandomProjector getProjector(int seed, boolean sparse) {
    new RandomProjectorMersenne(seed);
    if (sparse)
      return new RandomProjector2of6(seed);
    else
      return new RandomProjectorPlusMinus(seed);
  }
  
  public Vector times(Vector v, int resultSize) {
    resetSeed();
    
    if (v.isDense()) {
      int size = v.size();
      Vector w = new DenseVector(resultSize);
      for (int c = 0; c < resultSize; c++) {
        
        double sum = sumRow(v);
        if (sum != 0) {
          w.setQuick(c, sum);
        } 
        bumpSeed(size);
      }
      return w;
    } else {
      int sparse = v.getNumNondefaultElements();
      int[] indexes = new int[sparse];
      double[] values = new double[sparse];
      getMap(v, indexes, values);
      
      Vector w = new RandomAccessSparseVector(resultSize);
      for (int c = 0; c < resultSize; c++) {
        double sum = sumRow(indexes, values);
        w.setQuick(c, sum);
        bumpSeed(v.size());
      }
      return w;
    }
  }
  
  private void getMap(Vector v, int[] indexes, double[] values) {
    Iterator<Element> iter = v.iterateNonZero();
    int index = 0;
    while(iter.hasNext()) {
      Element e = iter.next();
      int i = e.index();
      indexes[index] = i;
      values[index] = e.get();
      index++;
    }
    if (index != indexes.length)
      throw new IllegalStateException("Vector class " + v.getClass().getCanonicalName() + " does not do getNumNonDefaultElements correctly");
  }
  
  protected abstract double sumRow(int[] indexes, double[] values);
  
  protected abstract double sumRow(Vector v);
  
  protected abstract void bumpSeed(int bump);
  
  protected abstract void resetSeed();
  
}

/*
 * Generate +1,-1,0,0,0,0 as multipliers
 * Use this with Sparse data, as it has a much higher chance of returning 0.
 */

class RandomProjector2of6 extends RandomProjector {
  final private ByteBuffer buf;
  private int seed;
  final private int originalSeed;
  
  public RandomProjector2of6(int seed) {
    this.originalSeed = seed ^ 0xc6a4a793;
    this.seed = originalSeed;
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    MurmurHash.hash64A(buf, seed);    // burp MurmurHash- setSeed doesn't work the first time
  }
  
  static double six[] = {1,-1,0,0,0,0};
  
  @Override
  protected double sumRow(Vector v) {
    double sum = 0;
    //  6^24 < 2^63 < 6^25
    int length = v.size();
    //    System.out.println("sumRows dense: ");
    for(int index = 0; index < length; index += 24) {
      long x = MurmurHash.hash64A(buf, seed + index);
      long z = Math.abs(x);
      for(int offset = 0; offset < 24 && index + offset < length; offset++) {
        int z6 = (int) (z % 6L);
        //        if (v.getQuick(index + offset) > 0)
        //          System.out.println("\t" + (index + offset) + ": " + z6 + ", out of: " + z);
        sum += six[z6] * v.getQuick(index + offset);
        z /= 6;
      }
    }
    return sum;
  }
  
  @Override
  protected double sumRow(int[] indexes, double[] values) {
    double sum = 0;
    //    System.out.println("sumRow sparse: ");
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      int offset = index % 24;
      int block = (index / 24) * 24;
      
      long x = MurmurHash.hash64A(buf, seed + block);
      long z = Math.abs(x);
      //      System.out.println("\t offset:" + offset + ", block: " + block + ", z: " + z);
      while (offset > 0) {
        z /= 6;
        offset--;
      }
      int z6 = (int) (z % 6L);
      //      System.out.println("\t" + index + ": " + z6 + ", out of: " + z);
      sum += six[(int) z6] * values[i];
    }
    return sum;
  }
  
  @Override
  protected void bumpSeed(int size) {
    seed += size;
  }
  
  @Override
  protected void resetSeed() {
    seed = originalSeed;
  }
  
}

/*
 * Create and use 64 random bits, and generate +1/-1 for each cell.
 */

class RandomProjectorPlusMinus extends RandomProjector {
  final private ByteBuffer buf;
  final private int origSeed;
  private int seed;
  
  public RandomProjectorPlusMinus(int seed) {
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    this.origSeed = seed ^ 0xc6a4a793;
    this.seed = origSeed;
    MurmurHash.hash64A(buf, seed);    // burp MurmurHash - see above
  }
  
  @Override
  protected double sumRow(Vector v) {
    int length = v.size();
    double sum = 0;
    for(int i = 0; i < length; i += 64) {
      long bits = MurmurHash.hash64A(buf, seed + i);
      for(int b = 0; b < 64 && b + i < length; b++) {
        if ((bits & (1<<b)) != 0)
          sum += v.getQuick(i + b);
        else
          sum -= v.getQuick(i + b);
      }
    }
    return sum;
  }
  
  @Override
  protected double sumRow(int[] indexes, double[] values) {
    double sum = 0;
    int cached = 0;
    int block = -1;
    long bits = 0;
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      int bit = index % 64;
      if (block != index - bit) {
        block = index - bit;
        bits = MurmurHash.hash64A(buf, seed + block);
      } else {
        cached++;
      }
      if ((bits & (1 << bit)) != 0) {
        sum += values[i];
      } else {
        sum -= values[i];
      }
    }
    if (cached > 2)
      System.out.println("Cached: " + cached);
    return sum;
  }
  
  @Override
  protected void bumpSeed(int size) {
    seed += size;
  }
  
  @Override
  protected void resetSeed() {
    seed = origSeed;    
  }
  
}

class RandomProjectorMersenne extends RandomProjector {
  protected RandomGenerator rnd = new MersenneTwister();
  private final int origSeed;
  private int seed;
  
  RandomProjectorMersenne(int seed) {
    origSeed = seed;
    this.seed = seed;
  }
  
  @Override
  protected double sumRow(Vector v) {
    double sum = 0;
    rnd.setSeed(seed);
    for(int i = 0;i < v.size(); i ++) {
      //      rnd.setSeed(seed + i);
      sum += v.getQuick(i) / rnd.nextDouble();
    }
    
    return sum;
  }
  
  @Override
  protected double sumRow(int[] indexes, double[] values) {
    double sum = 0;
    rnd.setSeed(seed);
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (values[i] != 0) {
        //        rnd.setSeed(seed + index);
        sum += values[i] / rnd.nextDouble();
      }
    }
    return sum;
  }
  
  @Override
  protected void bumpSeed(int size) {
    seed += size;
  }
  
  @Override
  protected void resetSeed() {
    seed = origSeed;
  }
  
}
