package working;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.tools.ant.taskdefs.GenerateKey.DistinguishedName;

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
  int count = 0;
  int skip = 0;
  FastIDSet modUsers = new FastIDSet();
  FastIDSet modItems = new FastIDSet();

  public SemanticVectorFactory(DataModel model, int dimensions) {
    this(model, dimensions, new Random());
  }

  public SemanticVectorFactory(DataModel model, int dimensions, Random rnd) {
    this.model = model;
    this.dimensions = dimensions;
    this.rnd = rnd;
  }

  /*
   * Create a Semantic Vector for this user with Item as independent variable
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
    if (samples == 0 || samples >= nItems) {
      for(int i = 0; i < nItems; i++) {
        long itemID = items[i];
        float pref = model.getPreferenceValue(userID, itemID);
        pref = (pref - minPreference)/(maxPreference - minPreference);
        prefSum += pref;
      }
      count = nItems;
    } else {
      samples = Math.min(samples, nItems);
      while(count < samples) {
        long itemID;
        while(true) {
          long sample;
          sample = rnd.nextInt(nItems);
          if (items[(int) sample] >= 0) {
            itemID = items[(int) sample];
            items[(int) sample] = -1;
            break;
          }
        }
        Float pref = model.getPreferenceValue(userID, itemID);
        if (null == pref) {
          System.out.println("userID not there: " + userID);
        }
        pref = (pref - minPreference)/(maxPreference - minPreference);
        prefSum += pref;
        count++;
      }
    }
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < nItems; j++) {
        rndSum += rnd.nextDouble();
      }
      float position = ((rndSum + prefSum)/2)/nItems;
      values[i] = position;
      //      System.out.println(i + ": " + position);
    }
    Vector v = new DenseVector(values);
    return v;
  }

  /*
   * Create a Semantic Vector for this item with User as independent variable
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
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < count; j++) {
        rndSum += rnd.nextDouble();
      }
      float position = ((rndSum + prefSum)/2)/count;
      values[i] = position;
      //      System.out.println(i + ": " + position);
    }
    Vector v = new DenseVector(values);

    return v;
  }
  
  public Double linearDistance(Vector v1, Vector v2, DistanceMeasure measure) {
    double dist = measure.distance(v1, v2);
    return Distributions.normal2linear(dist);
  }
  

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_10k/ratings.dat"));

    SemanticVectorFactory svf = new SemanticVectorFactory(model, 100, new Random(0));
    //    Vector v = svf.getUserVector(100, 20, 50);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    //    Vector v2 = svf.getItemVector(1282, 10, 20);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    checkUserDistances(svf, model);
//    checkItemDistances(svf, model);
  }

  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 0, 20);
//      va[i] = invert(va[i]);
    }
    int[] buckets = new int[20];
    int dimensions = va[0].size();
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        double dist = Math.sqrt(va[i].getDistanceSquared(va[j])/dimensions);
        double distance = Distributions.normal2linear(dist);
        buckets[(int) (distance*20)]++;

        System.out.println(distance);
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
  }

  private static Vector invert(Vector vector) {
    for(int i = 0; i < vector.size(); i++) {
      double d = Distributions.normal2linear(vector.get(i));
      vector.set(i, d);
    }
    return vector;
  }

  private static void checkItemDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      va[i] = svf.getItemVector(itemID, 0, 20);
      va[i] = invert(va[i]);
    }
    int[] buckets = new int[20];
    int dimensions = va[0].size();
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        double distance = Math.sqrt(va[i].getDistanceSquared(va[j]))/Math.sqrt(dimensions);
        buckets[(int) (distance*20)]++;

                System.out.println(distance);
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
  }

}
