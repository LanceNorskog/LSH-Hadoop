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
import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.map.OpenIntDoubleHashMap;

/**
 * Random Projector: efficient implementation of projecting a vector or matrix with a random matrix.  
 * It's much much easier to do this directly, even though it is a kind of matrix.
 * 
 * These classes use a wonderful result from:
 * 
 * Database-friendly random projections: Johnson-Lindenstrauss with binary coins
 * Achlioptas, 2001
 * 
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.84.4546&rep=rep1&type=pdf
 * 
 * @inproceedings{ author = {Dimitris Achlioptas}, title = {Database-friendly
 * random projections}, booktitle = {Symposium on Principles of Database
 * Systems}, year = {2001}, pages = {274--281}, doi = {10.1145/375551.375608},
 * masid = {133250} }
 * 
 * This is deterministic: twice with the same seed gets the same output.
 * Dense and Sparse have exactly the same output.
 */

public abstract class RandomProjector {
  static int ROW = 0;
  static int COL = 1;
  final int[] card = new int[2];
  int zeroes = 0;
  
  static public RandomProjector getProjector(int r, int c, int seed, boolean sparse) {
    if (sparse)
      return new RandomProjector2of6(r, c, seed);
    else
      return new RandomProjectorPlusMinus(r, c, seed);
  }
  
  public Vector times(Vector v) {
    resetSeed();
    
    if (v.isDense()) {
      int size = v.size();
      Vector w = new DenseVector(card[COL]);
      for (int c = 0; c < card[COL]; c++) {
        
        double sum = sumRow(v);
        if (sum != 0) {
          w.setQuick(c, sum);
        } else {
          zeroes++;
        }
        bumpSeed(size);
      }
      return w;
    } else {
      int sparse = v.getNumNondefaultElements();
      int[] indexes = new int[sparse];
      double[] values = new double[sparse];
      getMap(v, indexes, values);
      
      Vector w = new RandomAccessSparseVector(card[COL]);
      for (int c = 0; c < card[COL]; c++) {
        double sum = sumRow(indexes, values);
        if (sum != 0) {
          w.setQuick(c, sum);
        } else {
          zeroes++;
        }
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
  
  protected abstract void bumpSeed(int size);
  
  protected abstract void resetSeed();
  
}

/*
 * 
 * Random projection does not require a full-range random number in each cell.
 * It only requires either a random +1/-1 in every cell, OR {+1, -1, 0, 0, 0, 0}
 * These two classes implement Achlioptas's results.
 * 
 * These classes generate random bits via MurmurHash and use them to generate
 * +1/-1 and 2/6. You cannot do modulo on a long, only an int. 6^11 < 2^31 <
 * 6^12, and so we pull 11 6's from the high int and 11 6's from the low int.
 * 
 * Use this with Sparse data, as it has a better (but still small) chance of returning 0.
 */

class RandomProjector2of6 extends RandomProjector {
  //  final private int masks[] = new int[32];
  final private ByteBuffer buf;
  private int seed;
  final private int originalSeed;
  
  public RandomProjector2of6(int r, int c, int seed) {
    card[ROW] = r;
    card[COL] = c;
    this.originalSeed = seed;
    this.seed = seed;
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
  }
  
  static double six[] = {1,-1,0,0,0,0};
  
  @Override
  protected double sumRow(Vector v) {
    double sum = 0;
    //  6^11 < 2^31 < 6^12
    int length = v.size();
    for(int i = 0; i < length; i += 24) {
      long x = MurmurHash.hash64A(buf, seed + i);
      // you cannot modulo a long!
      // 6^24 < 2^63 < 6^25.
      // 
      long z = Math.abs(x);
      for(int y = 0; y < 24 && i + y < length; y++) {
        int z6 = (int) (z % 6);
        sum += six[z6] * v.getQuick(i + y);
        z /= 6;
      }
    }
    return sum;
  }
  
  @Override
  protected double sumRow(int[] indexes, double[] values) {
    double sum = 0;
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      int offset = index % 24;
      int block = index - offset;
      long x = MurmurHash.hash64A(buf, seed + block);
      x = Math.abs(x);
      int z6 = (int)((x / (6 * offset)) % 6);
      sum += six[z6] * values[i];
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
 * Twice as fast as above for dense rows. All returns are non-zero.
 */

class RandomProjectorPlusMinus extends RandomProjector {
  final private int masks[] = new int[32];
  final private ByteBuffer buf;
  final private int origSeed;
  private int seed;
  
  public RandomProjectorPlusMinus(int r, int c, int seed) {
    card[ROW] = r;
    card[COL] = c;
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    int mask = 1;
    for(int i = 0; i < 32; i++) {
      masks[i] = mask;
      mask = (mask << 1) | 1;
    }
    this.origSeed = seed;
    this.seed = seed;
  }
  
  @Override
  protected double sumRow(Vector v) {
    int length = v.size();
    double sum = 0;
    for(int i = 0; i < length; i += 16) {
      long x = MurmurHash.hash64A(buf, seed + i);
      // harvest 64th bit
      //      if (x > 0)
      //        sum++;
      //      else
      //        sum--;
      // use 1st -> 63rd bit
      x = Math.abs(x);
      for(int b = 0; b < 16 && b + i < length; b++) {
        if (((1<<b) & x) != 0)
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
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      int bit = index % 16;
      int block = index - bit;
      long x = Math.abs(MurmurHash.hash64A(buf, seed + block));
      int mask = 1 << bit;
      if ((x & mask) != 0) {
        sum += values[i];
      } else {
        sum -= values[i];
        zeroes++;
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

class RandomProjectorJava extends RandomProjector {
  protected Random rnd = new MurmurHashRandom(0);
  private final int origSeed;
  private int seed;
  
  RandomProjectorJava(int r, int c, int seed) {
    super.card[ROW] = r;
    super.card[COL] = c;
    origSeed = seed;
    this.seed = seed;
  }
  
  @Override
  protected double sumRow(Vector v) {
    double sum = 0;
    for(int i = 0;i < v.size(); i ++) {
      rnd.setSeed(seed + i);
      long next = rnd.nextLong();
      rnd.setSeed(next);
      sum += v.getQuick(i) / rnd.nextDouble();
    }
    
    return sum;
  }
  
  @Override
  protected double sumRow(int[] indexes, double[] values) {
    double sum = 0;
    for(int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (values[i] != 0) {
        rnd.setSeed(seed + index);
        long next = rnd.nextLong();
        rnd.setSeed(next);
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
