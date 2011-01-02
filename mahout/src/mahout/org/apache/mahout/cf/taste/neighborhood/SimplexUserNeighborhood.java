/**
 * 
 */
package org.apache.mahout.cf.taste.neighborhood;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lsh.core.Hasher;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.math.Vector;

/**
 * @author lance
 *
 * Implement neighborhood based on LOD-based SimplexSpace
 */
public class SimplexUserNeighborhood implements UserNeighborhood {
  final SimplexSpace space;
  
  public SimplexUserNeighborhood(SimplexSpace space) {
    this.space = space;
  }
  
  public void addUser(Long userID, Vector v) {
    space.addVector(userID, v);
  }
  
  // All of those crazy recommender collections
  
  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.neighborhood.UserNeighborhood#getUserNeighborhood(long)
   */
  @Override
  public long[] getUserNeighborhood(long userID) throws TasteException {
    long[] values = space.findUsers(userID);
    return values;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    throw new UnsupportedOperationException();
  }

}
