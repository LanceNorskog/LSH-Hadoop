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
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/* 
 * DataModel has all answers. Just ask it.
 */

public class PointTextItemItemRecommender implements Recommender {

  final DataModel model;
  final ItemSimilarity itemSimilarity;

  public PointTextItemItemRecommender(DataModel model) {
    this.model = model;
    this.itemSimilarity = null;
  }

  public PointTextItemItemRecommender(DataModel model, ItemSimilarity itemSimilarity) {
    this.model = model;
    this.itemSimilarity = itemSimilarity;
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
    Recommender rec = new PointTextItemItemRecommender(model);
    //		List<RecommendedItem> x = rec.recommend(5, 3);
    //		x.hashCode();
    RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
    double evaluation = evaluator.evaluate(new PointTextItemItemRecommenderBuilder(),
        null,
        model,
        0.9,
        0.3);
    System.err.println("Evaluation: " + evaluation);
  }

}

class PointTextItemItemRecommenderBuilder implements RecommenderBuilder {

  @Override
  public Recommender buildRecommender(DataModel dataModel)
  throws TasteException {
    return new PointTextItemItemRecommender(dataModel);
  }

}

class PointItemSimilarity implements ItemSimilarity {
  public PointItemSimilarity() {
    
  }

  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    // TODO Auto-generated method stub
    return null;
  }
}

