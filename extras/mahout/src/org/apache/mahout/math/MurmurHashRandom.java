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
import java.util.Random;

import org.apache.mahout.math.MurmurHash;

/*
 * MurmurHash implementation of Java.lang.Random.
 * Random moves forward an int at a time, 
 * and MurmurHash generates a long,
 * so return the long one int at a time.
 * Passes simple randomness tests, but not verified.
 */
public class MurmurHashRandom extends Random {
  private int masks[] = new int[32];
  
  private static final long serialVersionUID = 1L;
  private int murmurSeed[] = new int[2];
  private int counter = 0;
  private ByteBuffer buf;
  
  // lifted from parent
  public MurmurHashRandom() {
    this (++seedUniquifier + System.nanoTime());
  }
  
  public MurmurHashRandom(long seed) {
    int mask = 1;
    for(int i = 0; i < 32; i++) {
      masks[i] = mask;
      mask = (mask << 1) | 1;
    }
    setMurmurHashSeed(seed);
  }
  
  private static volatile long seedUniquifier = 8682522807148012L;;
  
  @Override
  public void setSeed(long seed) {
    setMurmurHashSeed(seed);
  }

  private void setMurmurHashSeed(long seed) {
    // if these are in the constructors, they have not yet happened. Weird.
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    murmurSeed = new int[2];
    murmurSeed[0] = (int) MurmurHash.hash64A(buf, (int) (seed >> 32));
    murmurSeed[1] = (int) MurmurHash.hash64A(buf, (int) seed);
    counter = 0;
  }
  
  @Override
  protected int next(int bits) {
    if (counter++ % 2 == 0) {
      int shortcut = (int) (murmurSeed[0] & ((1L << bits) -1));
      return shortcut;
    }
    int value = murmurSeed[1];
    long dual = MurmurHash.hash64A(buf, murmurSeed[0] ^ murmurSeed[1]);
    murmurSeed[0] = (int) dual;
    murmurSeed[1] = (int) (dual >> 32);
    return (int) (value & ((1L << bits) -1));
  }
  
}
