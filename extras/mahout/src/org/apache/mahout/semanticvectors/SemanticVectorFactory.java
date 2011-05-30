package org.apache.mahout.semanticvectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

/*
 * Given a DataModel, create a semantic vector for a User or Item.
 * Vectors are always 0.0 -> 1.0, normal distribution
 * 
 * Semantic Vector formula: ((sum(random U)+ sum(pref(u,i)))/2)/#U
 */

// TODO: Split this in raw dual engine and datamodel wrapper
// Add generator for matching random vector

public class SemanticVectorFactory {
  private final DataModel model;
  private final int dimensions;
  // allow testing by adjusting the amount of randomness in output vector
  float FACTOR = 0.99f;
  
  public SemanticVectorFactory(DataModel model, int dimensions) {
    this.model = model;
    this.dimensions = dimensions;
  }

  /*
   * Create a Semantic Vector from items preferred by this User
   * The vector represents the common preferences for all users
   */
  public Vector projectUserDense(long userID, int minimum) throws TasteException {
    Random rnd = getRandomItemVector(userID);
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
    for(int i = 0; i < nItems; i++) {
      long itemID;
      int sample;
      sample = rnd.nextInt(nItems);
      if (items[sample] == -1)
        continue;
      else {
        itemID = items[(int) sample];
        items[(int) sample] = -1;
      }
      Float pref = model.getPreferenceValue(userID, itemID);
      if (null == pref) {
        continue;
      }
      pref = (pref - minPreference)/(maxPreference - minPreference);
      prefSum += pref;
      count++;
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
  public Vector projectItemDense(final long itemID, int minimum) throws TasteException {
    Random rnd = getRandomUserVector(itemID);
    PreferenceArray prefs = model.getPreferencesForItem(itemID);
    int nUsers = prefs.length();
    if (nUsers < minimum)
      return null;
    //    System.out.println("item: " + itemID + " nUsers: " + nUsers);
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    Iterator<Preference> lpi = prefs.iterator();
    float prefSum = 0f;
    
    while (lpi.hasNext()) {
      Preference preference = lpi.next();
      float value = preference.getValue();
      value = (value - minPreference)/(maxPreference - minPreference);
      prefSum += value;
      
    }
    prefSum /= nUsers;
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < nUsers; j++) {
        rndSum += rnd.nextDouble()/nUsers;
      }
      float position = (rndSum*(1-FACTOR) + prefSum*FACTOR)/2;
      values[i] = position;
    }
    Vector v = new DenseVector(values);
   
    return v;
  }

  // User and Item projection vectors are deterministic, with a disjoint seed space
  public Random getRandomUserVector(long itemID) {
    Random rnd = RandomUtils.getRandom(itemID);
    return rnd;
  }
  
  public Random getRandomItemVector(long userID) {
    Random rnd = RandomUtils.getRandom(-userID);
    return rnd;
  }
  
  public int getDimensions(){
    return dimensions;
  }
  
}
