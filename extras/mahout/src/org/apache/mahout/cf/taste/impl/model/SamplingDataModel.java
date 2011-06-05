/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
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
import org.apache.mahout.common.RandomUtils;

/**
 * Subsample entries in data model.
 * Two sampling modes: 
 *  holographic decimates by preference,
 *  linear decimates users and then a user's preferences
 *  user decimates users but includes all preferences
 *  
 *  Can give upper and lower bounds, allowing subsets for training/testing.
 *  Memory-based. Requires:
 *      Sparse map of user IDs.
 *      Dense bitmap per userID of item ID.
 *      Optional mean/stdev per user ID.
 *  
 *  TODO: boolean prefs? how to add?
 */

public class SamplingDataModel implements DataModel {
  static BitSet NOBITSET = new BitSet();
  static PreferenceArray NOUSERPREFS = new GenericUserPreferenceArray(0);

  final DataModel delegate;
  final double lower;
  final double higher;
  final Distribution samplingMode;
  Float defaultPref = 0.0f;
  final FastByIDMap<BitSet> userIDMap = new FastByIDMap<BitSet>();
  final FastByIDMap<Integer> itemIDMap = new FastByIDMap<Integer>();
  final FastByIDMap<Double> meanMap = new FastByIDMap<Double>();

  long timeItems = 0;
  long timeUsers = 0;
  long countItems = 0;
  long countUsers = 0;

  /*
   * Sampling modes
   * HOLOGRAPHIC: decimate preferences
   * LINEAR: decimate users, then decimate items
   */
  public enum Distribution {HOLOGRAPHIC, USER};

  public SamplingDataModel(DataModel delegate, double higher) throws TasteException {
    this(delegate, 0.0, higher, Distribution.USER);
  }

  public SamplingDataModel(DataModel delegate, double lower, double higher, Distribution samplingMode) throws TasteException {
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
      if (userIDMap.get(userID) == NOBITSET)
        continue;
      for(int i = 0; i < itemIDs.length; i++) {
        // TODO: user BitSet scan-forward method
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
    try {
      if (! prefExists(userID, itemID)) {
        return null;
      }
    } catch (NoSuchUserException e) {
      return null;
    } catch (NoSuchItemException e) {
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
    long startTime = System.currentTimeMillis();
    if (! itemIDMap.containsKey(itemID))
      throw new NoSuchItemException();
    int itemIndex = itemIDMap.get(itemID);
    PreferenceArray prefs = delegate.getPreferencesForItem(itemID);
    ArrayList<Preference> prefList = new ArrayList<Preference>();
    int size = prefs.length();
    for(int i = 0; i < size; i++) {
        long userID = prefs.getUserID(i);
        BitSet itemSet = userIDMap.get(userID);
        if (! itemSet.get(itemIndex))
          continue;
        Preference pref = new GenericPreference(userID, prefs.getItemID(i), prefs.getValue(i));
        prefList.add(pref);
    }
    PreferenceArray sampled = new GenericItemPreferenceArray(prefList);

    timeItems += System.currentTimeMillis() - startTime;
    countItems++;
    return sampled;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferencesFromUser(long)
   */
  @Override
  public PreferenceArray getPreferencesFromUser(long userID)
  throws TasteException {
    long startTime = System.currentTimeMillis();
    if (! userIDMap.containsKey(userID))
      throw new NoSuchUserException();
    if (samplingMode == Distribution.USER && !isSampledUserID(userID))
      return NOUSERPREFS;
    BitSet itemSet = userIDMap.get(userID);
    PreferenceArray prefs = delegate.getPreferencesFromUser(userID);
    ArrayList<Preference> prefList = new ArrayList<Preference>();
    int size = prefs.length();
    for(int i = 0; i < size; i++) {
      long itemID = prefs.getItemID(i);
      Integer itemIndex = itemIDMap.get(itemID);
      if (itemSet.get( itemIndex)) {
        Preference pref = new GenericPreference(userID, itemID, prefs.getValue(i));
        prefList.add(pref);
      }
    }
    PreferenceArray sampled = new GenericUserPreferenceArray(prefList);
    timeUsers += System.currentTimeMillis() - startTime;
    countUsers++;
    return sampled;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getUserIDs()
   */
  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    // ahah!
    return userIDMap.keySetIterator();
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
   * Cache implementation of whether a preference exists
   * 
   * userIDMap: user ID -> bitset of whether a preference exists for an item ID
   * itemIDMap: item ID -> index into above bitset
   */
  private void fillBitCache() throws TasteException {
    final Random rnd = RandomUtils.getRandom();
    LongPrimitiveIterator it = delegate.getUserIDs();
    int numItems = delegate.getNumItems();
    while(it.hasNext()) {
      long userID = it.next();
      if (samplingMode == Distribution.HOLOGRAPHIC || isSampled(rnd)) {
        userIDMap.put(userID, new BitSet(numItems));
      } else {
        userIDMap.put(userID, NOBITSET);
        continue;
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
      if (itemBits == NOBITSET)
        continue;
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

  private boolean isSampledUserID(long userID) {
    BitSet userBits = userIDMap.get(userID);
    return userBits != NOBITSET;
  }

  boolean prefExists(long userID, long itemID) throws TasteException {
    if (! userIDMap.containsKey(userID))
      throw new NoSuchUserException();

    BitSet itemBits = userIDMap.get(userID);
    Integer itemIndex = itemIDMap.get(itemID);
    if (samplingMode == Distribution.USER && NOBITSET == itemBits)
      return false;
    if (null == itemIndex)
      throw new NoSuchItemException();
    boolean value = itemBits.get(itemIndex);
    return value;
  }

}
