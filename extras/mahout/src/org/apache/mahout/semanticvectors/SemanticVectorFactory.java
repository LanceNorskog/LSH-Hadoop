package org.apache.mahout.semanticvectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
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

// TODO: Split this in raw dual engine and datamodel wrapper
// Remove subsampling

public class SemanticVectorFactory {
  private final DataModel model;
  private final int dimensions;
  private final Random rnd;
  float FACTOR = 0.99f;
  
  public SemanticVectorFactory(DataModel model, int dimensions) {
    this(model, dimensions, new Random());
  }
  
  public SemanticVectorFactory(DataModel model, int dimensions, Random rnd) {
    this.model = model;
    this.dimensions = dimensions;
    this.rnd = rnd;
  }
  
  List<Integer> scramble(int n) {
    List<Integer> x = new ArrayList<Integer>(n);
    for(int i = 0; i < n; i++)
      x.add(i);
    Collections.shuffle(x);
    return x;
  }
  
  /*
   * Create a Semantic Vector for this User with Item as independent variable
   * Subsample using Bernoulli sampling if more Items than samples requested
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
      // Bernoulli sampling
      for(int i = 0; i < samples; i++) {
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
    List<Integer> mixed = scramble(Math.max(samples, nUsers));
    int count = mixed.size();
    
    for(Integer index: mixed) {
      Preference preference = prefs.get(index);
      float value = preference.getValue();
      value = (value - minPreference)/(maxPreference - minPreference);
      //        System.out.println("pref: " + value);
      prefSum += value;
      
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
  
  //  public Double linearDistance(Vector v1, Vector v2, DistanceMeasure measure, double rescale) {
  //    double dist = measure.distance(v1, v2)/rescale;
  //    dist = Distributions.normal2linear(dist);
  //    return dist;
  //  }
  
  public int getDimensions(){
    return dimensions;
  }
  
}
