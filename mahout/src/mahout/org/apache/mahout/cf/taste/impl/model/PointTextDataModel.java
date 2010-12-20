package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Lookup;
import lsh.core.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/*
 * Load semantic vectors tables.
 */

public class PointTextDataModel extends AbstractDataModel {
  // Use L(0.4) instead of L1 (Manhattan) or L2 (Euclidean)
  // L(0.4) seems to give a saddle for 200-300 dimension saddle
  private final double FRACTION_L = 0.5;
  // raw text corner-first LSH of users
  final Lookup userDB;
  // raw text corner-first LSH of items
  final Lookup itemDB;
  int dimensions;
  final double scale = 1;
  final double offset = 0;
  public double total = 0;
  public int count = 0;
  public int clamped = 0;

  public long[] buckets = new long[10];
  private double rescale = Double.NaN;

  public PointTextDataModel(String pointsPath) throws IOException {
    this(new File(pointsPath));
  }

  public PointTextDataModel(File pointsFile) {
    userDB = new Lookup(null, true, false, true, true, false, false, false, false);
    itemDB = new Lookup(null, true, false, true, true, false, false, false, false);
    try {
      FileReader itemReader = new FileReader(pointsFile);
      FileReader userReader = new FileReader(pointsFile);
      itemDB.loadPoints(itemReader, "I");
      itemReader.close();
      userDB.loadPoints(userReader, "U");
      userReader.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dimensions = userDB.getDimensions();
    rescale  = Math.pow(dimensions, 1/FRACTION_L);
  }

  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return new LPI(itemDB.ids.iterator());
  }

  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    int nitems = getNumItems();
    FastIDSet fids = new FastIDSet(nitems);
    for(int i = 0; i < nitems; i++) {
      fids.add(i);
    }
    return fids;
  }

  @Override
  public int getNumItems() throws TasteException {
    return itemDB.ids.size();
  }

  @Override
  public int getNumUsers() throws TasteException {
    return userDB.ids.size();
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
    return getPreferenceValuePoint(userID, itemID);
  }

  private Float getPreferenceValuePoint(long userID, long itemID) {
    Point userP = userDB.id2point.get((userID) + "");
    Point itemP = itemDB.id2point.get((itemID) + "");
    if (null == userP || null == itemP)
      return 0.5f;
    double distance = fractionalD(itemP.values, userP.values);
    total += distance;
    count++;
    //		System.err.println(userID +"," + itemID + "," + distance);
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
    Point up = userDB.id2point.get((userID) + "");
    if (null == up)
      return new GenericUserPreferenceArray(0);
    PreferenceArray prefs = new GenericUserPreferenceArray(itemDB.ids.size());
    for(String item: itemDB.ids) {
      Point ip = itemDB.id2point.get(item);
      double distance = fractionalD(up.values, ip.values);
      total += distance;
      float rating = (float) distance2rating(distance);
      prefs.setUserID(prefIndex, userID);
      prefs.setItemID(prefIndex, Long.parseLong(item));
      prefs.setValue(prefIndex, rating);
      prefIndex++;
    }
    return prefs;
  }

  // L<1 distance
  public double minkowskiD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < dimensions; i++) {
      sum += Math.pow(Math.abs(a[i] - b[i]), FRACTION_L);
    }
    double dist = Math.pow(sum, 1/FRACTION_L);
    return dist / rescale;
//    double r = sum / dimensions;
//    return r;
  }

  // L<1 distance w/o powers - bogus Minkowski
  public double fractionalD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < dimensions; i++) {
      sum += Math.abs(a[i] - b[i]);
    }
    double dist = Math.pow(sum, 1/FRACTION_L);
    return dist / rescale;
//    double r = sum / dimensions;
//    return r;
  }

  // L1 rectangular distance
  public double manhattanD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < dimensions; i++) {
      sum += Math.abs(a[i] - b[i]);
    }
    double r = sum / dimensions;
    return r;
  }

  // L2 euclidean distance
  public double euclidD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < dimensions; i++) {
      sum += (a[i] - b[i]) * (a[i] - b[i]);
    }			
    double dist = Math.sqrt(sum);
    return dist / Math.sqrt(dimensions);
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
    return new LPI(userDB.ids.iterator());
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
  
  public int getDimensions() {
    return dimensions;
  }

  public void setDimensions(int dim) throws TasteException {
    if (dim > userDB.getDimensions())
      throw new TasteException();
    this.dimensions = dim;
  }

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    PointTextDataModel model = new PointTextDataModel(args[0]);
    model.hashCode();
    double pow =  Math.pow(100, 0.5);
    System.out.println("pow: " + Math.pow(200, 0.5));
    System.out.println("Items");
    doscan(model.itemDB, model.dimensions);
    System.out.println("Users");
    doscan(model.userDB, model.dimensions);
    dodistances(model);
  }

  static void dodistances(PointTextDataModel model) throws TasteException {
    Float min= 10000f, max = 0f;
    for(String userID: model.userDB.ids) {
      for(String itemID: model.itemDB.ids) {
        long u = Long.parseLong(userID);
        long id = Long.parseLong(itemID);
        Float f = model.getPreferenceValue(u, id);
        if (min > f)
          min = f;
        if (max < f)
          max = f;
      }
    }
    System.out.println("User->Item prefs");
    System.out.println("min:     " + min);
    System.out.println("max:     " + max);
    System.out.println("clamped: " + model.clamped);
    System.out.println("total:   " + model.total);
    System.out.println("count:   " + model.count);
    System.out.println("mean:    " + (model.total / model.count));
    System.out.println("buckets: " + (Arrays.toString(model.buckets)));
    System.out.println("range:   " + (max - min));
  }

  static void doscan(Lookup points, int dimensions) {
    double min = 100000;
    double max = -100000;
    double sum = 0;
    for(Point p: points.points) {
      double[] values = p.values;
      for(int i = 0; i < dimensions;i++) {
        min = Math.min(values[i], min);
        max = Math.max(values[i], max);
        sum += values[i];
      }
    }
    System.out.println("min: " + min + ", max: " + max + ", mean: " + sum / (points.points.size() * dimensions));

  }

}
