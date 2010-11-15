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

import lmr.TestFullPass;

import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.function.BinaryFunction;

/** Implements vector as reproducible random numbers: 
 * the same index always returns the same double 
 * 
 * Use Float min/max as limits just to avoid Outer Limits problems.
 */
public class RandomVector extends AbstractVector {

	private static final double MIN_BOUND = 0.0000000001;
	private static final double MAX_BOUND = 0.99999999999;
	final int cardinality;
	final Random rnd = new Random();
	final long seed;
	final long stride;
	final boolean zero1;
	final double lowerBound;
	final double upperBound;
	final boolean gaussian;

	//  need way to reproduce any random matrix row or column

	/** For serialization purposes only */
	public RandomVector() {
		this(0, 0, 0, false, 0.0, 1.0, false);
	}

	/** Construct a new instance of the given cardinality */
	public RandomVector(int cardinality) {
		this(cardinality, 0, 1, true, 0.0, 1.0, false);
	}

	/** Construct a new instance of the given cardinality */
	public RandomVector(int cardinality, long seed, long skip, double lowerBound, double upperBound, boolean gaussian) {
		this(cardinality, seed, skip, false, lowerBound, upperBound, gaussian);
	}

	/** Construct a new instance of the given cardinality */
	public RandomVector(int cardinality, long seed, long stride, boolean zero1, double lowerBound, double upperBound, boolean gaussian) {
		super(cardinality);
		this.cardinality = cardinality;
		this.seed = seed;
		this.stride = stride;
		this.zero1 = zero1;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.gaussian = gaussian;
		rnd.setSeed(seed);
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
		throw new UnsupportedOperationException();

		//		return new RandomMatrix(rows, columns, seed, lowerBound, upperBound, gaussian);
	}

	@Override
	public RandomVector clone() {
		return new RandomVector(cardinality, seed, stride, zero1, lowerBound, upperBound, gaussian);
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
		if (!(value > lowerBound && value < upperBound))
			throw new Error("RandomVector: getQuick created NaN");
		return value;
	}

	private long getSeed(int index) {
		return seed + (index * stride);
	}

	// give a wide range but avoid NaN land
	private double getRandom() {
		double raw = gaussian ? rnd.nextGaussian() : rnd.nextDouble();
		if (zero1)
			return raw;
		double range = (upperBound - lowerBound);
		double expand = raw * range;
		expand += lowerBound;
		return expand;
	}


	public RandomVector like() {
		throw new UnsupportedOperationException();
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
	
	  public Vector plus(double x) {
		    Vector result = getDense();
		    if (x == 0.0) {
		      return result;
		    }
		    int size = result.size();
		    for (int i = 0; i < size; i++) {
		      result.setQuick(i, getQuick(i) + x);
		    }
		    return result;
		  }

	public Vector plus(Vector x) {
		if (size() != x.size()) {
			throw new CardinalityException(size(), x.size());
		}

		// prefer to have this be the denser than x
		if (!isDense() && (x.isDense() || x.getNumNondefaultElements() > this.getNumNondefaultElements())) {
			return x.plus(this);
		}

		Vector result = getDense();
		Iterator<Element> iter = x.iterateNonZero();
		while (iter.hasNext()) {
			Element e = iter.next();
			int index = e.index();
			result.setQuick(index, this.getQuick(index) + e.get());
		}
		return result;
	}


	public Vector minus(Vector that) {
		if (size() != that.size()) {
			throw new CardinalityException(size(), that.size());
		}

		Vector result = getDense();

		Iterator<Element> iter = that.iterateNonZero();
		while (iter.hasNext()) {
			Element thatElement = iter.next();
			int index = thatElement.index();
			result.setQuick(index, this.getQuick(index) - thatElement.get());
		}
		return result;
	}


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

	public Vector logNormalize(double power, double normLength) {   
		// we can special case certain powers
		if (Double.isInfinite(power) || power <= 1.0) {
			throw new IllegalArgumentException("Power must be > 1 and < infinity");
		} else {
			double denominator = normLength * Math.log(power);
			Vector result = getDense();
			Iterator<Element> iter = result.iterateNonZero();
			while (iter.hasNext()) {
				Element element = iter.next();
				element.set(Math.log(1 + element.get()) / denominator);
			}
			return result;
		}
	}

	public double norm(double power) {
		if (power < 0.0) {
			throw new IllegalArgumentException("Power must be >= 0");
		}
		// we can special case certain powers
		if (Double.isInfinite(power)) {
			double val = 0.0;
			Iterator<Element> iter = this.iterateNonZero();
			while (iter.hasNext()) {
				val = Math.max(val, Math.abs(iter.next().get()));
			}
			return val;
		} else if (power == 2.0) {
			return Math.sqrt(dotSelf());
		} else if (power == 1.0) {
			double val = 0.0;
			Iterator<Element> iter = this.iterateNonZero();
			while (iter.hasNext()) {
				val += Math.abs(iter.next().get());
			}
			return val;
		} else if (power == 0.0) {
			// this is the number of non-zero elements
			double val = 0.0;
			Iterator<Element> iter = this.iterateNonZero();
			while (iter.hasNext()) {
				val += iter.next().get() == 0 ? 0 : 1;
			}
			return val;
		} else {
			double val = 0.0;
			Iterator<Element> iter = this.iterateNonZero();
			while (iter.hasNext()) {
				Element element = iter.next();
				val += Math.pow(element.get(), power);
			}
			return Math.pow(val, 1.0 / power);
		}
	}


	public Vector divide(double x) {
		if (x == 1.0) {
			return getDense();
		}
		Vector result = getDense();
		Iterator<Element> iter = result.iterateNonZero();
		while (iter.hasNext()) {
			Element element = iter.next();
			element.set(element.get() / x);
		}
		return result;
	}

	public Matrix cross(Vector other) {
		Matrix result = new DenseMatrix(size(), other.size());
		for (int row = 0; row < size(); row++) {
			result.assignRow(row, other.times(getQuick(row)));
		}
		return result;
	}
	
	public Vector times(double x) {
		Vector result = getDense();
		if (x == 1.0) {
			return result;
		}
		if (x == 0.0) {
			return new DenseVector(size());
		}
		Iterator<Element> iter = result.iterateNonZero();
		while (iter.hasNext()) {
			Element element = iter.next();
			element.set(element.get() * x);
		}

		return result;
	}

	public Vector times(Vector x) {
		if (size() != x.size()) {
			throw new CardinalityException(size(), x.size());
		}

		Vector to = this;
		Vector from = x;
		// Clone and edit to the sparse one; if both are sparse, edit the more sparse one (more zeroes)
		if (isDense() || (!x.isDense() && getNumNondefaultElements() > x.getNumNondefaultElements())) {
			to = x;
			from = this;
		}

		Vector result = getDense();
		Iterator<Element> iter = result.iterateNonZero();
		while (iter.hasNext()) {
			Element element = iter.next();
			element.set(element.get() * from.getQuick(element.index()));
		}

		return result;
	}



	public Vector getDense() {
		Vector v = new DenseVector(size());
		for(int i = 0; i < size(); i++) 
			v.setQuick(i, getQuick(i));
		return v;
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
