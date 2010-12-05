package org.apache.mahout.cf.taste.impl.eval;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
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

public class OrderBasedRecommenderEvaulator implements RecommenderEvaluator {
  float minPreference, maxPreference;
  public PrintStream csvOut = null;



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
    printHeader();
    double scores = 0;
    LongPrimitiveIterator users = model1.getUserIDs();

    int foundusers = 0;
    while (users.hasNext()) {
      long userID = users.nextLong();
      PreferenceArray prefs1, prefs2;

      prefs1 = model1.getPreferencesFromUser(userID);
      prefs1.sortByValueReversed();
      prefs2 = model2.getPreferencesFromUser(userID);
      prefs2.sortByValueReversed();
      int found = Math.min(prefs1.length(), prefs2.length());
      found = Math.min(found, samples);
      if (found < 2)
        continue;
      foundusers++;
      int max = Math.max(prefs1.length(), prefs2.length());
      max = Math.min(max, samples);

      FastIDSet commonSet = new FastIDSet();
      int subset = minimalSet(prefs1, prefs2, commonSet, max);
      Long[] items1 = getCommonItems(commonSet, prefs1, max);
      Long[] items2 = getCommonItems(commonSet, prefs2, max);
      double variance = scoreResults(tag, userID, max, subset, commonSet, items1, items2);
      scores += variance;
      this.hashCode();
      //  points gets more trash but need measure that finds it
    }
    return scores / foundusers;
  }


  private void printHeader() {
    if (null != csvOut)
      csvOut.println("tag,user,sampled,common,hamming,rank,normal,score");
  } 

  private double scoreResults(String tag, long userID, int sampled, int subset, FastIDSet commonSet,
      Long[] itemsL, Long[] itemsR) {
    FastIDSet setL = new FastIDSet();
    FastIDSet setR = new FastIDSet();
    setBits(setL, itemsL);
    setBits(setR, itemsR);
    int found = itemsL.length;
    
    int[] vectorZ = new int[found];
    int[] vectorZabs = new int[found];
    double hamming = slidingWindowHamming(itemsR, itemsL, sampled);
    getVectorZ(itemsR, itemsL, vectorZ, vectorZabs);
    double normalW = normalWilcoxon(vectorZ, vectorZabs);
    double meanRank = getMeanRank(vectorZabs);

    double variance = 0;
    // case statement for requested value
    variance = meanRank;

    variance = Math.sqrt(variance);
    if (null != csvOut)
      csvOut.println(tag + "," + userID + "," + sampled + "," + subset + "," + hamming + "," + meanRank + "," + normalW + "," + variance);

    return variance;
  } 


  private Long[] getCommonItems(FastIDSet commonSet, PreferenceArray prefs1, int max) {
    Long[] commonItems = new Long[Math.min(commonSet.size(), max)];
    int index = 0;
    for(int i = 0; i < prefs1.length(); i++) {
      Long item = prefs1.getItemID(i);
      if (commonSet.contains(item))
        commonItems[index++] = item;
      if (index == max)
        break;
    }
    if (index != commonItems.length)
      ((Object)null).hashCode();
    return commonItems;
  }

  // find minimal set of common recommended items - in order of first prefs array
  // cap at 'max'
  // return number of shared recommendations in the first->max entries
  private int minimalSet(PreferenceArray prefs1, PreferenceArray prefs2, FastIDSet commonSet, int max) {
    FastIDSet set1 = new FastIDSet();
    for(int i = 0; i < prefs1.length() && i < max; i++) {
      Long item = prefs1.getItemID(i);
      set1.add(item);
    }
    for(int i = 0; i < prefs2.length() && i < max; i++) {
      Long item = prefs2.getItemID(i);
      if (set1.contains(item))
        commonSet.add(item);
      if (commonSet.size() == max)
        break;
    }
    int subset = 0;
    for(int i = 0; i < max; i++) {
      if (commonSet.contains(prefs1.getItemID(i)) || commonSet.contains(prefs2.getItemID(i))) {
        subset++;
      }
     }
    return subset;
  }

  // find minimal set of common recommended items - in order of first prefs array
  private List<Long> getItemList(PreferenceArray prefs, int max) {
    List<Long> items = new ArrayList<Long>();
    for(int i = 0; i < prefs.length(); i++) {
      Long item = prefs.getItemID(i);
      items.add(item);
      if (items.size() == max)
        break;
    }
    return items;
  }

  private void getMatchesFromPrefsArray(Long userID, Long[] itemsL, 
      PreferenceArray prefsArray,
      FastIDSet common) throws TasteException {
    int index = 0;
    for(int i = 0; i < prefsArray.length(); i++) {
      Long itemID = prefsArray.getItemID(i);
      if (common.contains(itemID))
        itemsL[index++] = prefsArray.getItemID(i);
      this.hashCode();
    }
    if (index != common.size()) {
      System.err.println("Must have pulled common.size() prefs! got: " + index + " instead of: " + common.size());

    }
  }


  private void setBits(FastIDSet modelSet, List<RecommendedItem> items) {
    for(RecommendedItem item: items) {
      modelSet.add(item.getItemID());
    }
  }

  private void setBits(FastIDSet modelSet, Long[] items) {
    for(int i = 0; i < items.length; i++) {
      modelSet.add(items[i]);
    }
  }

  private void setBits(FastIDSet modelSet, Preference[] prefs) {
    for(int i = 0; i < prefs.length; i++) {
      modelSet.add(prefs[i].getItemID());
    }
  }

  // simple sliding-window hamming distance: a[i+-1] == b[i]
  private int slidingWindowHamming(Long[] itemsR, Long[] itemsL, int sampled) {
    int count = 0;
    try {
      if (itemsR[0] == itemsL[0] || itemsR[0] == itemsL[1])
        count++;
      for(int i = 1; i < sampled && i < itemsR.length - 1; i++) {
        long itemID = itemsL[i];
        if ((itemsR[i] == itemID) ||
            (itemsR[i-1] == itemID)||
            (itemsR[i+1] == itemID)) {
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
  double normalWilcoxon(int[] vectorZ, int[] vectorZabs) {
    double mean = 0;
    int nitems = vectorZ.length;

    double[] ranks = new double[nitems];
    double[] ranksAbs = new double[nitems];
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
  private void getVectorZ(Long[] itemsR, Long[] itemsL, int[] vectorZ, int[] vectorZabs) {
    int nitems = itemsR.length;
    int bottom = 0;
    int top = nitems - 1;
    for(int i = 0; i < nitems; i++) {
      long itemID = itemsR[i];
      for(int j = bottom; j <= top; j++) {
        if (itemsL[j] == null)
          continue;
        long test = itemsL[j];
        if (itemID == test) {
          vectorZ[i] = i - j;
          vectorZabs[i] = Math.abs(i - j);
          if (j == bottom) {
            bottom++;
          } else if (j == top) {
            top--;
          } else {
            itemsL[j] = null;
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

  private double getMeanRank(int[] ranks) {
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
