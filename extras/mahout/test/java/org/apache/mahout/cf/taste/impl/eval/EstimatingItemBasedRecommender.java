/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.impl.eval;

import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A simple {@link org.apache.mahout.cf.taste.recommender.Recommender} which uses a given
 * {@link org.apache.mahout.cf.taste.model.DataModel} and
 * {@link org.apache.mahout.cf.taste.similarity.ItemSimilarity} to produce recommendations. This class
 * represents Taste's support for item-based recommenders.
 * </p>
 * 
 * <p>
 * The {@link org.apache.mahout.cf.taste.similarity.ItemSimilarity} is the most important point to discuss
 * here. Item-based recommenders are useful because they can take advantage of something to be very fast: they
 * base their computations on item similarity, not user similarity, and item similarity is relatively static.
 * It can be precomputed, instead of re-computed in real time.
 * </p>
 * 
 * <p>
 * Thus it's strongly recommended that you use
 * {@link org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity} with pre-computed similarities if
 * you're going to use this class. You can use
 * {@link org.apache.mahout.cf.taste.impl.similarity.HoeffdingCorrelationSimilarity} too, which computes
 * similarities in real-time, but will probably find this painfully slow for large amounts of data.
 * </p>
 */
public class EstimatingItemBasedRecommender extends AbstractRecommender implements ItemBasedRecommender {
  
  private static final Logger log = LoggerFactory.getLogger(EstimatingItemBasedRecommender.class);
  
  private final ItemSimilarity similarity;
  private final RefreshHelper refreshHelper;
  private EstimatedPreferenceCapper capper;

  public EstimatingItemBasedRecommender(DataModel dataModel,
                                     ItemSimilarity similarity,
                                     CandidateItemsStrategy candidateItemsStrategy) {
    super(dataModel, candidateItemsStrategy);
    if (similarity == null) {
      throw new IllegalArgumentException("similarity is null");
    }
    this.similarity = similarity;
    this.refreshHelper = new RefreshHelper(null);
    refreshHelper.addDependency(dataModel);
    refreshHelper.addDependency(similarity);
    capper = buildCapper();
  }

  public EstimatingItemBasedRecommender(DataModel dataModel,
                                     ItemSimilarity similarity) {
    this(dataModel, similarity, AbstractRecommender.getDefaultCandidateItemsStrategy());
  }

  public ItemSimilarity getSimilarity() {
    return similarity;
  }
  
  @Override
  public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
    if (howMany < 1) {
      throw new IllegalArgumentException("howMany must be at least 1");
    }
    
    log.debug("Recommending items for user ID '{}'", userID);
    
    PreferenceArray preferencesFromUser = getDataModel().getPreferencesFromUser(userID);
    if (preferencesFromUser.length() == 0) {
      return Collections.emptyList();
    }
    
    FastIDSet possibleItemIDs = getAllOtherItems(userID, preferencesFromUser);
    
    TopItems.Estimator<Long> estimator = new Estimator(userID);
    
    List<RecommendedItem> topItems = TopItems.getTopItems(howMany, possibleItemIDs.iterator(), rescorer,
      estimator);
    
    log.debug("Recommendations are: {}", topItems);
    return topItems;
  }
  
  @Override
  public float estimatePreference(long userID, long itemID) throws TasteException {
//    DataModel model = getDataModel();
//    Float actualPref = model.getPreferenceValue(userID, itemID);
//    if (actualPref != null) {
//      return actualPref;
//    }
    return doEstimatePreference(userID, itemID);
  }
  
  @Override
  public List<RecommendedItem> mostSimilarItems(long itemID, int howMany) throws TasteException {
    return mostSimilarItems(itemID, howMany, null);
  }
  
  @Override
  public List<RecommendedItem> mostSimilarItems(long itemID, int howMany,
                                                Rescorer<LongPair> rescorer) throws TasteException {
    TopItems.Estimator<Long> estimator = new MostSimilarEstimator(itemID, similarity, rescorer);
    return doMostSimilarItems(new long[] {itemID}, howMany, estimator);
  }
  
  @Override
  public List<RecommendedItem> mostSimilarItems(long[] itemIDs, int howMany) throws TasteException {
    return mostSimilarItems(itemIDs, howMany, null);
  }
  
  @Override
  public List<RecommendedItem> mostSimilarItems(long[] itemIDs, int howMany,
                                                Rescorer<LongPair> rescorer) throws TasteException {
    TopItems.Estimator<Long> estimator = new MultiMostSimilarEstimator(itemIDs, similarity, rescorer);
    return doMostSimilarItems(itemIDs, howMany, estimator);
  }
  
  @Override
  public List<RecommendedItem> recommendedBecause(long userID, long itemID, int howMany) throws TasteException {
    if (howMany < 1) {
      throw new IllegalArgumentException("howMany must be at least 1");
    }
    
    DataModel model = getDataModel();
    TopItems.Estimator<Long> estimator = new RecommendedBecauseEstimator(userID, itemID, similarity);
    
    PreferenceArray prefs = model.getPreferencesFromUser(userID);
    int size = prefs.length();
    FastIDSet allUserItems = new FastIDSet(size);
    for (int i = 0; i < size; i++) {
      allUserItems.add(prefs.getItemID(i));
    }
    allUserItems.remove(itemID);
    
    return TopItems.getTopItems(howMany, allUserItems.iterator(), null, estimator);
  }
  
  private List<RecommendedItem> doMostSimilarItems(long[] itemIDs,
                                                   int howMany,
                                                   TopItems.Estimator<Long> estimator) throws TasteException {
    DataModel model = getDataModel();
    FastIDSet possibleItemsIDs = new FastIDSet();
    for (long itemID : itemIDs) {
      PreferenceArray prefs = model.getPreferencesForItem(itemID);
      int size = prefs.length();
      for (int i = 0; i < size; i++) {
        long userID = prefs.get(i).getUserID();
        possibleItemsIDs.addAll(model.getItemIDsFromUser(userID));
      }
    }
    possibleItemsIDs.removeAll(itemIDs);
    return TopItems.getTopItems(howMany, possibleItemsIDs.iterator(), null, estimator);
  }
  
  protected float doEstimatePreference(long userID, long itemID) throws TasteException {
    double preference = 0.0;
    double totalSimilarity = 0.0;
    int count = 0;
    PreferenceArray prefs = getDataModel().getPreferencesFromUser(userID);
    double[] similarities = similarity.itemSimilarities(itemID, prefs.getIDs());
    for (int i = 0; i < similarities.length; i++) {
      double theSimilarity = similarities[i];
      if (!Double.isNaN(theSimilarity)) {
        // Weights can be negative!
        preference += theSimilarity * prefs.getValue(i);
        totalSimilarity += theSimilarity;
        count++;
      }
    }
    // Throw out the estimate if it was based on no data points, of course, but also if based on
    // just one. This is a bit of a band-aid on the 'stock' item-based algorithm for the moment.
    // The reason is that in this case the estimate is, simply, the user's rating for one item
    // that happened to have a defined similarity. The similarity score doesn't matter, and that
    // seems like a bad situation.
    if (count <= 1) {
      return Float.NaN;
    }
    float estimate = (float) (preference / totalSimilarity);
    if (capper != null) {
      estimate = capper.capEstimate(estimate);
    }
    return estimate;
  }
  
  private int getNumPreferences(long userID) throws TasteException {
    return getDataModel().getPreferencesFromUser(userID).length();
  }
  
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    refreshHelper.refresh(alreadyRefreshed);
    capper = buildCapper();
  }
  
  @Override
  public String toString() {
    return "GenericItemBasedRecommender[similarity:" + similarity + ']';
  }

  private EstimatedPreferenceCapper buildCapper() {
    DataModel dataModel = getDataModel();
    if (Float.isNaN(dataModel.getMinPreference()) && Float.isNaN(dataModel.getMaxPreference())) {
      return null;
    } else {
      return new EstimatedPreferenceCapper(dataModel);
    }
  }
  
  public static class MostSimilarEstimator implements TopItems.Estimator<Long> {
    
    private final long toItemID;
    private final ItemSimilarity similarity;
    private final Rescorer<LongPair> rescorer;
    
    public MostSimilarEstimator(long toItemID, ItemSimilarity similarity, Rescorer<LongPair> rescorer) {
      this.toItemID = toItemID;
      this.similarity = similarity;
      this.rescorer = rescorer;
    }
    
    @Override
    public double estimate(Long itemID) throws TasteException {
      LongPair pair = new LongPair(toItemID, itemID);
      if ((rescorer != null) && rescorer.isFiltered(pair)) {
        return Double.NaN;
      }
      double originalEstimate = similarity.itemSimilarity(toItemID, itemID);
      return rescorer == null ? originalEstimate : rescorer.rescore(pair, originalEstimate);
    }
  }
  
  private final class Estimator implements TopItems.Estimator<Long> {
    
    private final long userID;
    
    private Estimator(long userID) {
      this.userID = userID;
    }
    
    @Override
    public double estimate(Long itemID) throws TasteException {
      return doEstimatePreference(userID, itemID);
    }
  }
  
  private static final class MultiMostSimilarEstimator implements TopItems.Estimator<Long> {
    
    private final long[] toItemIDs;
    private final ItemSimilarity similarity;
    private final Rescorer<LongPair> rescorer;
    
    private MultiMostSimilarEstimator(long[] toItemIDs, ItemSimilarity similarity, Rescorer<LongPair> rescorer) {
      this.toItemIDs = toItemIDs;
      this.similarity = similarity;
      this.rescorer = rescorer;
    }
    
    @Override
    public double estimate(Long itemID) throws TasteException {
      RunningAverage average = new FullRunningAverage();
      double[] similarities = similarity.itemSimilarities(itemID, toItemIDs);
      for (int i = 0; i < toItemIDs.length; i++) {
        long toItemID = toItemIDs[i];
        LongPair pair = new LongPair(toItemID, itemID);
        if ((rescorer != null) && rescorer.isFiltered(pair)) {
          continue;
        }
        double estimate = similarities[i];
        if (rescorer != null) {
          estimate = rescorer.rescore(pair, estimate);
        }
        average.addDatum(estimate);
      }
      return average.getAverage();
    }
  }
  
  private final class RecommendedBecauseEstimator implements TopItems.Estimator<Long> {
    
    private final long userID;
    private final long recommendedItemID;
    private final ItemSimilarity similarity;
    
    private RecommendedBecauseEstimator(long userID, long recommendedItemID, ItemSimilarity similarity) {
      this.userID = userID;
      this.recommendedItemID = recommendedItemID;
      this.similarity = similarity;
    }
    
    @Override
    public double estimate(Long itemID) throws TasteException {
      Float pref = getDataModel().getPreferenceValue(userID, itemID);
      if (pref == null) {
        return Float.NaN;
      }
      double similarityValue = similarity.itemSimilarity(recommendedItemID, itemID);
      return (1.0 + similarityValue) * pref;
    }
  }

  @Override
  public List<RecommendedItem> mostSimilarItems(long[] itemIDs, int howMany,
      boolean excludeItemIfNotSimilarToAll) throws TasteException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RecommendedItem> mostSimilarItems(long[] itemIDs, int howMany,
      Rescorer<LongPair> rescorer, boolean excludeItemIfNotSimilarToAll)
      throws TasteException {
    // TODO Auto-generated method stub
    return null;
  }
  
}
