/**
 * 
 */
package org.apache.mahout.cf.taste.neighborhood;

import java.util.Collection;

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
  private final SimplexSpace userSpace;
  private final SimplexSpace itemSpace;
  private final DistanceMeasure measure;
  
  public SimplexSimilarity(SimplexSpace userSpace, SimplexSpace itemSpace, DistanceMeasure measure) {
    this.userSpace = userSpace;
    this.itemSpace = itemSpace;
    this.measure = measure;
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
    double d =1/ userSpace.getDistance(userID1, userID2, measure);
    return d;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.similarity.ItemSimilarity#itemSimilarities(long, long[])
   */
  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    double[] values = new double[itemID2s.length];
    for(int i = 0; i < itemID2s.length; i++) {
      values[i] = 1/itemSpace.getDistance(itemID1, itemID2s[i], measure);
    }
    return values;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.similarity.ItemSimilarity#itemSimilarity(long, long)
   */
  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    double d = itemSpace.getDistance(itemID1, itemID2, measure);
    return 1/d;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
