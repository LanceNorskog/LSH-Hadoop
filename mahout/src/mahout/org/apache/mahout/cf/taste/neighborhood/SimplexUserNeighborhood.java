/**
 * 
 */
package org.apache.mahout.cf.taste.neighborhood;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.math.Vector;

/**
 * @author lance
 *
 * Implement neighborhood based on LOD-based SimplexSpace
 */
public class SimplexUserNeighborhood implements UserNeighborhood {
  final SimplexSpace space;
  final long[] EMPTY = new long[0];
  
  public SimplexUserNeighborhood(SimplexSpace space) {
    this.space = space;
  }
  
  public void addUser(Long userID, Vector v) {
    space.addVector(userID, v);
  }
  
  // All of those crazy recommender collections?
  
  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.neighborhood.UserNeighborhood#getUserNeighborhood(long)
   */
  @Override
  public long[] getUserNeighborhood(long userID) throws TasteException {
    long[] values = space.findNeighbors(userID, 0);
    if (null == values)
      return EMPTY;
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
