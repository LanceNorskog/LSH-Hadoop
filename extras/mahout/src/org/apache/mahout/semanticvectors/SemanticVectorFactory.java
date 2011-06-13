package org.apache.mahout.semanticvectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.hadoop.item.UserVectorSplitterMapper;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
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
// Add generator for matching random vector

public class SemanticVectorFactory {
  private final DataModel model;
  private final FastByIDMap<Vector> userRs = new FastByIDMap<Vector>();
  private final FastByIDMap<Vector> itemRs = new FastByIDMap<Vector>();
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
  
//  TODO redo this to match projectItem
  public Vector projectUserDense(long userID, int minimum) throws TasteException {
    /*    setRandomUserVecs();
    Vector randomUserV = userRs.get(userID);
    Random rnd = RandomUtils.getRandom(userID);
    FastIDSet prefs = model.getItemIDsFromUser(userID);
    int nItems = prefs.size();
    if (nItems < minimum)
      return null;
    LongPrimitiveIterator itemIter = prefs.iterator();
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    float prefSum = 0f;
    int count = 0;
    Vector userR = userRs.get(userID);
    for(int dim = 0; dim < dimensions; dim++) {
      double userPos = userR.get(dim);
      while(itemIter.hasNext()) {
        long itemID = itemIter.next();;
        Vector itemR = itemRs.get(itemID);
        Float pref = model.getPreferenceValue(userID, itemID);
        assert(pref >= 0);
        pref = (pref - minPreference)/(maxPreference - minPreference);
        
        prefSum += userPos - pref;
        count++;
      }
      prefSum /= count;
      float position = prefSum;
      values[dim] = position;
    }
    
    Vector v = new DenseVector(values);
    return v;*/
    return null;
  }
  
  /*
   * Create a Semantic Vector for this Item with User as independent variable
   */
  public Vector projectItemDense(final long itemID, int minimum) throws TasteException {
    setRandomItemVecs();
    setRandomUserVecs();
    
    PreferenceArray prefs = model.getPreferencesForItem(itemID);
    int nUsers = prefs.length();
    if (nUsers < minimum)
      return null;
    //    System.out.println("item: " + itemID + " nUsers: " + nUsers);
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    Iterator<Preference> lpi = prefs.iterator();
    Vector itemR = itemRs.get(itemID);
    int count = 0;
    while (lpi.hasNext()) {
      Preference preference = lpi.next();
      long userID = preference.getUserID();
      Vector userR = userRs.get(userID);
      float pref = preference.getValue();
      pref = (pref - minPreference)/(maxPreference - minPreference);
      for(int dim = 0; dim < dimensions; dim++) {
        double value = 0;
        
        double uRd = userR.get(dim);
        double iRd = itemR.get(dim);
        double position = iRd + (uRd - iRd)/2 * pref;
        assert(position >= 0.0 && position <= 1.0);
        values[dim] += position;
      }
      count++;
    }
    for(int dim = 0; dim < dimensions; dim++) {
      values[dim] = values[dim] / count;
    }
    Vector v = new DenseVector(values);
    
    return v;
  }
  
  private void setRandomUserVecs() throws TasteException {
    if (userRs.size() == 0) {
      LongPrimitiveIterator userIter = model.getUserIDs();
      while (userIter.hasNext()) {
        long userID = userIter.nextLong();
        userRs.put(userID, getRandomUserVector(userID));
      }
    }   
  }
  
  private void setRandomItemVecs() throws TasteException {
    if (itemRs.size() == 0) {
      LongPrimitiveIterator itemIter = model.getItemIDs();
      while (itemIter.hasNext()) {
        long itemID = itemIter.nextLong();
        itemRs.put(itemID, getRandomItemVector(itemID));
      }
    }
  }
  
  // User and Item projection vectors are deterministic, with a disjoint seed space
  public Vector getRandomUserVector(long userID) {
    
    Vector v = new DenseVector(dimensions);
    Random rnd = new Random();
    for(int dim = 0; dim < dimensions; dim++) {
      rnd.setSeed(userID + 500000 + dim * 10);
      v.setQuick(dim, rnd.nextDouble());
    }
    return v;
  }
  
  public Vector getRandomItemVector(long itemID) {
    Vector v = new DenseVector(dimensions);
    Random rnd = new Random();
    for(int dim = 0; dim < dimensions; dim++) {
      rnd.setSeed(itemID + 300000 + dim*10);
      v.setQuick(dim, rnd.nextDouble());
    }
    return v;
  }
  
  public int getDimensions(){
    return dimensions;
  }
  
}
