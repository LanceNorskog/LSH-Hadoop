package org.apache.mahout.math;

import java.util.Iterator;
import java.util.Map;

import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.VectorFunction;

public class BaseMatrix implements Matrix {

  @Override
  public double aggregate(DoubleDoubleFunction combiner, DoubleFunction mapper) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Vector aggregateColumns(VectorFunction f) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Vector aggregateRows(VectorFunction f) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String asFormatString() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assign(double value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assign(double[][] values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assign(Matrix other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assign(DoubleFunction function) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assign(Matrix other, DoubleDoubleFunction function) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assignColumn(int column, Vector other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix assignRow(int row, Vector other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int columnSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double determinant() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Matrix divide(double x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double get(int row, int column) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double get(String rowLabel, String columnLabel) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Vector getColumn(int column) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Integer> getColumnLabelBindings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] getNumNondefaultElements() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getQuick(int row, int column) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Vector getRow(int row) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Integer> getRowLabelBindings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix like() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix like(int rows, int columns) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix minus(Matrix x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix plus(double x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix plus(Matrix x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int rowSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void set(int row, int column, double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void set(int row, double[] data) {
    // TODO Auto-generated method stub

  }

  @Override
  public void set(String rowLabel, String columnLabel, double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void set(String rowLabel, String columnLabel, int row, int column,
      double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void set(String rowLabel, double[] rowData) {
    // TODO Auto-generated method stub

  }

  @Override
  public void set(String rowLabel, int row, double[] rowData) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setColumnLabelBindings(Map<String, Integer> bindings) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setQuick(int row, int column, double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setRowLabelBindings(Map<String, Integer> bindings) {
    // TODO Auto-generated method stub

  }

  @Override
  public int[] size() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix times(double x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix times(Matrix x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix transpose() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Vector viewColumn(int column) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix viewPart(int[] offset, int[] size) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix viewPart(int rowOffset, int rowsRequested, int columnOffset,
      int columnsRequested) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Vector viewRow(int row) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double zSum() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Iterator<MatrixSlice> iterateAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int numCols() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int numRows() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int numSlices() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Vector times(Vector v) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Vector timesSquared(Vector v) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterator<MatrixSlice> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

}
