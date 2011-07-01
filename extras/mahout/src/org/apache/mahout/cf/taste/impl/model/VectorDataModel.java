package org.apache.mahout.cf.taste.impl.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;

//TODO: need to add preferences

/*
 * Maintain vectors of user v.s. item. 
 * 
 * Values are always normalized to 0.0 <= pref <= 1.0.
 */

public class VectorDataModel extends AbstractDataModel implements UserSimilarity, ItemSimilarity{
  final private int dimensions;
  final private DistanceMeasure measure;
  final private FastByIDMap<Vector> users = new FastByIDMap<Vector>();
  final private FastByIDMap<Vector> items = new FastByIDMap<Vector>();
  final double maximum;
  long[] buckets = new long[10];
  
  public VectorDataModel(int dimensions) {
    this.measure = new EuclideanDistanceMeasure();
    this.dimensions = dimensions;
    this.maximum = dimensions * dimensions;
  }
  
  public VectorDataModel(int dimensions, DistanceMeasure measure, double maximum) {
    this.measure = measure;
    this.dimensions = dimensions;
    this.maximum = maximum;
  }
  
  public void addItem(long itemID, Vector itemV) {
    items.put(itemID, itemV);
  }
  
  public void addUser(long userID, Vector userV) {
    users.put(userID, userV);
  }
  
  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    long[] la = getSortedLongArray(items);
    return new LongPrimitiveArrayIterator(la);
  }

  private long[] getSortedLongArray(FastByIDMap<Vector> keys) {
    long[] la = new long[keys.size()];
    int i = 0;
    LongPrimitiveIterator lpi = keys.keySetIterator();
    while(lpi.hasNext()) {
      la[i++] = lpi.nextLong();
    }
    Arrays.sort(la);
    return la;
  }
  
  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    if (! users.containsKey(userID))
      return null;
    FastIDSet fids = new FastIDSet(items.size());
    // TODO: switch to sorted long array
    LongPrimitiveIterator lpi = getItemIDs();
    while(lpi.hasNext()) {
      fids.add(lpi.nextLong());
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
    for(long itemID: itemIDs) {
      if (items.containsKey(itemID))
        return getNumUsers();
    }
    return 0;
  }
  
  @Override
  public Long getPreferenceTime(long userID, long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Float getPreferenceValue(long userID, long itemID)
  throws TasteException {
    Vector u = users.get(userID);
    Vector i = items.get(itemID);
    if (null == u)
      throw new TasteException("UserID nonexistent: " + userID);
    if (null == i)
      throw new TasteException("ItemID nonexistent: " + itemID);
    return getPreferenceValuePoint(users.get(userID), items.get(itemID));
  }
  
  private Float getPreferenceValuePoint(Vector v1, Vector v2) {
    double distance = measure.distance(v1, v2);
    double rating = distance2rating(distance);
    return (float) rating;
  }
  
  double distance2rating(double d) {
    d = d / maximum;
    if (d <= 0.0 || d > 1.0d) {
      d = Math.max(0.000001, d);
      d = Math.min(0.999999, d);
      this.hashCode();
    }
    buckets[(int) (d * (buckets.length-1))]++;
    double r;
    r = (1-d);
    return r;
  }
  
  @Override
  public PreferenceArray getPreferencesForItem(long itemID)
  throws TasteException {
    GenericUserPreferenceArray pa = new GenericUserPreferenceArray(users.size());
    if (! items.containsKey(itemID))
      return null;
    Vector vi = items.get(itemID);
    int index = 0;
    long[] la = getSortedLongArray(users);
    for(long userID: la) {
      pa.setItemID(index, itemID);
      double d = getPreferenceValuePoint(users.get(userID), vi);
      pa.setValue(index, (float) d);
      pa.setUserID(index, userID);
      index++;
    }
    return pa;
  }
  
  @Override
  public PreferenceArray getPreferencesFromUser(long userID)
  throws TasteException {
    if (null == users.get(userID))
      throw new TasteException("UserID nonexistent: " + userID);
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
  
  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    return new LongPrimitiveArrayIterator(getSortedLongArray(users));
  }
  
  @Override
  public boolean hasPreferenceValues() {
    return true;
  }
  
  @Override
  public void removePreference(long userID, long itemID)
  throws TasteException {
    // cannot recalculate values
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public void setPreference(long userID, long itemID, float value)
  throws TasteException {
    // cannot recalculate values
    throw new UnsupportedOperationException();
    
  }
  
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    // just recreate with 
    throw new UnsupportedOperationException();
    
  }
  
  public int getDimensions() {
    return dimensions;
  }

  
  /* ItemSimilarity methods */
    @Override
    public double[] itemSimilarities(long itemID1, long[] itemID2s)
        throws TasteException {
      requireItems();
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
      requireItems();
      float distance = getPreferenceValuePoint(items.get(itemID1), items.get(itemID2));
      return (double) distance;
    }
  
  
    // this requires a distance value - perhaps could give it in constructor?
    @Override
    public long[] allSimilarItemIDs(long itemID) throws TasteException {
      requireItems();
      throw new UnsupportedOperationException();
    }

    /* UserSimilarity methods */
    @Override
    public double userSimilarity(long userID1, long userID2)
        throws TasteException {
      requireUsers();
      return 0;
    }

    @Override
    public void setPreferenceInferrer(PreferenceInferrer inferrer) {
      throw new UnsupportedOperationException();
      
    }
    
    private void requireUsers() {
      if (users.size() == 0)
        throw new UnsupportedOperationException("VectorDataModel: UserSimilarity method requires user vectors");
    }
  
    private void requireItems() {
      if (items.size() == 0)
        throw new UnsupportedOperationException("VectorDataModel: ItemSimilarity method requires item vectors");
    }
  
}
