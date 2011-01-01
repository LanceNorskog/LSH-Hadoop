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
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
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
  float FACTOR = 0f;

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
    prefSum /= count;
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < count; j++) {
        rndSum += rnd.nextDouble();
      }
      rndSum /= count;
//      rndSum = (float) Distributions.normal2linear(rndSum);
      float position = (rndSum*FACTOR + prefSum*(1.0f-FACTOR))/2;
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
    prefSum /= count;
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < count; j++) {
        rndSum += rnd.nextDouble()/count;
      }
      float position = (rndSum + prefSum)/2;
      values[i] = position;
      //      System.out.println(i + ": " + position);
    }
    Vector v = new DenseVector(values);

    return v;
  }
  
  public Double linearDistance(Vector v1, Vector v2, DistanceMeasure measure, double normalize) {
    double dist = measure.distance(v1, v2)/normalize;
    return dist;
  }
  

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_10k/ratings.dat"));
    int dimensions = 500;
    DistanceMeasure measure = new MinkowskiDistanceMeasure(4.0);
    double normalize;

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
    normalize = measure.distance(zeroVector, unitVector);
    checkUserDistances(svf, model, measure, normalize);
    checkItemDistances(svf, model, measure, normalize);
  }

  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double normalize) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 10, 50);
//      if (null != va[i])
//        va[i] = invert(va[i]);
    }
    showDistributions(va, measure, normalize);
  }

  private static Vector invert(Vector vector) {
    for(int i = 0; i < vector.size(); i++) {
      double d = Distributions.normal2linear(vector.get(i));
      vector.set(i, d);
    }
    return vector;
  }

  private static void checkItemDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double normalize) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      va[i] = svf.getItemVector(itemID, 10, 40);
//      if (null != va[i])
//        va[i] = invert(va[i]);
    }
    showDistributions(va, measure, normalize);
  }

  private static void showDistributions(Vector[] va,
      DistanceMeasure measure, double normalize) {
    int[] buckets = new int[20];
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        if ((null == va[i]) || (va[j] == null))
          continue;
        double distance = measure.distance(va[i], va[j]);
        distance /= normalize;
        distance = Distributions.normal2linear(distance);
        buckets[(int) (distance*20)]++;
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
  }

}
