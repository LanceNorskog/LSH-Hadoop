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

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.Vector.Element;

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
 */

public abstract class RandomProjector {
  
  static public RandomProjector getProjector(boolean sparse) {
    if (sparse)
      return new RandomProjector2of6();
    else
      return new RandomProjectorPlusMinus();
  }
  
  //  public Matrix times(Matrix other) {
  //    int[] c = size();
  //    int[] o = other.size();
  //    if (c[COL] != o[ROW]) {
  //      throw new CardinalityException(c[COL], o[ROW]);
  //    }
  //    Matrix result = like(c[ROW], o[COL]);
  //    for (int row = 0; row < c[ROW]; row++) {
  //      for (int col = 0; col < o[COL]; col++) {
  //        double sum = 0;
  //        for (int k = 0; k < c[COL]; k++) {
  //          sum += getQuick(row, k) * other.getQuick(k, col);
  //        }
  //        result.setQuick(row, col, sum);
  //      }
  //    }
  //    return result;
  //  }
  
  public Vector times(Vector v) {
    int size = v.size();
    Vector w = v.like();
    
    if (v.isDense()) {
      for (int i = 0; i < size; i++) {
        double d = v.get(i);
        if (d != 0) {
          double sum = sumRow(i, size, d);
          if (sum != 0)
            w.setQuick(i, sum);
        } 
      }
    } else {
      Iterator<Element> iter = v.iterateNonZero();
      while(iter.hasNext()) {
        Element e = iter.next();
        int i = e.index();
        double d = e.get();
        if (d != 0) {
          double sum = sumRow(i, size, d);
          if (sum != 0)
            w.setQuick(i, sum);
        } 
      }
    }
    return w;
  }
  
  protected abstract double sumRow(int r, int len, double d);

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
  final private int masks[] = new int[32];
  final private ByteBuffer buf;
  final private int seed;
  
  public RandomProjector2of6() {
    this(RandomUtils.getRandom().nextInt());
  }
  
  public RandomProjector2of6(int seed) {
    this.seed = seed;
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    int mask = 1;
    for(int i = 0; i < 32; i++) {
      masks[i] = mask;
      mask = (mask << 1) | 1;
    }
  }
  
  static double six[] = {1,-1,0,0,0,0};
  
  @Override
  protected double sumRow(int r, int len, double d) {
    double sum = 0;
    //  6^11 < 2^31 < 6^12
    for(int i = 0; i < len; i += 22) {
      long x = MurmurHash.hash64A(buf, (seed + r +i) * len);
      // you cannot modulo a long!
      int z = Math.abs((int) x);
      for(int y = 0; y < 11 && i + y < len; y++) {
        int z6 = z % 6;
        sum += six[z6];
        z /= 6;
      }
      z = Math.abs((int) x>>>32);
      for(int y = 0; y < 11 && i + y < len; y++) {
        int z6 = z % 6;
        sum += six[z6];
        z /= 6;
      }
    }
    return sum * d;
  }

}

/*
 * Create and use 64 random bits, and generate +1/-1 for each cell.
 * Twice as fast as above for dense rows. All returns are non-zero.
 */

class RandomProjectorPlusMinus extends RandomProjector {
  final private int masks[] = new int[32];
  final private ByteBuffer buf;
  final private int seed;
  
  public RandomProjectorPlusMinus() {
    this(RandomUtils.getRandom().nextInt());
  }
  
  public RandomProjectorPlusMinus(int seed) {
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    int mask = 1;
    for(int i = 0; i < 32; i++) {
      masks[i] = mask;
      mask = (mask << 1) | 1;
    }
    this.seed = seed;
  }
  
  @Override
  protected double sumRow(int r, int len, double d) {
    double sum = 0;
    for(int i = 0; i < len; i += 64) {
      long x = MurmurHash.hash64A(buf, (seed + r +i) * len);
      // harvest 64th bit
      if (x > 0)
        sum++;
      else
        sum--;
      // use 1st -> 63rd bit
      x = Math.abs(x);
      for(int b = 0; b < 63 && b + i < len; b++) {
        if ((1<<b & x) != 0)
          sum++;
        else
          sum--;
      }
    }
    return sum * d;
  }
  
}
