package org.apache.mahout.math.stats;

/*
 * Function on a sample type: returns a double for that sample
 */

public abstract class SampleFunction<T> {

  public abstract double apply(T arg1);

}
