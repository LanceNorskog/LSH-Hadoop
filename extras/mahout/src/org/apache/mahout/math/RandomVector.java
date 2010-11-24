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
 * Use Float min/max as limits just to avoid Outer Limits problems.
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
    super(0);   // Java requires this
    throw new UnsupportedOperationException();
  }

  @Override
  protected Matrix matrixLike(int rows, int columns) {
    throw new UnsupportedOperationException();

  }

  @Override
  public RandomVector clone() {
    return new RandomVector(cardinality, seed, stride, mode);
  }

  /**
   * @return true
   */
  public boolean isDense() {
    return true;
  }

  /**
   * @return true
   */
  public boolean isSequentialAccess() {
    return true;
  }

  @Override
  public double dotSelf() {
    double result = 0.0;
    int max = size();
    for (int i = 0; i < max; i++) {
      double value = this.getQuick(i);
      result += value * value;
    }
    return result;
  }

  // maybe it doesn't have to be so crazy?
  public double getQuick(int index) {
    long seed2 = getSeed(index);
    rnd.setSeed(seed2);
    double value = getRandom();
    if (!(value > Double.MIN_VALUE && value < Double.MAX_VALUE))
      throw new Error("RandomVector: getQuick created NaN");
    return value;
  }

  private long getSeed(int index) {
    return seed + (index * stride);
  }

  // give a wide range but avoid NaN land
  double getRandom() {
    switch (mode) {
    case RandomMatrix.LINEAR: return rnd.nextDouble();
    case RandomMatrix.GAUSSIAN: return rnd.nextGaussian();
    case RandomMatrix.GAUSSIAN01: return gaussian01();
    default: throw new Error();
    }
  }

  // normal distribution between zero and one
  private double gaussian01() {
    double d = rnd.nextGaussian()/6;
    while(d > 0.5 || d < -0.5) {
      d = rnd.nextGaussian()/6;
    }
    return d;
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

  @Override
  public Vector viewPart(int offset, int length) {
    if (offset < 0) {
      throw new IndexException(offset, size());
    }
    if (offset + length > size()) {
      throw new IndexException(offset + length, size());
    }
    return new VectorView(this, offset, length);
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

  @Override
  public void addTo(Vector v) {
    if (size() != v.size()) {
      throw new CardinalityException(size(), v.size());
    }
    for (int i = 0; i < cardinality; i++) {
      v.setQuick(i, getQuick(i) + v.getQuick(i));
    }
  }

  public void addAll(Vector v) {
    throw new UnsupportedOperationException();
  }
  
  // AbstractVector uses a more general implementation.
  public Matrix cross(Vector other) {
    Matrix result = new DenseMatrix(size(), other.size());
    for (int row = 0; row < size(); row++) {
      result.assignRow(row, other.times(getQuick(row)));
    }
    return result;
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
