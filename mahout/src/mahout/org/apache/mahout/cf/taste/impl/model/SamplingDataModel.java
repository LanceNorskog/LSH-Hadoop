/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 * @author lance
 * 
 * Subsample entries in data model.
 */
public class SamplingDataModel implements DataModel {
  final DataModel delegate;
  final double lower;
  final double higher;
  Float defaultPref = 0.0f;
  final Random rnd = new Random(0);
  final long numUsers;

  public SamplingDataModel(DataModel delegate, double higher) throws TasteException {
    this.delegate = delegate;
    this.lower = 0.0;
    this.higher = higher;
    numUsers = delegate.getNumUsers();
  }

  public SamplingDataModel(DataModel delegate, double lower, double higher) throws TasteException {
    this.delegate = delegate;
    this.lower = lower;
    this.higher = higher;
    numUsers = delegate.getNumUsers();
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
    LongPrimitiveIterator users = delegate.getUserIDs();
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
    if (null != defaultPref)  {
      if (! prefExists(userID, itemID)) {
        return 0L;
      }
    }
    return delegate.getPreferenceTime(userID, itemID);
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.model.DataModel#getPreferenceValue(long, long)
   */
  @Override
  public Float getPreferenceValue(long userID, long itemID)
  throws TasteException {
    if (null != defaultPref)  {
      if (! prefExists(userID, itemID)) {
        return defaultPref;
      }
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
    List<Preference> sampled = new ArrayList<Preference>();
    for(int i = 0; i < prefs.length(); i++) {
      long userID = prefs.getUserID(i);
      if (prefExists(userID, itemID)) {
        Preference pref = new GenericPreference(userID, itemID, prefs.getValue(i));
        sampled.add(pref);
      }
    }
    PreferenceArray array = new GenericItemPreferenceArray(sampled);
    return array;
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
   * Sampler - hokey
   * and slow!
   */

  boolean prefExists(long userID, long itemID) {
//    return true;
    rnd.setSeed(numUsers * 13 * userID + itemID);
    double sample = rnd.nextDouble();
    return sample >= lower && sample <= higher;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
