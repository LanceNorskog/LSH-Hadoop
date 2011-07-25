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

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.vectorizer.encoders.MurmurHash;


/*
 * MurmurHash implementation of Java.lang.Random. 
 * Not as good as MersenneTwister, but MT does not
 * do setSeed() gracefully.
 * 
 * Use this only if you want to do setSeed().
 */
public class MurmurHashRandom extends Random {
  private static final long serialVersionUID = 1L;

  private int murmurSeed[] = new int[2];
  private int counter = 0;
  private ByteBuffer buf;
  
  // lifted from parent
  public MurmurHashRandom() {
    this (RandomUtils.getRandom().nextLong());
  }
  
  // MurmurHash gives nothing but 0 if you give 0 as the seed
  private static volatile long seedUniquifier = 8682522807148012L;;
  
  public MurmurHashRandom(long seed) {
    setMurmurHashSeed(seed + seedUniquifier);
  }
  
  @Override
  public void setSeed(long seed) {
    setMurmurHashSeed(seed + seedUniquifier);
  }
  
  // if a zero goes in, MurmurHash becomes zero permanently.
  private void setMurmurHashSeed(long seed) {
    byte[] bits = new byte[8];
    buf = ByteBuffer.wrap(bits);
    // MurmurHash needs to be burped when it gets a new byte array
    MurmurHash.hash64A(buf, (int) seedUniquifier);   
    murmurSeed = new int[2];
    seed += seedUniquifier;
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
