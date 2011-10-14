package org.apache.mahout.randomvectorspace;

import java.util.Iterator;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

public class RVSVector implements Vector {
  RVS mask;
  
  @Override
  public Vector clone() {
    RVSVector v = new RVSVector();
    v.mask = mask;
    return v;
  }
  
  @Override
  public double aggregate(DoubleDoubleFunction aggregator, DoubleFunction map) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public double aggregate(Vector other, DoubleDoubleFunction aggregator,
      DoubleDoubleFunction combiner) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public String asFormatString() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(double value) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(double[] values) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(Vector other) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(DoubleFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(Vector other, DoubleDoubleFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector assign(DoubleDoubleFunction f, double y) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix cross(Vector other) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Vector divide(double x) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public double dot(Vector x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double get(int index) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double getDistanceSquared(Vector v) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Element getElement(int index) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double getLengthSquared() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public int getNumNondefaultElements() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double getQuick(int index) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public boolean isDense() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public boolean isSequentialAccess() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Iterator<Element> iterateNonZero() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Iterator<Element> iterator() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector like() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector logNormalize() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector logNormalize(double power) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double maxValue() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public int maxValueIndex() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double minValue() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public int minValueIndex() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector minus(Vector x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double norm(double power) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector normalize() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector normalize(double power) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector plus(double x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector plus(Vector x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public void set(int index, double value) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public void setQuick(int index, double value) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public int size() {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector times(double x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector times(Vector x) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public Vector viewPart(int offset, int length) {
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public double zSum() {
    throw new UnsupportedOperationException();
    
  }
  
}
