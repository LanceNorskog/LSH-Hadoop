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

package org.apache.mahout.cf.taste.impl.common;

import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.mahout.cf.taste.impl.common.AbstractLongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.common.RandomUtils;

/**
 * Wraps a {@link LongPrimitiveIterator} and returns only some subset of the elements that it would,
 * as determined by a sampling rate parameter.
 */
public final class SamplingLongPrimitiveIterator extends AbstractLongPrimitiveIterator {
  static private long enforcer = 0;
  
  private final LongPrimitiveIterator delegate;
  private final double lower;
  private final double upper;
  private long next;
  private boolean hasNext;
  private int toSkip;
  private Random rnd = RandomUtils.getRandom();
  

  public SamplingLongPrimitiveIterator(LongPrimitiveIterator delegate, double samplingRate) {
    this(delegate, 0, samplingRate, 0);
  }
  
  public SamplingLongPrimitiveIterator(LongPrimitiveIterator delegate, double lower, double upper, long seed) {
    this.delegate = delegate;
    this.lower = lower;
    this.upper = upper;
    this.hasNext = delegate.hasNext();
    rnd.setSeed(seed);
    doNext();
  }
  
  @Override
  public boolean hasNext() {
    return hasNext;
  }
  
  @Override
  public long nextLong() {
    if (hasNext) {
      long result = next;
      doNext();
      return result;
    }
    throw new NoSuchElementException();
  }
  
  @Override
  public long peek() {
    if (hasNext) {
      return next;
    }
    throw new NoSuchElementException();
  }
  
  @Override
  public void remove() {
    delegate.remove();
    doNext();
 }
  
  @Override
  public void skip(int n) {
    while(n > 0) {
      if (! isSampled()) {
        toSkip++;
      }
      n--;
    }
    doNext();
  }

  private void doNext() {
    while (true) {
      if (isSampled()) 
        break;
      toSkip++;
    }
    if (toSkip > 0) {
      delegate.skip(toSkip);
      toSkip = 0;
    }
    hasNext = delegate.hasNext();
    if (hasNext) {
      next = delegate.next();
    }
  }

  private boolean isSampled() {
    double sample = rnd.nextDouble();
    boolean sampled = (sample >= lower && sample < upper);
    return sampled;
  }
  
  public static LongPrimitiveIterator maybeWrapIterator(LongPrimitiveIterator delegate, double samplingRate) {
    enforcer++;
    return samplingRate >= 1.0 ? delegate : new SamplingLongPrimitiveIterator(delegate, 0, samplingRate, System.currentTimeMillis() + 13*enforcer);
  }
  
  public static LongPrimitiveIterator maybeWrapIterator(LongPrimitiveIterator delegate, double lower, double upper) {
    enforcer++;
    return lower >= 1.0 ? delegate : new SamplingLongPrimitiveIterator(delegate, lower, upper, System.currentTimeMillis() + 7*enforcer);
  }
    
}