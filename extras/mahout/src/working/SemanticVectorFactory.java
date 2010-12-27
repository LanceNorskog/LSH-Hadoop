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
  int count = 0;
  int skip = 0;

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
    LongPrimitiveIterator itemList = prefs.iterator();
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    float prefSum = 0f;
    while(itemList.hasNext()) {
      long itemID = itemList.next();
      float pref = model.getPreferenceValue(userID, itemID);
      pref = (pref - minPreference)/(maxPreference - minPreference);
      prefSum += pref;
    }
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < nItems; j++) {
        rndSum += rnd.nextDouble();
      }
      float position = ((rndSum + prefSum)/2)/nItems;
      values[i] = position;
      System.out.println(i + ": " + position);
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
    System.out.println("item: " + itemID + " nUsers: " + nUsers);
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
        System.out.println("pref: " + value);
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
        System.out.println("index: " + index);
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
      System.out.println(i + ": " + position);
    }
    Vector v = new DenseVector(values);

    return v;
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
  }

  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 0, 100000);
    }
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        if (null == va[i] || null == va[j])
          continue;
        System.out.println(Math.sqrt(va[i].getDistanceSquared(va[j])));
      }
    }
  }

}
