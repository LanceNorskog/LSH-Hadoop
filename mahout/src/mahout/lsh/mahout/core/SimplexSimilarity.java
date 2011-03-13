/**
 * 
 */
package lsh.mahout.core;

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
  private final SimplexSpace<Long> userSpace;
  private final SimplexSpace<Long> itemSpace;
  private final DistanceMeasure measure;
  
  public SimplexSimilarity(SimplexSpace<Long> userSpace, SimplexSpace<Long> itemSpace, DistanceMeasure measure) {
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
    double d =(1 + 1/ userSpace.getDistance(userID1, userID2, measure));
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
    double[] values = new double[itemID2s.length];
    for(int i = 0; i < itemID2s.length; i++) {
      values[i] = (1 + 1/itemSpace.getDistance(itemID1, itemID2s[i], measure));
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
    return (1 + 1/d);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
