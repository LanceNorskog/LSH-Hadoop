package working;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.MinkowskiDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

/*
 * Given a DataModel, create a semantic vector for a User or Item.
 * Vectors are always 0.0 -> 1.0, normal distribution
 * 
 * Semantic Vector formula: ((sum(random U)+ sum(pref(u,i)))/2)/#U
 */

public class SemanticVectorFactory {
  private final DataModel model;
  private final int dimensions;
  private final Random rnd;
  float FACTOR = 0.7f;

  public SemanticVectorFactory(DataModel model, int dimensions) {
    this(model, dimensions, new Random());
  }

  public SemanticVectorFactory(DataModel model, int dimensions, Random rnd) {
    this.model = model;
    this.dimensions = dimensions;
    this.rnd = rnd;
  }

  /*
   * Create a Semantic Vector for this User with Item as independent variable
   */
  public Vector getUserVector(long userID, int minimum, int samples) throws TasteException {
    FastIDSet prefs = model.getItemIDsFromUser(userID);
    int nItems = prefs.size();
    if (nItems < minimum)
      return null;
    long[] items = new long[nItems];
    LongPrimitiveIterator itemList = prefs.iterator();
    int index = 0;
    while (itemList.hasNext()) {
      items[index++] = itemList.nextLong();
    }
    prefs = null;
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    float prefSum = 0f;
    int count = 0;
    if (samples == 0 || samples >= nItems) {
      for(int i = 0; i < nItems; i++) {
        long itemID = items[i];
        Float pref = model.getPreferenceValue(userID, itemID);
        if (null == pref)
          continue;
        pref = (pref - minPreference)/(maxPreference - minPreference);
        prefSum += pref;
        count++;
      }
    } else {
      samples = Math.min(samples, nItems);
      int marker = 0;
      while(marker < samples) {
        long itemID;
        while(true) {
          long sample;
          sample = Math.abs(rnd.nextInt()) % nItems;
          if (items[(int) sample] >= 0) {
            itemID = items[(int) sample];
            items[(int) sample] = -1;
            break;
          }
        }
        marker++;
        Float pref = model.getPreferenceValue(userID, itemID);
        if (null == pref) {
          continue;
        }
        pref = (pref - minPreference)/(maxPreference - minPreference);
        prefSum += pref;
        count++;
      }
    }
    prefSum /= count;
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < count; j++) {
        rndSum += rnd.nextDouble();
      }
      rndSum /= count;
      float position = (rndSum*(1-FACTOR) + prefSum*FACTOR)/2;
      values[i] = position;
    }
    Vector v = new DenseVector(values);
    return v;
  }

  /*
   * Create a Semantic Vector for this Item with User as independent variable
   */
  public Vector getItemVector(final long itemID, int minimum, int samples) throws TasteException {
    PreferenceArray prefs = model.getPreferencesForItem(itemID);
    int nUsers = prefs.length();
    if (nUsers < minimum)
      return null;
    //    System.out.println("item: " + itemID + " nUsers: " + nUsers);
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    float prefSum = 0f;
    int count = 0;
    if (samples == 0 || samples >= nUsers) {
      Iterator<Preference> userList = prefs.iterator();
      while(userList.hasNext()) {
        Preference preference = userList.next();
        float value = preference.getValue();
        value = (value - minPreference)/(maxPreference - minPreference);
        //        System.out.println("pref: " + value);
        prefSum += value;
      }
      count = nUsers;
    } else {
      samples = Math.min(samples, nUsers);
      while(count < samples) {
        int index = 0;
        while(true) {
          index = rnd.nextInt(nUsers);
          long userID = prefs.getUserID(index);
          if (userID != -1) {
            break;
          }
        }
        float value = prefs.getValue((int) index);
        prefs.setUserID(index, -1);
        value = (value - minPreference)/(maxPreference - minPreference);
        //        System.out.println("index: " + index);
        prefSum += value;
        count++;
      }
    }
    prefSum /= count;
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < count; j++) {
        rndSum += rnd.nextDouble()/count;
      }
      float position = (rndSum*(1-FACTOR) + prefSum*FACTOR)/2;
      values[i] = position;
      //      System.out.println(i + ": " + position);
    }
    Vector v = new DenseVector(values);

    return v;
  }
  
  public Double linearDistance(Vector v1, Vector v2, DistanceMeasure measure, double rescale) {
    double dist = measure.distance(v1, v2)/rescale;
    dist = Distributions.normal2linear(dist);
    return dist;
  }
  

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public  void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_100k/ratings.dat"));
    int dimensions = 200;
    DistanceMeasure measure = new MinkowskiDistanceMeasure(1.5);
    double rescale;

    SemanticVectorFactory svf = new SemanticVectorFactory(model, dimensions, new Random(0));
    //    Vector v = svf.getUserVector(100, 20, 50);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    //    Vector v2 = svf.getItemVector(1282, 10, 20);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    dimensions = svf.dimensions;
    Vector unitVector = new DenseVector(dimensions);
    Vector zeroVector = new DenseVector(dimensions);
    for(int i = 0; i < dimensions; i++) 
      unitVector.set(i, 1.0);
    rescale = measure.distance(zeroVector, unitVector);
    checkUserDistances(svf, model, measure, rescale);
    System.out.println();
    checkItemDistances(svf, model, measure, rescale);
  }

  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 10, 40);
    }
    showDistributions(va, measure, rescale);
  }

  private static void checkItemDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      va[i] = svf.getItemVector(itemID, 10, 40);
    }
    showDistributions(va, measure, rescale);
  }

  private static void showDistributions(Vector[] va,
      DistanceMeasure measure, double rescale) {
    int[] buckets = new int[21];
    double delta = 0;
    int count = 0;
    double max = 0;
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        if ((null == va[i]) || (va[j] == null))
          continue;
        double distance = measure.distance(va[i], va[j]);
        if (max < distance)
          max = distance;

        distance /= rescale;
        distance = Distributions.normal2linear(distance);
        buckets[(int) (distance*20)]++;
        for(int k = 0; k < va[i].size(); k++) {
          double d = (Math.abs(va[i].get(k) - va[j].get(k)));
          delta += d;
          count++;
        }
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
    System.out.println("average point dist: " + (delta/count) + ", max: " + max);
  }

}
