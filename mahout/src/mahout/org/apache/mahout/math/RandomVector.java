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

import org.apache.mahout.math.function.BinaryFunction;

/** Implements vector as reproducible random numbers: 
 * the same index always returns the same double 
 */
public class RandomVector extends AbstractVector {

  final int cardinality;
  final Random rnd = new Random();
  final long seed;
  final boolean gaussian;
  final boolean limit = true;
  
  need way to reproduce any random matrix row or column

  /** For serialization purposes only */
  public RandomVector() {
    super(0);
    cardinality = 0;
    seed = 0;
    gaussian = false;
  }

  /** Construct a new instance of the given cardinality */
  public RandomVector(int cardinality) {
    super(cardinality);
    this.cardinality = cardinality;
    seed = 0;
    gaussian = false;
  }

  /** Construct a new instance of the given cardinality */
  public RandomVector(int cardinality, long seed) {
    super(cardinality);
    this.cardinality = cardinality;
   this.seed = seed;
   gaussian = false;
  }

  /** Construct a new instance of the given cardinality */
  public RandomVector(int cardinality, long seed, boolean gaussian) {
    super(cardinality);
    this.cardinality = cardinality;
   this.seed = seed;
   this.gaussian = gaussian;
  }

  /**
   * Copy-constructor (for use in turning a sparse vector into a dense one, for example)
   * @param vector
   */
  public RandomVector(Vector vector) {
	  super(0);
	  throw new UnsupportedOperationException();
  }

  @Override
  protected Matrix matrixLike(int rows, int columns) {
    return new RandomMatrix(rows, columns, seed, gaussian, limit);
  }

  @Override
  public RandomVector clone() {
    return new RandomVector(cardinality, seed);
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
	rnd.setSeed(seed + index);
    double value = (((gaussian ? rnd.nextGaussian() : rnd.nextDouble()) -0.5) * Float.MAX_VALUE) * 1.99;
    if (! Double.isNaN(value))
    	throw new Error("RandomVector: getQuick created NaN");
    return value;
  }

  public RandomVector like() {
    return new RandomVector(cardinality, seed);
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

  /**
   * Returns an iterator that traverses this Vector from 0 to cardinality-1, in that order.
   */
  public Iterator<Element> iterateNonZero() {
    return new NonDefaultIterator();
  }

  public Iterator<Element> iterator() {
    return new AllIterator();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RandomVector) {
      // speedup for random
    	RandomVector r = (RandomVector) o;
      return this.cardinality == r.cardinality && this.seed == r.seed;
    }
    return super.equals(o);
  }

//  @Override
//  public void addTo(Vector v) {
//    if (size() != v.size()) {
//      throw new CardinalityException(size(), v.size());
//    }
//    for (int i = 0; i < cardinality; i++) {
//      v.setQuick(i, getQuick(i) + v.getQuick(i));
//    }
//  }
  
//  public void addAll(Vector v) {
//    if (size() != v.size()) {
//      throw new CardinalityException(size(), v.size());
//    }
//    
//    Iterator<Element> iter = v.iterateNonZero();
//    while (iter.hasNext()) {
//      Element element = iter.next();
//      values[element.index()] += element.get();
//    }
//  }

  
  @Override
  public double dot(Vector x) {
    if (size() != x.size()) {
      throw new CardinalityException(size(), x.size());
    }
    if (this == x) {
      return dotSelf();
    }
    
    double result = 0;
    if (x instanceof RandomVector) {
      for (int i = 0; i < x.size(); i++) {
        result += this.getQuick(i) * x.getQuick(i);
      }
      return result;
    } else {
      // Try to get the speed boost associated fast/normal seq access on x and quick lookup on this
      Iterator<Element> iter = x.iterateNonZero();
      while (iter.hasNext()) {
        Element element = iter.next();
        result += element.get() * this.getQuick(element.index());
      }
      return result;
    }
  }


  private final class NonDefaultIterator implements Iterator<Element> {

	    private final DenseElement element = new DenseElement();
	    private int index = 0;

	    private NonDefaultIterator() {
	    }

	    public boolean hasNext() {
	      return index < size();
	    }

	    public Element next() {
	      if (index >= size()) {
	        throw new NoSuchElementException();
	      } else {
	        element.index = index;
	        index++;
	        return element;
	      }
	    }

	    public void remove() {
	      throw new UnsupportedOperationException();
	    }
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
