///**
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.mahout.math;
//
//import java.nio.ByteBuffer;
//import java.util.Random;
//
//import org.apache.commons.lang.math.RandomUtils;
//
///**
// * Random Projector: efficient implementation of projecting a vector or matrix with a random matrix.  
// * It's much much easier to do this directly, even though it is a kind of matrix.
// * Uses MurmurHash code directly for hyper-efficient 
// */
//
//public class RandomProjector {
//  // To assist sparsity, absolute values smaller get dropped
//  private int masks[] = new int[32];
//  private ByteBuffer buf;
//  
//  public RandomProjector() {
//    setupMurmurHash();
//  }
//  
//  //  public Matrix times(Matrix other) {
//  //    int[] c = size();
//  //    int[] o = other.size();
//  //    if (c[COL] != o[ROW]) {
//  //      throw new CardinalityException(c[COL], o[ROW]);
//  //    }
//  //    Matrix result = like(c[ROW], o[COL]);
//  //    for (int row = 0; row < c[ROW]; row++) {
//  //      for (int col = 0; col < o[COL]; col++) {
//  //        double sum = 0;
//  //        for (int k = 0; k < c[COL]; k++) {
//  //          sum += getQuick(row, k) * other.getQuick(k, col);
//  //        }
//  //        result.setQuick(row, col, sum);
//  //      }
//  //    }
//  //    return result;
//  //  }
//  
//  public Vector times(Vector v) {
//    int size = v.size();
//    Vector w = v.like();
//    
//    for (int i = 0; i < size; i++) {
//      double d = v.get(i);
//      double sum = sumRow(i, size, d);
////      if (Math.abs(sum) > EPSILON)
//        w.setQuick(i, sum);
//    }
//    return w;
//  }
//
//  double six[] = {1,-1,0,0,0,0};
//
//  /*
//   *  Every call to MurmurHash returns a random long.
//   *  This harvests 10 6-ary values 
//   */
//  double sumRow(int r, int len, double d) {
//    double sum = 0;
//    for(int i = 0; i < len; i += 10) {
//      // 10 * 6 == 60
//      long x = MurmurHash.hash64A(buf, (r +i) * len);
//      if (x < 0)
//        x = -x;
//      // you cannot modulo a long!
//      int z = Math.abs((int) x);
//      for(int y = 0; y < 5 && i + y < len; y++) {
//        int z6 = z % 6;
//        sum += six[z6] * d;
//        z /= 6;
//      }
//      for(int y = 0; y < 5 && i + y < len; y++) {
//        int z6 = z % 6;
//        sum += six[z6] * d;
//        z /= 6;
//      }
//    }
//    return sum * d * Math.sqrt(3);
//  }
//  
//
//  
//  private void setupMurmurHash() {
//    byte[] bits = new byte[8];
//    buf = ByteBuffer.wrap(bits);
//    int mask = 1;
//    for(int i = 0; i < 32; i++) {
//      masks[i] = mask;
//      mask = (mask << 1) | 1;
//    }
//  }
//
//  
//}
//
///*
//class RandomProjectorJDK extends RandomProjector {
//  protected Random rnd = new MurmurHashRandom(0);
//
//  double sumRow(int r, int len, double d) {
//    double sum = 0;
//    for(int i = 0; i < len; i ++) {
//      sum += d * rnd.nextDouble();
//    }
//    return sum;
//  }
//}
//*/
//
//class RandomProjectorAch extends RandomProjector {
//  private int masks[] = new int[32];
//  private ByteBuffer buf;
//
//  public RandomProjectorAch() {
//    byte[] bits = new byte[8];
//    buf = ByteBuffer.wrap(bits);
//    int mask = 1;
//    for(int i = 0; i < 32; i++) {
//      masks[i] = mask;
//      mask = (mask << 1) | 1;
//    }
//  }
//  
//  double six[] = {1,-1,0,0,0,0};
//  int buckets[] = {0,0,0,0,0,0};
//  int buckets2[] = {0,0,0,0,0,0};
//  
//  double sumRow(int r, int len, double d) {
//    double sum = 0;
//    for(int i = 0; i < len; i += 10) {
//      // 10 * 6 == 60
//      long x = MurmurHash.hash64A(buf, (r +i) * len);
//      if (x < 0)
//        x = -x;
//      // you cannot modulo a long
//      int z = Math.abs((int) x);
//      for(int y = 0; y < 5 && i + y < len; y++) {
//        int z6 = z % 6;
//        buckets[z6]++;
//        sum += six[z6] * d;
//        z /= 6;
//      }
//      for(int y = 0; y < 5 && i + y < len; y++) {
//        int z6 = z % 6;
//        buckets2[z6]++;
//        sum += six[z6] * d;
//        z /= 6;
//      }
//    }
//    return sum * d * Math.sqrt(3);
//  }
//  
//
//}
