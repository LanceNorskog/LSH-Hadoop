/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 * @author lance
 * 
 * Subsample entries in data model.
 * Two sampling modes: 
 *  holographic decimates by preference,
 *  linear decimates users and then a user's preferences
 *  user decimates users but includes all preferences
 *  
 *  Can give upper and lower bounds, allowing subsets for training/testing.
 *  Scalability: requires bitmap of prefs existence.
 */

public class Copy_2_of_SamplingDataModel implements DataModel {
  final DataModel delegate;
  final double lower;
  final double higher;
  final Mode samplingMode;
  Float defaultPref = 0.0f;
  // cache of whether a preference exists
  final FastByIDMap<BitSet> userIDMap = new FastByIDMap<BitSet>();
  final FastByIDMap<Integer> itemIDMap = new FastByIDMap<Integer>();

  /*
   * Sampling modes
   * HOLOGRAPHIC: decimate preferences
   * LINEAR: decimate users, then decimate items
   */
  public enum Mode {HOLOGRAPHIC, LINEAR, USER};

  public Copy_2_of_SamplingDataModel(DataModel delegate, double higher) throws TasteException {
    this(delegate, 0.0, higher, Mode.LINEAR);
  }

  public Copy_2_of_SamplingDataModel(DataModel delegate, double lower, double higher, Mode samplingMode) throws TasteException {
    this.delegate = delegate;
    this.lower = lower;
    this.higher = higher;
    this.samplingMode = samplingMode;
    fillBitCache();
  }

  public void setDefaultPref(Float value) {
    defaultPref = value;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getItemIDs()
   */
  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return delegate.getItemIDs();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getItemIDsFromUser(long)
   */
  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    return delegate.getItemIDsFromUser(userID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getMaxPreference()
   */
  @Override
  public float getMaxPreference() {
    // problem: what if we don't return anything at minimum?
    return delegate.getMaxPreference();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getMinPreference()
   */
  @Override
  public float getMinPreference() {
    // problem: what if we don't return anything at minimum?
    return delegate.getMinPreference();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getNumItems()
   */
  @Override
  public int getNumItems() throws TasteException {
    return delegate.getNumItems();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getNumUsers()
   */
  @Override
  public int getNumUsers() throws TasteException {
    return delegate.getNumUsers();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getNumUsersWithPreferenceFor(long[])
   */
  @Override
  public int getNumUsersWithPreferenceFor(long... itemIDs)
  throws TasteException {
    int count = 0;
    LongPrimitiveIterator users = userIDMap.keySetIterator(); // delegate.getUserIDs();
    while (users.hasNext()) {
      long userID = users.nextLong();
      for(int i = 0; i < itemIDs.length; i++) {
        boolean exists = prefExists(userID, itemIDs[i]);
        if (exists) {
          count++;
          break;
        }
      }
    }
    return count;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferenceTime(long, long)
   */
  @Override
  public Long getPreferenceTime(long userID, long itemID) throws TasteException {
    if (! prefExists(userID, itemID)) {
      return null;
    }
    return delegate.getPreferenceTime(userID, itemID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferenceValue(long, long)
   */
  @Override
  public Float getPreferenceValue(long userID, long itemID)
  throws TasteException {
    if (! prefExists(userID, itemID)) {
      return null;
    }
    return delegate.getPreferenceValue(userID, itemID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferencesForItem(long)
   */
  @Override
  public PreferenceArray getPreferencesForItem(long itemID)
  throws TasteException {
    PreferenceArray prefs = delegate.getPreferencesForItem(itemID);
    int count = 0;
    boolean[] exists = new boolean[prefs.length()];
    int total = 0;
    for(Preference pref: prefs) {
      if (prefExists(pref.getUserID(), pref.getItemID())) {
        exists[total] = true;
        count++;
      } 
      total++;
    }
    // some algs may blow up if get a 0 array
    if (count == 0) {
      count = 1;
      exists[0] = true;
    }
    PreferenceArray sampled = new GenericItemPreferenceArray(count);
    count = 0;
    for(int i = 0; i < sampled.length(); i++) {
      if (exists[i]) {
        long userID = prefs.getUserID(i);
        float value = prefs.getValue(i);
        sampled.setUserID(count, userID);
        sampled.setItemID(count, itemID);
        sampled.setValue(count, value);
        count++;
      }
    }
    return sampled;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferencesFromUser(long)
   */
  @Override
  public PreferenceArray getPreferencesFromUser(long userID)
  throws TasteException {
    PreferenceArray prefs = delegate.getPreferencesFromUser(userID);
    List<Preference> sampled = new ArrayList<Preference>();
    for(int i = 0; i < prefs.length(); i++) {
      long itemID = prefs.getItemID(i);
      if (prefExists(userID, itemID)) {
        Preference pref = new GenericPreference(userID, itemID, prefs.getValue(i));
        sampled.add(pref);
      }
    }
    PreferenceArray array = new GenericUserPreferenceArray(sampled);
    return array;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getUserIDs()
   */
  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    return delegate.getUserIDs();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#hasPreferenceValues()
   */
  @Override
  public boolean hasPreferenceValues() {
    return delegate.hasPreferenceValues();
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#removePreference(long, long)
   */
  @Override
  public void removePreference(long userID, long itemID) throws TasteException {
    delegate.removePreference(userID, itemID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#setPreference(long, long, float)
   */
  @Override
  public void setPreference(long userID, long itemID, float value)
  throws TasteException {
    delegate.setPreference(userID, itemID, value);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
   */
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    delegate.refresh(alreadyRefreshed);
  }

  /*
   * Cache implementation 
   * 
   */
  private void fillBitCache() throws TasteException {
    final Random rnd = new Random(0);
    LongPrimitiveIterator it = delegate.getUserIDs();
    int numItems = delegate.getNumItems();
    while(it.hasNext()) {
      long userID = it.next();
      double sample = rnd.nextDouble();
      if (samplingMode == Mode.HOLOGRAPHIC || isSampled(rnd)) {
        userIDMap.put(userID, new BitSet(numItems));
      }
    }
    int count = 0;
    it = delegate.getItemIDs();
    while(it.hasNext()) {
      itemIDMap.put(it.next(), count++);
    }
    it = userIDMap.keySetIterator();
    while(it.hasNext()) {
      long userID = it.next();
      BitSet itemBits = userIDMap.get(userID);
      PreferenceArray items = delegate.getPreferencesFromUser(userID);
      for(Preference pref: items) {
        if (isSampled(rnd)) {
          Integer itemIndex = itemIDMap.get(pref.getItemID());
          itemBits.set(itemIndex);
        }
      }
    }
  }

  private boolean isSampled(Random rnd) {
    double sample = rnd.nextDouble();
    return sample >= lower && sample < higher;
  }

  boolean prefExists(long userID, long itemID) throws TasteException {
    //    return true;
    BitSet itemBits = userIDMap.get(userID);
    Integer itemIndex = itemIDMap.get(itemID);
    if (null == itemBits)
      throw new NoSuchUserException();
    if (null == itemIndex)
      throw new NoSuchItemException();
    boolean value = itemBits.get(itemIndex);
    return value;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
