package org.apache.mahout.math.stats;

import java.util.Iterator;

/*
 * Injectable implementations of sampling.
 * Usable in both map/reduce and online jobs.
 * This class really has two different uses:
 * 1) tell me if I would drop this sample at this time, and
 * 2) store this sample for me and perhaps give it back.
 * 
 * getSamples() returns the entire currently available sample stream.
 * 
 */

public abstract class Sampler<T> {

  /* Add a new sample */
  public abstract void addSample(T sample);
  
  /* Are there any samples? */
  // what if it reads from a separate thread?
//  public abstract boolean hasSamples();
  
  /* Return current set of samples. */
  public abstract Iterator<T> getSamples(boolean flush);

  /* would this sample be added at this moment? */
  public abstract boolean isSampled(T sample);
  
  /* stop operations. Iterators now end. */
  public abstract void stop();
}
