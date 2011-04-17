package org.apache.mahout.math.stats;

import java.util.Iterator;

/*
 * Injectable implementations of sampling.
 * Usable in both map/reduce and online jobs.
 */

public abstract class Sampler<T> {

  /* Add a new sample */
  public abstract void addSample(T sample);
  
  /* Return current set of samples. */
  public abstract Iterator<T> getSamples(boolean flush);

  /* next available sample. Return null of none available. */
  public abstract T getSample();
}
