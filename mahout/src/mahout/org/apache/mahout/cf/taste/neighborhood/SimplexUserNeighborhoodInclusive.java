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
public class SimplexUserNeighborhoodInclusive implements UserNeighborhood {
  final SimplexSpace space;
  final SimplexSpace spaceLOD;
  final long[] EMPTY = new long[0];
  
  public SimplexUserNeighborhoodInclusive(SimplexSpace space, SimplexSpace spaceLOD) {
    this.space = space;
    this.spaceLOD = spaceLOD;
  }
  
  /*
   * External pass to clone. migrate to here.
   */
  public void addUser(Long userID, Vector v) {
    space.addVector(userID, v);
  }
  
  // All of those crazy recommender collections?
  
  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.neighborhood.UserNeighborhood#getUserNeighborhood(long)
   */
  @Override
  public long[] getUserNeighborhood(long userID) throws TasteException {
    long[] values = space.findNeighbors(userID);
    if (null != values)
      return values;
    values = spaceLOD.findNeighbors(userID);
    if (null != values) {
      System.err.println("LOD: " + values.length);
      return values;
    }
    return EMPTY;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    throw new UnsupportedOperationException();
  }

}
