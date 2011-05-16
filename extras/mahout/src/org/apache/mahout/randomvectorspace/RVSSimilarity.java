package org.apache.mahout.randomvectorspace;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.AbstractItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class RVSSimilarity extends AbstractItemSimilarity implements UserSimilarity {

  protected RVSSimilarity(DataModel dataModel) {
    super(dataModel);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void setPreferenceInferrer(PreferenceInferrer inferrer) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public double userSimilarity(long userID1, long userID2)
      throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }
}
