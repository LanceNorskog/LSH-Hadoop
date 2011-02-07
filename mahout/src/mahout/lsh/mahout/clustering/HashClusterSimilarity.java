/**
 * 
 */
package lsh.mahout.clustering;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;

/**
 * @author lance
 *
 */
public class HashClusterSimilarity implements ClusterSimilarity {

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity#getSimilarity(org.apache.mahout.cf.taste.impl.common.FastIDSet, org.apache.mahout.cf.taste.impl.common.FastIDSet)
   */
  @Override
  public double getSimilarity(FastIDSet cluster1, FastIDSet cluster2)
      throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // TODO Auto-generated method stub

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
