package org.apache.mahout.cf.taste.impl.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


/* 
 * DataModel has all answers. Just ask it.
 */

public class PointTextUserItemRecommender implements Recommender {

  final DataModel model;
  final UserSimilarity userSimilarity;

  public PointTextUserItemRecommender(DataModel model) {
    this.model = model;
    this.userSimilarity = null;
  }

  public PointTextUserItemRecommender(DataModel model, UserSimilarity userSimilarity) {
    this.model = model;
    this.userSimilarity = userSimilarity;
  }

  @Override
  public float estimatePreference(long userID, long itemID)
  throws TasteException {
    return model.getPreferenceValue(userID, itemID);
  }

  @Override
  public DataModel getDataModel() {
    return model;
  }

  @Override
  public List<RecommendedItem> recommend(long userID, int howMany)
  throws TasteException {
    List<RecommendedItem> recs = new ArrayList<RecommendedItem>();
    PreferenceArray prefs = model.getPreferencesFromUser(userID);
    prefs.sortByValueReversed();
    for(Preference p: prefs) {
      howMany--;
      if (howMany < 0)
        break;
      RecommendedItem rec = new GenericRecommendedItem(p.getItemID(), p.getValue());
      recs.add(rec);
    }
    return recs;
  }

  @Override
  public List<RecommendedItem> recommend(long userID, int howMany,
      IDRescorer rescorer) throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePreference(long userID, long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();

  }

  @Override
  public void setPreference(long userID, long itemID, float value)
  throws TasteException {
    throw new UnsupportedOperationException();

  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    throw new UnsupportedOperationException();

  }

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new PointTextDataModel(args[0]);
    Recommender rec = new PointTextUserItemRecommender(model);
    //		List<RecommendedItem> x = rec.recommend(5, 3);
    //		x.hashCode();
    RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
    double evaluation = evaluator.evaluate(new PointTextUserItemRecommenderBuilder(),
        null,
        model,
        0.9,
        0.3);
    System.err.println("Evaluation: " + evaluation);
  }

}

class PointTextUserItemRecommenderBuilder implements RecommenderBuilder {

  @Override
  public Recommender buildRecommender(DataModel dataModel)
  throws TasteException {
    return new PointTextUserItemRecommender(dataModel);
  }

}

class PointUserSimilarity implements UserSimilarity {
  public PointUserSimilarity() {
    
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
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // TODO Auto-generated method stub
    
  }
}

