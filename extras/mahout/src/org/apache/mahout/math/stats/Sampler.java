package org.apache.mahout.math.stats;

import java.util.Iterator;

/*
 * Injectable implementations of sampling.
 * Usable in both map/reduce and online jobs.
 * 
 * getSamples() returns the entire currently available sample stream.
 * getSample() walks through the available input stream. 
 * This walking position is not required correlate with the iterator.
 * You may not wish to interleave these two calls.
 */

public abstract class Sampler<T> {

  /* Add a new sample */
  public abstract void addSample(T sample);
  
  /* Are there any samples? */
  // what if it reads from a separate thread?
//  public abstract boolean hasSamples();
  
  /* Return current set of samples. */
  public abstract Iterator<T> getSamples(boolean flush);

  /* next available sample. Return null of none available. */
  public abstract T getSample();
  
  /* stop operations. Iterators now end. */
  public abstract void stop();
}
