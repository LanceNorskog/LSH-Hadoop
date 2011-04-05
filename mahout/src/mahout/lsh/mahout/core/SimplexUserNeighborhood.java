/**
 * 
 */
package lsh.mahout.core;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.math.Vector;

/**
 * @author lance
 *
 * Implement neighborhood based on LOD-based SimplexSpace.
 * Store two SimplexSpaces, with the same hash values but at two 
 * different Levels Of Detail (LOD). Neighbors are users in the outer
 * outer space but not in the local space. (All users in the same
 * hash are distance 0, so they're not very useful.)
 * 
 * what is this about?
 */
public class SimplexUserNeighborhood implements UserNeighborhood {
  public final SimplexSpace<Long> space;
  public final SimplexSpace<Long> spaceLOD;
  static final long[] EMPTY = new long[0];
  public int total = 0, subtracted = 0;

  public SimplexUserNeighborhood(SimplexSpace<Long> space, SimplexSpace<Long> spaceLOD) {
    this.space = space;
    this.spaceLOD = spaceLOD;
  }

  /*
   * External pass to clone. migrate to here.
   */
  public void addUser(Long userID, Vector v) {
    space.addVector(v, userID);
    spaceLOD.addVector(v, userID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.neighborhood.UserNeighborhood#getUserNeighborhood(long)
   */
  @Override
  public long[] getUserNeighborhood(long userID) throws TasteException {
    FastIDSet nabes = space.findNeighborsIDSet(userID);
    FastIDSet nabesLOD = spaceLOD.findNeighborsIDSet(userID);
    if (null == nabesLOD || nabesLOD.size() == 0) 
      return EMPTY;
    int size = nabesLOD.size();
    nabesLOD.removeAll(nabes);
    total += size;
    subtracted += size - nabesLOD.size();
    return getLongArray(nabesLOD);
  }

  private long[] getLongArray(FastIDSet nabes) {
    if (nabes.size() == 0)
      return EMPTY;
    LongPrimitiveIterator lpi;
    long[] values = new long[nabes.size()];
    lpi = nabes.iterator();
    int count = 0;
    while (lpi.hasNext()) {
      values[count++] = lpi.nextLong();
    }
//    System.out.println("nabors: " + values.length);
    return values;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // something else created and maintains the stored spaces
    ;
  }

}
