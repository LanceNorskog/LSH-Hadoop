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

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.ReadOnlyVector;

import com.google.common.primitives.Longs;

/**
 * 
 * Vector with repeatable random values.
 *
 * Can only use java.util.Random as generator from RandomUtils does not honor setSeed()
 * 
 * Change to use MurmurHash projectors directly!
 */
public class RandomVector extends ReadOnlyVector {
  
  final private Random rnd ;
  final private long baseSeed;
  final boolean gaussian;
  
  // required for serialization
  public RandomVector() {
    super(0);
    baseSeed = 0;
    gaussian = false;
    rnd = RandomUtils.getRandom();
  }
  
  /*
   * @param size
   * @param seed
   * @param gaussian
   */
  public RandomVector(int size, long seed, boolean gaussian) {
    super(size);
    baseSeed = seed;
    this.gaussian = gaussian;
    rnd = RandomUtils.getRandom(seed);
  }
  
  /*
   * @param size
   * @param rnd
   */
  public RandomVector(int size, Random rnd, boolean gaussian) {
    super(size);
    this.rnd = rnd;
    baseSeed = rnd.nextLong();
    this.gaussian = gaussian;
  }
  
  public boolean isDense() {
    return true;
  }
  
  public boolean isSequentialAccess() {
    return false;
  }
  
public int getNumNondefaultElements() {
    return size();
  }
  
  public double getQuick(int index) {
    rnd.setSeed(getSeed(index));
    return gaussian ? rnd.nextGaussian() : rnd.nextDouble();
  }
  
  private long getSeed(int index) {
    return baseSeed + index;
  }
  
  public Iterator<Element> iterateNonZero() {
    return new AllIterator(this);
  }
  
  public Iterator<Element> iterator() {
    return new AllIterator(this);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o.getClass() == RandomVector.class) {
      RandomVector r = (RandomVector) o;
      return size() == r.size() && baseSeed == r.baseSeed && gaussian == r.gaussian;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Longs.hashCode(baseSeed) ^ Longs.hashCode(size() ^ (gaussian ? 7 : 11));
  }

  
}
