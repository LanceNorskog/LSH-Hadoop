///**
// * 
// */
//package org.apache.mahout.math;
//
//import java.util.Iterator;
//import java.util.Random;
//
//import org.apache.mahout.math.ReadOnlyVector;
//
///**
// * @author lance
// *
// */
//public class RandomVector2 extends ReadOnlyVector {
//  
//  final private Random rnd;
//  final long seed;
//  final long stride;
//  
//  /**
//   * @param size
//   * @param value
//   * @param stride
//   *    If vector is a column-stride vector created by a RandomMatrix.
//   * @param rnd
//   */
//  public RandomVector2(int size, long seed, long stride, Random rnd) {
//    super(size);
//    this.rnd = rnd;
//    this.seed = seed;
//    this.stride = stride;
//  }
//
//  public int getNumNondefaultElements() {
//    return size();
//  }
//
//  public double getQuick(int index) {
//    rnd.setSeed(getSeed(index));
//    return rnd.nextDouble();
//  }
//  
//  private long getSeed(int index) {
//    return seed + (index * stride);
//  }
//
//  public Iterator<Element> iterateNonZero() {
//    return new AllIterator(this);
// }
//
// public Iterator<Element> iterator() {
//   return new AllIterator(this);
// }
//
//}
