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
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.function.BinaryFunction;

/** Implements vector as reproducible random numbers: 
 * the same index always returns the same double 
 * 
 * 'stride' allows this to be a RandomMatrix column view
 */
public class RandomVector extends AbstractVector {
  final int cardinality;
  final Random rnd = new Random();
  final long seed;
  final long stride;
  final int mode;

  //  need way to reproduce any random matrix row or column

  /** For serialization purposes only */
  public RandomVector() {
    this(0, 0, 0, RandomMatrix.LINEAR);
  }

  /** Construct a new instance of the given cardinality */
  public RandomVector(int cardinality) {
    this(cardinality, 0, 1, RandomMatrix.LINEAR);
  }

  /** Construct a new instance of the given cardinality */
  public RandomVector(int cardinality, long seed, long stride, int mode) {
    super(cardinality);
    this.cardinality = cardinality;
    this.seed = seed;
    this.stride = stride;
    this.mode = mode;
  }

  /**
   * Copy-constructor (for use in turning a sparse vector into a dense one, for example)
   * @param vector
   */
  public RandomVector(Vector vector) {
    super(0);   // Java requires this.
    throw new UnsupportedOperationException();
  }

  @Override
  protected Matrix matrixLike(int rows, int columns) {
    return new DenseMatrix(rows, columns);

  }

  @Override
  public RandomVector clone() {
    return new RandomVector(cardinality, seed, stride, mode);
  }

  /**
   * This can be whatever it needs to be.
   * @return true
   */
  public boolean isDense() {
    return true;
  }

  /**
   * This can be whatever it needs to be.
   * @return true
   */
  public boolean isSequentialAccess() {
    return true;
  }

  public double getQuick(int index) {
    rnd.setSeed(getSeed(index));
    return getRandom();
  }

  // as it turns out, numbers from consecutive seeds are highly correlated.
  private long getSeed(int index) {
    long starter = seed + index;
    rnd.setSeed(starter);
    return rnd.nextLong();
  }


  // give a wide range but avoid NaN land
  double getRandom() {
    switch (mode) {
    case RandomMatrix.LINEAR: return rnd.nextDouble();
    case RandomMatrix.GAUSSIAN: return rnd.nextGaussian();
    case RandomMatrix.GAUSSIAN01: return gaussian01();
    default: 
      throw new Error("Not a random distribution: " + mode);
    }
  }

  // normal distribution between zero and one
  private double gaussian01() {
    double d = rnd.nextGaussian()/3;
    while(d > 0.5 || d < -0.5) {
      d = rnd.nextGaussian()/3;
    }
    return d + 0.5;
  }

  public Vector like() {
    return new DenseVector(cardinality);
  }

  public void setQuick(int index, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector assign(double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector assign(Vector other, BinaryFunction function) {
    if (size() != other.size()) {
      throw new CardinalityException(size(), other.size());
    }
    throw new UnsupportedOperationException();
  }

  public int getNumNondefaultElements() {
    return cardinality;
  }

  public Iterator<Element> iterateNonZero() {
    return new AllIterator();
  }

  public Iterator<Element> iterator() {
    return new AllIterator();
  }

  @Override
  public boolean equals(Object o) {
    if (o.getClass() == RandomVector.class) {
      RandomVector r = (RandomVector) o;
      return this.cardinality == r.cardinality && this.seed == r.seed;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return RandomUtils.hashLong(seed)^ Long.toString(cardinality).hashCode();
  }

  public void addAll(Vector v) {
    if (size() != v.size()) {
      throw new CardinalityException(size(), v.size());
    }
    throw new UnsupportedOperationException();
  }

  private final class AllIterator implements Iterator<Element> {

    private final DenseElement element = new DenseElement();

    private AllIterator() {
      element.index = -1;
    }

    public boolean hasNext() {
      return element.index + 1 < size();
    }

    public Element next() {
      if (element.index + 1 >= size()) {
        throw new NoSuchElementException();
      } else {
        element.index++;
        return element;
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private final class DenseElement implements Element {

    int index;

    public double get() {
      return getQuick(index);
    }

    public int index() {
      return index;
    }

    public void set(double value) {
      throw new UnsupportedOperationException();
    }
  }

}
