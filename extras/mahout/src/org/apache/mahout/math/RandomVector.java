/**
 * 
 */
package org.apache.mahout.math;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.math.ReadOnlyVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.ReadOnlyVector.AllIterator;
import org.apache.mahout.math.Vector.Element;

/**
 * 
 * Vector with repeatable random values.
 *
 */
public class RandomVector extends ReadOnlyVector {
  
  final private Random rnd;
  final private int seed;
  
  // required for serialization
  public RandomVector() {
    super(0);
    rnd = new Random(0);
    seed = 0;
  }
  
  /*
   * @param size
   * @param rnd
   */
  public RandomVector(int size, Random rnd) {
    super(size);
    this.rnd = rnd;
    seed = rnd.nextInt();
  }
  
  public int getNumNondefaultElements() {
    return size();
  }
  
  public double getQuick(int index) {
    rnd.setSeed(getSeed(index));
    return rnd.nextDouble();
  }
  
  private long getSeed(int index) {
    return seed + index;
  }
  
  public Iterator<Element> iterateNonZero() {
    return new AllIterator(this);
  }
  
  public Iterator<Element> iterator() {
    return new AllIterator(this);
  }
  
}
