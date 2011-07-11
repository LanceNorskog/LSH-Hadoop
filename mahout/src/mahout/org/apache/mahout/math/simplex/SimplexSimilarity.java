package org.apache.mahout.math.simplex;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class SimplexSimilarity implements UserSimilarity, ItemSimilarity,
    ClusterSimilarity {
  
  public SimplexSimilarity() {
    
  }
  
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
  }
  
  @Override
  public double getSimilarity(FastIDSet cluster1, FastIDSet cluster2)
      throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public long[] allSimilarItemIDs(long itemID) throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public double userSimilarity(long userID1, long userID2)
      throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void setPreferenceInferrer(PreferenceInferrer inferrer) {
    throw new UnsupportedOperationException();
  }
  
}
