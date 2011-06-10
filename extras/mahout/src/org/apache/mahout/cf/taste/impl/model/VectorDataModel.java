package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;


need to add preferences

/*
 * Load semantic vectors point vectors, LSH format.
 * 
 * Mahout-ified version.
 * 
 * Values: 0.0 <= pref <= 1.0, normal distribution. 0.5 default.
 */

public class VectorDataModel extends AbstractDataModel implements ItemSimilarity {
  // Use L(0.5) instead of L1 (Manhattan) or L2 (Euclidean)
  // L(0.5) seems to give a saddle for 200-300 dimension saddle
  public static final double FRACTION_L = 0.5;
  public static final float DEFAULT_PREF = 0.5f;
  int dimensions;
  final DistanceMeasure measure;
  Map<Long,Vector> users = new HashMap<Long, Vector>();
  Map<Long,Vector> items = new HashMap<Long, Vector>();
  FastByIDMap<Long> userXitem = new FastByIDMap<Long>();
  FastByIDMap<Long> itemXuser = new FastByIDMap<Long>();
  // debug
  public double total = 0;
  public int count = 0;
  public int clamped = 0;
  public long[] buckets = new long[10];
  final private double rescale;

  public VectorDataModel(int dimensions, DistanceMeasure measure) {
    this.measure = measure;
    this.dimensions = dimensions;
    this.rescale = dimensions * dimensions;
  }
  
  public void addEntry(long userID, Vector userV, long itemID, Vector itemV) {
    userXitem.put(userID, itemID);
    itemXuser.put(itemID, userID);
    users.put(userID, userV);
    items.put(itemID, itemV);
  }

  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return new LPIL(items.keySet().iterator());
  }

  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    int nitems = getNumItems();
    FastIDSet fids = new FastIDSet(nitems);
    for(Long itemID: items.keySet()) {
      fids.add(itemID);
    }
    return fids;
  }

  @Override
  public int getNumItems() throws TasteException {
    return items.size();
  }

  @Override
  public int getNumUsers() throws TasteException {
    return users.size();
  }

  @Override
  public int getNumUsersWithPreferenceFor(long... itemIDs)
  throws TasteException {
    return getNumUsers();
  }

  @Override
  public Long getPreferenceTime(long userID, long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float getPreferenceValue(long userID, long itemID)
  throws TasteException {
    return getPreferenceValuePoint(users.get(userID), items.get(itemID));
  }

  private Float getPreferenceValuePoint(Vector v1, Vector v2) {
    if (null == v1 || null == v2) {
      return DEFAULT_PREF;
    }
    double distance = measure.distance(v1, v2) / rescale;
    total += distance;
    count++;
    System.err.println(v1 +"," + v2 + "," + distance);
    double rating = distance2rating(distance);
    return (float) rating;
  }

  @Override
  public PreferenceArray getPreferencesForItem(long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PreferenceArray getPreferencesFromUser(long userID)
  throws TasteException {
    PreferenceArray prefs = getPreferencesFromUserPoint(userID);
    prefs.sortByItem();
    return prefs;
  }

  private PreferenceArray getPreferencesFromUserPoint(long userID)
  throws NoSuchUserException {
    int prefIndex = 0;
    Vector v = users.get(userID);
    if (null == v)
      return new GenericUserPreferenceArray(0);
    PreferenceArray prefs = new GenericUserPreferenceArray(items.size());
    for(Entry<Long, Vector> itemEntry: items.entrySet()) {
      Long itemID = itemEntry.getKey();
      float rating = (float) getPreferenceValuePoint(users.get(userID), items.get(itemID));
      prefs.setUserID(prefIndex, userID);
      prefs.setItemID(prefIndex, itemID);
      prefs.setValue(prefIndex, rating);
      prefIndex++;
    }
    return prefs;
  }

  double distance2rating(double d) {
    if (d < 0.0 || d > 1.0d) {
      d = Math.max(0.01, d);
      d = Math.min(0.99, d);
      clamped ++;
      this.hashCode();
    }
    buckets[(int) (d * (buckets.length-1))]++;
    double e;
    e = (1-d);
    return e;
  }

  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    return new LPIL(users.keySet().iterator());
  }

  @Override
  public boolean hasPreferenceValues() {
    return true;
  }

  @Override
  public void removePreference(long userID, long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();

  }

  @Override
  public void setPreference(long userID, long itemID, float value)
  throws TasteException {
    throw new UnsupportedOperationException();

  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    throw new UnsupportedOperationException();

  }

  /* ItemSimilarity methods */
  @Override
  public double[] itemSimilarities(long itemID1, long[] itemID2s)
      throws TasteException {
    double[] prefs = new double[itemID2s.length];
    for(int i = 0; i < itemID2s.length; i++) {
      float distance = getPreferenceValuePoint(items.get(itemID1), items.get(itemID2s[i]));
      prefs[i] = (double) distance;
    }
    return prefs;
  }

  @Override
  public double itemSimilarity(long itemID1, long itemID2)
      throws TasteException {
    float distance = getPreferenceValuePoint(items.get(itemID1), items.get(itemID2));
    return (double) distance;
  }


  @Override
  public long[] allSimilarItemIDs(long itemID) throws TasteException {
    // TODO Auto-generated method stub
    return null;
  }

 }
