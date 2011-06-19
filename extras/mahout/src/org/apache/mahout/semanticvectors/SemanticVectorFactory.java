package org.apache.mahout.semanticvectors;

import java.util.Iterator;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
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
  private float FACTOR = 0.99f;
  
  public SemanticVectorFactory(DataModel model, int dimensions) {
    this.model = model;
    this.dimensions = dimensions;
  }
  
  /*
   * Create a Semantic Vector from items preferred by this User
   * The vector represents the common preferences for all users
   */
  
  public Vector projectUserDense(final long userID, int minimum) throws TasteException {
    setRandomItemVecs();
    setRandomUserVecs();
    
    PreferenceArray prefs = model.getPreferencesFromUser(userID);
    int nPrefs = prefs.length();
    if (nPrefs < minimum)
      return null;
    //    System.out.println("user: " + userID + " nUsers: " + nUsers);
    double[] values = new double[dimensions];
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    Iterator<Preference> lpi = prefs.iterator();
    Vector userR = userRs.get(userID);
    int count = 0;
    while (lpi.hasNext()) {
      Preference preference = lpi.next();
      long itemID = preference.getItemID();
      Vector itemR = itemRs.get(itemID);
      float pref = preference.getValue();
      pref = (pref - minPreference)/(maxPreference - minPreference);
      for(int dim = 0; dim < dimensions; dim++) {
        double independent = itemR.get(dim);
        double dependent = userR.get(dim);
        double position = dependent + (independent - dependent)/2 * pref;
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
  
  /*
   * Create a Semantic Vector for this Item with User as independent variable
   */
  public Vector projectItemDense(final long itemID, int minimum) throws TasteException {
    setRandomItemVecs();
    setRandomUserVecs();
    
    PreferenceArray prefs = model.getPreferencesForItem(itemID);
    int nPrefs = prefs.length();
    if (nPrefs < minimum)
      return null;
    //    System.out.println("item: " + itemID + " nItems: " + nItems);
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
        double independent = userR.get(dim);
        double dependent = itemR.get(dim);
        double position = dependent + (independent - dependent)/2 * pref;
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
