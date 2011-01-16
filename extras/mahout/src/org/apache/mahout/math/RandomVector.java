/**
 * 
 */
package org.apache.mahout.math;

import java.util.Iterator;
import java.util.Random;

/**
 * @author lance
 *
 */
public class RandomVector extends ReadOnlyVector {
  
  final private Random rnd;
  final long seed;
  final long stride;
  
  /**
   * @param size
   * @param value
   */
  public RandomVector(int size, long seed, long stride, Random rnd) {
    super(size);
    this.rnd = rnd;
    this.seed = seed;
    this.stride = stride;
  }

  public int getNumNondefaultElements() {
    return size();
  }

  public double getQuick(int index) {
    // TODO Auto-generated method stub
    return 0;
  }

  public Iterator<Element> iterateNonZero() {
    // TODO Auto-generated method stub
    return null;
  }

  public Iterator<Element> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

}
