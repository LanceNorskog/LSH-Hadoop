package org.apache.mahout.math.stats;

/*
 * Function on a sample type: returns X for that sample
 */

public abstract class SampleFunction<T> {

  public abstract double apply(T arg1);

}
