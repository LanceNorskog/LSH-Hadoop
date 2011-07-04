/**
 * 
 */
package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;

/**
 * Implement UserSimilarity and ItemSimilarity using SimplexSpace. 
 * SimplexSpace can store User->Item or Item->Item.
 */
public class SimplexSimilarity implements UserSimilarity, ItemSimilarity {
  private final SimplexSpace<Long> userSpace;
  private final SimplexSpace<Long> itemSpace;
  private final DistanceMeasure measure;
  private final double nearness;
  
  public SimplexSimilarity(SimplexSpace<Long> userSpace, SimplexSpace<Long> itemSpace, DistanceMeasure measure) {
    this.userSpace = userSpace;
    this.itemSpace = itemSpace;
    this.measure = measure;
    this.nearness = 0.1;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.similarity.UserSimilarity#setPreferenceInferrer(org.apache.mahout.cf.taste.similarity.PreferenceInferrer)
   */
  @Override
  public void setPreferenceInferrer(PreferenceInferrer inferrer) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.similarity.UserSimilarity#userSimilarity(long, long)
   */
  @Override
  public double userSimilarity(long userID1, long userID2)
      throws TasteException {
    if (null == userSpace)
      throw new TasteException("SimplexSimilarity: no User vectors configured");
    double d = userSpace.getDistance(userID1, userID2, measure);
    return d;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.similarity.ItemSimilarity#itemSimilarities(long, long[])
   */
  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    if (null == itemSpace)
      throw new TasteException("SimplexSimilarity: no Item vectors configured");
    double[] values = new double[itemID2s.length];
    for(int i = 0; i < itemID2s.length; i++) {
      values[i] = (1 + 1/itemSpace.getDistance(itemID1, itemID2s[i], measure));
    }
    return values;
  }

  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    if (null == itemSpace)
      throw new TasteException("SimplexSimilarity: no Item vectors configured");
    Double d = itemSpace.getDistance(itemID1, itemID2, measure);
    // 0.0 <= d <= 1.0, guaranteed by SimplexSpace implementation
    // distance of 0 or 1 causes problems- 1/0 happens
    if (d == null || Double.isInfinite(d) | Double.isNaN(d))
      return 0;
    return Math.max(-0.9999999, Math.min(0.9999999, d));
  }

  @Override
  public long[] allSimilarItemIDs(long itemID) throws TasteException {
    if (null == itemSpace)
      throw new TasteException("SimplexSimilarity: no Item vectors configured");
    List<Long> similar = new ArrayList<Long>();
    Iterator<Long> keys = itemSpace.getKeyIterator();
    while(keys.hasNext()) {
      long otherID = keys.next();
      double d = itemSpace.getDistance(itemID, otherID, measure);
      if (d < nearness)
        similar.add(otherID);
    }
    long[] values = new long[similar.size()];
    for(int i = 0; i < similar.size(); i++) {
      values[i] = similar.get(i);
    }
    return values;
  }
  
}
