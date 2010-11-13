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

import java.util.Random;

/** Matrix of random but consistent doubles. 
 * Double.MIN_Value -> Double.MAX_VALUE 
 * linear and Gaussian distributions
 * 
 * Seed for [row][col] is this.seed + (row * #columns) + column.
 * This allows a RandomVector to take seed + (row * #columns) as its seed
 * and be reproducible from this matrix.
 * */
public class RandomMatrix extends AbstractMatrix {
	//	final int rows, columns;
	final Random rnd = new Random();
	final long seed;
	final boolean gaussian;
	final boolean limit;

	public RandomMatrix() {
		cardinality[ROW] = 0;
		cardinality[COL] = 0;
		seed = 0;
		gaussian = false;
		limit = false;
	}

	/**
	 * Constructs an empty matrix of the given size.
	 * @param rows  The number of rows in the result.
	 * @param columns The number of columns in the result.
	 */
	public RandomMatrix(int rows, int columns) {
		cardinality[ROW] = rows;
		cardinality[COL] = columns;
		seed = 0;
		gaussian = false;
		limit = false;
	}

	public RandomMatrix(int rows, int columns, long seed, boolean gaussian, boolean limit) {
		cardinality[ROW] = rows;
		cardinality[COL] = columns;
		this.seed = seed;
		this.gaussian = gaussian;
		this.limit = limit;
	}

	@Override
	public Matrix clone() {
		RandomMatrix clone = new RandomMatrix(rowSize(), columnSize(), seed, gaussian, limit);
		return clone;
	}

	public double getQuick(int row, int column) {
		rnd.setSeed(getSeed(row, column));
		double value = getRandom();
		if (! Double.isNaN(value))
			throw new Error("RandomMatrix: getQuick created NaN");
		return value;
	}

	private long getSeed(int row, int column) {
		return seed + (row * columnSize()) + column;
	}

	// give a wide range but avoid NaN land
	private double getRandom() {
		double raw = gaussian ? rnd.nextGaussian() : rnd.nextDouble();
		if (limit)
			return raw;
		return ((raw*2 - 0.5) * Double.MAX_VALUE/2);
	}

	public Matrix like() {
		return like(rowSize(), columnSize());
	}

	public Matrix like(int rows, int columns) {
		return new RandomMatrix(rows, columns);
	}

	public void setQuick(int row, int column, double value) {
		throw new UnsupportedOperationException();
	}

	public int[] getNumNondefaultElements() {
		return size();
	}

	public Matrix viewPart(int[] offset, int[] size) {
		int rowOffset = offset[ROW];
		int rowsRequested = size[ROW];
		int columnOffset = offset[COL];
		int columnsRequested = size[COL];

		return viewPart(rowOffset, rowsRequested, columnOffset, columnsRequested);
	}

	@Override
	public Matrix viewPart(int rowOffset, int rowsRequested, int columnOffset, int columnsRequested) {
		if (rowOffset < 0) {
			throw new IndexException(rowOffset, rowSize());
		}
		if (rowOffset + rowsRequested > rowSize()) {
			throw new IndexException(rowOffset + rowsRequested, rowSize());
		}
		if (columnOffset < 0) {
			throw new IndexException(columnOffset, columnSize());
		}
		if (columnOffset + columnsRequested > columnSize()) {
			throw new IndexException(columnOffset + columnsRequested, columnSize());
		}
		return new MatrixView(this, new int[]{rowOffset, columnOffset}, new int[]{rowsRequested, columnsRequested});
	}

	@Override
	public Matrix assign(double value) {
		throw new UnsupportedOperationException();
	}

	public Matrix assignColumn(int column, Vector other) {
		throw new UnsupportedOperationException();
	}

	public Matrix assignRow(int row, Vector other) {
		throw new UnsupportedOperationException();
	}

	public Vector getColumn(int column) {
		if (column < 0 || column >= columnSize()) {
			throw new IndexException(column, columnSize());
		}
		return new TransposeViewVector(this, column);
	}

	public Vector getRow(int row) {
		if (row < 0 || row >= rowSize()) {
			throw new IndexException(row, rowSize());
		}
		return new RandomVector(cardinality[ROW], seed + row * cardinality[COL], gaussian);
	}

}
