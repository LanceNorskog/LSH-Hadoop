package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.stats.OnlineSummarizer;


public class SVFmain {
  
  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_100k/ratings.dat"));
    int dimensions = 200;
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double rescale;
    
    SemanticVectorFactory svf = new SemanticVectorFactory(model, dimensions);
    //    Vector v = svf.getUserVector(100, 20, 50);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    //    Vector v2 = svf.getItemVector(1282, 10, 20);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    dimensions = svf.getDimensions();
    Vector unitVector = new DenseVector(dimensions);
    Vector zeroVector = new DenseVector(dimensions);
    for(int i = 0; i < dimensions; i++) 
      unitVector.set(i, 1.0);
    rescale = measure.distance(zeroVector, unitVector);
    //    System.out.println("User distances");
    //    countUserPrefs(model);
    //    checkUserDistances(svf, model, measure, rescale);
    //    System.out.println("Item distances");
    //    countItemPrefs(model);
    //    checkItemDistances(svf, model, measure, rescale);
    System.out.println("User v.s. item");
    userVSitem(svf, model, measure, rescale);
  }
  
  // stats on distances for user/item distances for actual prefs
  private static void userVSitem(SemanticVectorFactory svf, DataModel model,
      DistanceMeasure measure, double rescale) throws TasteException {
    FastByIDMap<Vector> userMap = new FastByIDMap<Vector>();
    FastByIDMap<Vector> itemMap = new FastByIDMap<Vector>();
    LongPrimitiveIterator userIter = model.getUserIDs();
    LongPrimitiveIterator itemIter = model.getItemIDs();
    while(userIter.hasNext()) {
      long userID = userIter.nextLong();
      userMap.put(userID, svf.projectUserDense(userID));
    }
    while(itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      itemMap.put(itemID, svf.getRandomItemVector(itemID));
    }
    userIter = model.getUserIDs();
    OnlineSummarizer trackD = new OnlineSummarizer();
    OnlineSummarizer trackR = new OnlineSummarizer();
    while(userIter.hasNext()) {
      long userID = userIter.nextLong();
      PreferenceArray prefs = model.getPreferencesFromUser(userID);
      double mean = getMeanValue(prefs);
      for(int j = 0; j < prefs.length(); j++) {
        Vector userV = userMap.get(userID);
        Vector itemV = itemMap.get(prefs.get(j).getItemID());
        double distance = measure.distance(userV, itemV);
        distance /= rescale;
        trackD.add(distance);
        float value = prefs.getValue(j);
        float value2 = ((value - 1) / 4.0f);
        float value3 = (float) Math.max(0.00001, value2 - (float) mean);
        if (Double.isNaN(value3) || value3 < 0.0 || value3 > 1.0)
          throw new RuntimeException("Wrong " + value3);
//        if (distance/value3 < 0.3)
          trackR.add(distance/(1-value3));
        
      }
    }
    
    System.out.println("Distances:  " + trackD.toString());
    System.out.println("Ratios:     " + trackR.toString());
    
  }
  
  private static double getMeanValue(PreferenceArray prefs) {
    double d = 0;
    for(int i = 0; i < prefs.length(); i++) {
      d += (prefs.getValue(i) - 1)/4f;
    }
    return d/prefs.length();
  }

  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      long userID = users.nextLong();
      va[i] = svf.projectUserDense(userID);
    }
    showDistributions(va, measure, rescale);
  }
  
  private static void checkItemDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      long itemID = items.nextLong();
      va[i] = svf.projectItemDense(itemID);
    }
    showDistributions(va, measure, rescale);
  }
  
  private static void showDistributions(Vector[] va,
      DistanceMeasure measure, double rescale) {
    int[] buckets = new int[21];
    OnlineSummarizer tracker = new OnlineSummarizer();
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        if ((null == va[i]) || (va[j] == null))
          continue;
        double distance = measure.distance(va[i], va[j]);
        distance /= rescale;
        buckets[(int) (distance*20)]++;
        tracker.add(distance);
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
    System.out.println("Distances:  " + tracker.toString());
  }
  
  private static void countUserPrefs(DataModel model) throws TasteException {
    OnlineSummarizer tracker = new OnlineSummarizer();
    LongPrimitiveIterator userIter = model.getUserIDs();
    while(userIter.hasNext()) {
      long userID = userIter.nextLong();
      int nPrefs = model.getPreferencesFromUser(userID).length();
      tracker.add(nPrefs);
    }
    System.out.println("# of prefs: " + tracker.toString());
  }
  
  private static void countItemPrefs(DataModel model) throws TasteException {
    OnlineSummarizer tracker = new OnlineSummarizer();
    LongPrimitiveIterator itemIter = model.getItemIDs();
    while(itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      int nPrefs = model.getPreferencesForItem(itemID).length();
      tracker.add(nPrefs);
    }
    System.out.println("# of prefs: " + tracker.toString());
  }
  
  
  
}
