package org.apache.mahout.cf.taste.impl.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.hadoop.item.PrefAndSimilarityColumnWritable;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class NormalRankingRecommenderEvaulator implements RecommenderEvaluator {
  float minPreference, maxPreference;
  public boolean doCSV = false;


  @Override
  public double evaluate(RecommenderBuilder recommenderBuilder,
      DataModelBuilder dataModelBuilder, DataModel dataModel,
      double trainingPercentage, double evaluationPercentage)
  throws TasteException {
    return 0;
  }

  /*
   * Get randomly sampled recommendations
   * Data model v.s. Data model : use for semvec training v.s. test 
   */

  public double evaluate(DataModel model1,
      DataModel model2, Random rnd, int samples, String tag) throws TasteException {
    double scores = 0;
    int allItems = model1.getNumItems();
    LongPrimitiveIterator users = model1.getUserIDs();
    if (doCSV)
      System.out.println("tag,user,sampled,match,normal");

    int foundusers = 0;
    while (users.hasNext()) {
      long userID = users.nextLong();
      allItems = Math.min(allItems, samples);
      PreferenceArray prefs1, prefs2;

      prefs1 = model1.getPreferencesFromUser(userID);
      if (null == prefs1)
        continue;
      prefs2 = model2.getPreferencesFromUser(userID);
      Set<Long> common = new HashSet<Long>();
      minimalSet(prefs1, prefs2, common, 10);
      int sampled = common.size();
      if (sampled == 0)
        continue;
      foundusers++;
      Preference[] prefsR = new Preference[sampled];
      Preference[] prefsDM = new Preference[sampled];
      getMatchesFromPrefsArray(userID, prefsR, prefs1, model1, common);
      getMatchesFromPrefsArray(userID, prefsDM, prefs2, model2, common);
      double match = (sampled - sloppyHamming(prefsDM, prefsR))/(double) sampled;
      double normalW = normalWilcoxon(prefsDM, prefsR);
      double variance = normalW/sampled;
      if (doCSV)
        System.out.println(tag + "," + userID + "," + sampled + "," + match + "," + variance);
      scores += variance;
      this.hashCode();
      //  points gets more trash but need measure that finds it
    }
    return scores / foundusers;
  } 

  public double evaluate_bits(Recommender recco,
      DataModel model, Random rnd, int samples, String tag) throws TasteException {
    double scores = 0;
    int allItems = model.getNumItems();
    LongPrimitiveIterator users = model.getUserIDs();
    if (doCSV)
      System.out.println("tag,user,sampled,match,normal");

    int foundusers = 0;
    while (users.hasNext()) {
      long userID = users.nextLong();
      allItems = Math.min(allItems, samples);

      PreferenceArray prefsArray = model.getPreferencesFromUser(userID);
      FastIDSet modelSet = new FastIDSet();
      setBits(modelSet, prefsArray);
//      List<RecommendedItem> recs = getRecs(recco, userID, modelSet, allItems);
      int sampled = prefsArray.length();
//      if (sampled == 0)
//        continue;
      foundusers++;
      Preference[] prefsR = new Preference[sampled];
      Preference[] prefsDM = new Preference[sampled];
      getPrefsArray(userID, prefsR, prefsArray);
      getMatchesFromRecco(userID, prefsDM, recco, modelSet);
      double match = (sampled - sloppyHamming(prefsDM, prefsR))/ (double) sampled;
      double normalW = normalWilcoxon(prefsDM, prefsR);
      double variance = normalW/sampled;
      if (doCSV)
        System.out.println(tag + "," + userID + "," + sampled + "," + match + "," + variance);
      scores += variance;
      this.hashCode();
      //	points gets more trash but need measure that finds it
    }
    return scores / foundusers;
  } 

  private void getPrefsArray(long userID, Preference[] prefsR,
      PreferenceArray prefsArray) {
    for(int i = 0; i < prefsR.length; i++) 
      prefsR[i] = prefsArray.get(i);
  }

  private void getMatchesFromRecco(long userID, Preference[] prefsDM,
      Recommender recco, FastIDSet modelSet) throws TasteException {
    List<RecommendedItem> recs = new ArrayList<RecommendedItem>();
    int index = 0;
    LongPrimitiveIterator it = modelSet.iterator();
    while(it.hasNext()) {
      Long itemID = it.next();
      float rec = recco.estimatePreference(userID, itemID);
      prefsDM[index++] = new GenericPreference(userID, itemID, rec);
    }
    if (index != prefsDM.length)
      throw new TasteException("wrong length in getMatchesFromRecco");
  }

  private List<RecommendedItem> getRecs(Recommender recco, long userID,
      FastIDSet modelSet, int allItems) {
    //    List<RecommendedItem> recs = new ArrayList<RecommendedItem>();
    //    LongPrimitiveIterator it = modelSet.iterator();
    //    while(it.hasNext()) {
    //      Long item = it.next();
    //      rec = recco.
    //    }
    return null;
  }

  private void setBits(FastIDSet modelSet, PreferenceArray prefs) {
    for(int i = 0; i < prefs.length(); i++) {
      modelSet.add(prefs.getItemID(i));
    }
  }

  private void setBits(FastIDSet modelSet, Preference[] prefs) {
    for(int i = 0; i < prefs.length; i++) {
      modelSet.add(prefs[i].getItemID());
    }
  }

  public double evaluate(Recommender recco,
      DataModel model, Random rnd, int samples, String tag) throws TasteException {
    double scores = 0;
    int allItems = model.getNumItems();
    LongPrimitiveIterator users = model.getUserIDs();
    if (doCSV)
      System.out.println("tag,user,sampled,match,normal");

    int foundusers = 0;
    while (users.hasNext()) {
      long userID = users.nextLong();
      allItems = Math.min(allItems, samples);
      List<RecommendedItem> recs;
      try {
        recs = recco.recommend(userID, allItems);
      } catch (NoSuchUserException e) {
        continue;
      }
      int sampled = recs.size();
      if (sampled == 0)
        continue;
      foundusers++;
      Preference[] prefsR = new Preference[sampled];
      Preference[] prefsDM = new Preference[sampled];
      getPrefsArray(recs, userID, prefsR, rnd);
      getMatchesFromDataModel(userID, prefsDM, recs, model);
      double match = (sampled - sloppyHamming(prefsDM, prefsR))/ (double) sampled;
      double normalW = normalWilcoxon(prefsDM, prefsR);
      double variance = normalW/sampled;
      if (doCSV)
        System.out.println(tag + "," + userID + "," + sampled + "," + match + "," + variance);
      scores += variance;
      this.hashCode();
      //    points gets more trash but need measure that finds it
    }
    return scores / foundusers;
  } 

  /*
   * Get randomly sampled recommendations
   * 
   */
  public double evaluate(Recommender recco1,
      Recommender recco2, Random rnd, int samples, String tag) throws TasteException {
    double scores = 0;
    int allItems = recco1.getDataModel().getNumItems();
    LongPrimitiveIterator users = recco1.getDataModel().getUserIDs();
    if (doCSV)
      System.out.println("user,sampled,match,normal");

    int foundusers = 0;
    while (users.hasNext()) {
      long userID = users.nextLong();
      allItems = Math.min(allItems, samples);
      List<RecommendedItem> recs;
      try {
        recs = recco1.recommend(userID, allItems);
      } catch (NoSuchUserException e) {
        continue;
      }
      int sampled = recs.size();
      if (sampled == 0)
        continue;
      foundusers++;
      Preference[] prefsR = new Preference[sampled];
      Preference[] prefsDM = new Preference[sampled];
      getPrefsArray(recs, userID, prefsR, rnd);
      getMatchesFromRecommender(userID, prefsDM, recs, recco2);
      double match = (sampled - sloppyHamming(prefsDM, prefsR))/ (double) sampled;
      double normalW = normalWilcoxon(prefsDM, prefsR);
      double variance = normalW/sampled;
      if (doCSV)
        System.out.println(tag + "," + userID + "," + sampled + "," + match + "," + variance);
      scores += variance;
      this.hashCode();
      //	points gets more trash but need measure that finds it
    }
    return scores / foundusers;
  } 

  /*
   * Fill subsampled array from full list of recommended items 
   * Some recommenders give short lists
   */
  private Preference[] getPrefsArray(List<RecommendedItem> recs, long userID, Preference[] prefs, Random rnd) {
    int nprefs = prefs.length;
    if (nprefs > recs.size()) 
      this.hashCode();
    int found = 0;
    while (found < nprefs) {
      if (null == prefs[found]) {
        prefs[found] = new GenericPreference(userID, recs.get(found).getItemID(), recs.get(found).getValue());
        found++;
      }
    }
    Arrays.sort(prefs, new PrefComparator(-1, true));
    return prefs;
  }

  private Preference[] getMatchesFromDataModel(Long userID, Preference[] prefs, List<RecommendedItem> recs,
      DataModel dataModel) throws TasteException {
    int nprefs = prefs.length;
    Iterator<RecommendedItem> it = recs.iterator();
    for(int i = 0; i < nprefs; i++) {
      RecommendedItem rec = it.next();
      Float value = dataModel.getPreferenceValue(userID, rec.getItemID());
      prefs[i] = new GenericPreference(userID, rec.getItemID(), value);
    }
    Arrays.sort(prefs, new PrefComparator(-1, true));
    return prefs;
  }

  // find minimal set of common recommended items - in order of first prefs array
  private void minimalSet(PreferenceArray prefs1, PreferenceArray prefs2, Set<Long> common, int max) {
    Set<Long> set1 = new HashSet<Long>();
    for(int i = 0; i < prefs1.length(); i++) {
      Long item = prefs1.getItemID(i);
      set1.add(item);
    }
    for(int i = 0; i < prefs2.length(); i++) {
      Long item = prefs2.getItemID(i);
      if (set1.contains(item))
        common.add(item);
      if (common.size() == max)
        return;
    }
  }

  private Preference[] getMatchesFromPrefsArray(Long userID, Preference[] prefs, 
      PreferenceArray prefsArray,
      DataModel dataModel, Set<Long> common) throws TasteException {
    int nprefs = prefs.length;
    int index = 0;
    for(int i = 0; i < prefsArray.length(); i++) {
      Long itemID = prefsArray.getItemID(i);
      if (common.contains(itemID))
        prefs[index++] = prefsArray.get(i);
      this.hashCode();
    }
    if (index != common.size()) {
      System.err.println("Must have pulled common.size() prefs! got: " + index + " instead of: " + common.size());

    }
    Arrays.sort(prefs, new PrefComparator(-1, true));
    return prefs;
  }

  private Preference[] getMatchesFromRecommender(Long userID, Preference[] prefs, List<RecommendedItem> recs,
      Recommender recco2) throws TasteException {
    int nprefs = prefs.length;
    Iterator<RecommendedItem> it = recs.iterator();
    for(int i = 0; i < nprefs; i++) {
      RecommendedItem rec = it.next();
      Float value = recco2.estimatePreference(userID, rec.getItemID());
      prefs[i] = new GenericPreference(userID, rec.getItemID(), value);
    }
    Arrays.sort(prefs, new PrefComparator(-1, true));
    return prefs;
  }

  private int sloppyHamming(Preference[] prefsDM, Preference[] prefsR) {
    int count = 0;
    try {
      for(int i = 1; i < prefsDM.length - 1; i++) {
        long itemID = prefsR[i].getItemID();
        if ((prefsDM[i].getItemID() != itemID) &&
            (prefsDM[i+1].getItemID() != itemID)&&
            (prefsDM[i-1].getItemID() != itemID)) {
          count++;
        } else {
          //					System.out.println("xxx");
          this.hashCode();
        }
      }
    } catch (Exception e) {
      this.hashCode();
    }
    return count;
  }

  /*
   * Normal-distribution probability value for matched sets of values
   * http://comp9.psych.cornell.edu/Darlington/normscor.htm
   */
  double normalWilcoxon(Preference[] prefsDM, Preference[] prefsR) {
    double mean = 0;
    int nitems = prefsDM.length;

    int[] vectorZ = new int[nitems];
    int[] vectorZabs = new int[nitems];
    double[] ranks = new double[nitems];
    double[] ranksAbs = new double[nitems];
    getVectorZ(prefsDM, prefsR, vectorZ, vectorZabs);
    wilcoxonRanks(vectorZ, vectorZabs, ranks, ranksAbs);
    // Mean of abs values is W+, Mean of signed values is W-
    //		mean = getMeanRank(ranks);
    //		mean = Math.abs(mean) * (Math.sqrt(nitems));
    mean = Math.min(getMeanWplus(ranks), getMeanWminus(ranks));
    mean = mean * (Math.sqrt(nitems));
    return mean;
  }

  /*
   * vector Z is a list of distances between the correct value and the recommended value
   * Z[i] = position i of correct itemID - position of correct itemID in recommendation list
   * 	can be positive or negative
   * 	the smaller the better - means recommendations are closer
   * both are the same length, and both sample from the same set
   * 
   * destructive to prefsDM and prefsR arrays - allows N log N instead of N^2 order
   */
  private void getVectorZ(Preference[] prefsDM, Preference[] prefsR, int[] vectorZ, int[] vectorZabs) {
    int nitems = prefsDM.length;
    int bottom = 0;
    int top = nitems - 1;
    for(int i = 0; i < nitems; i++) {
      long itemID = prefsDM[i].getItemID();
      for(int j = bottom; j <= top; j++) {
        if (prefsR[j] == null)
          continue;
        long test = prefsR[j].getItemID();
        if (itemID == test) {
          vectorZ[i] = i - j;
          vectorZabs[i] = Math.abs(i - j);
          if (j == bottom) {
            bottom++;
          } else if (j == top) {
            top--;
          } else {
            prefsR[j] = null;
          }
          break;
        }
      }	
    }
  }

  /*
   * Ranks are the position of the value from low to high, divided by the # of values.
   * I had to walk through it a few times.
   */
  private void wilcoxonRanks(int[] vectorZ, int[] vectorZabs, double[] ranks, double[] ranksAbs) {
    int nitems = vectorZ.length;
    int[] sorted = vectorZabs.clone();
    Arrays.sort(sorted);
    int zeros = 0;
    for(; zeros < nitems; zeros++) {
      if (sorted[zeros] > 0) 
        break;
    }
    for(int i = 0; i < nitems; i++) {
      double rank = 0;
      int count = 0;
      int score = vectorZabs[i];
      for(int j = 0; j < nitems; j++) {
        if (score == sorted[j]) {
          rank += (j + 1) - zeros;
          count++;
        } else if (score < sorted[j]) {
          break;
        }
      }
      if (vectorZ[i] != 0) {
        ranks[i] = (rank/count) * ((vectorZ[i] < 0) ? -1 : 1);	// better be at least 1
        ranksAbs[i] = Math.abs(ranks[i]);
      }
    }
  }

  /*
   * get mean of deviation from hypothesized center of 0
   */
  private double getMeanRank(double[] ranks) {
    int nitems = ranks.length;
    double sum = 0;
    for(int i = 0; i < nitems; i++) {
      sum += ranks[i];
    }
    double mean = sum / nitems;
    return mean;
  }

  private double getMeanWplus(double[] ranks) {
    int nitems = ranks.length;
    double sum = 0;
    for(int i = 0; i < nitems; i++) {
      if (ranks[i] > 0)
        sum += ranks[i];
    }
    double mean = sum / nitems;
    return mean;
  }

  private double getMeanWminus(double[] ranks) {
    int nitems = ranks.length;
    double sum = 0;
    for(int i = 0; i < nitems; i++) {
      if (ranks[i] < 0)
        sum += -ranks[i];
    }
    double mean = sum / nitems;
    return mean;
  }

  @Override
  public float getMaxPreference() {
    return maxPreference;
  }

  @Override
  public float getMinPreference() {
    return minPreference;
  }

  @Override
  public void setMaxPreference(float maxPreference) {
    this.maxPreference = maxPreference;
  }

  @Override
  public void setMinPreference(float minPreference) {
    this.minPreference = minPreference;
  }

}
