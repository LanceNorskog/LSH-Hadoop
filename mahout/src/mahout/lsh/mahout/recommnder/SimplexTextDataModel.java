package lsh.mahout.recommnder;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.Lookup;
import lsh.core.Utils;
import lsh.core.VertexTransitiveHasher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/*
 * Load LSH corners-based text format using LSH bag-of-objects.
 * Very inefficient!
 * Load full database of points.
 * Can't load very many!
 */

public class SimplexTextDataModel extends AbstractDataModel {
  // raw text corner-first LSH of users
  final Lookup userDB;
  // raw text corner-first LSH of items
  final Lookup itemDB;
  // LSH projector
  //	final Hasher hasher;
  // corner-generator - does LSH projection
  final CornerGen cg;
  // real 1.0 = hash(1.0) * variance
  double varianceEuclid;
  double varianceManhattan;
  // real 1.0 dist 
  final double diagonal;
  // project ratings from 0->1 into 1...5
  final double scale = 4;
  final double offset = 1;
  final boolean earlyBinding = false;

  public SimplexTextDataModel(String cornersFile, CornerGen cg) throws IOException {
    //		this.hasher = hasher;
    this.cg = cg;
    userDB = new Lookup(null, false, false, false, false, true, false, false, false);
    itemDB = new Lookup(null, false, false, false, false, true, true, false, false);
    Reader f;
    f = new FileReader(new File(cornersFile));
    Utils.load_corner_points_format(f, "I", itemDB, "U", userDB);
    f.close();
    int dimension = cg.stretch.length;
    double[] zero = new double[dimension];
    for(int i = 0; i < zero.length; i++)
      zero[i] = 0.0;
    double[] unit = new double[dimension];
    for(int i = 0; i < unit.length; i++)
      unit[i] = 1.0;
    int[] zeroHash = cg.hasher.hash(zero);
    int[] unitHash = cg.hasher.hash(unit);
    varianceEuclid = manhattan(zeroHash,unitHash);
    varianceEuclid = Math.sqrt(2)/varianceEuclid;
    double dist = manhattanD(zero, unit);
    varianceManhattan = 1/dist;
    varianceManhattan = 1.0/dimension;
    diagonal = 1/Math.sqrt(dimension);
  }

  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return new LPIS(itemDB.id2corner.keySet().iterator());
  }

  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public int getNumItems() throws TasteException {
    return itemDB.id2corner.keySet().size();
  }

  @Override
  public int getNumUsers() throws TasteException {
    return userDB.id2corner.keySet().size();
  }

  @Override
  public int getNumUsersWithPreferenceFor(long... itemIDs)
  throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long getPreferenceTime(long userID, long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float getPreferenceValue(long userID, long itemID)
  throws TasteException {
    if (earlyBinding)
    {		return getPreferenceValueCorner(userID, itemID);

    } else
      return getPreferenceAverageCorner(userID, itemID);
  }

  private Float getPreferenceAverageCorner(long userID, long itemID) {
    Corner user = userDB.id2corner.get(userID + "");
    Corner item = itemDB.id2corner.get(itemID + "");
    Set<Corner> all = cg.getHashSet(item.hashes.clone());
    double sum = 0;
    for(Corner c: all) {
      double distance = manhattan(user.hashes, c.hashes);
      sum += distance;
    }
    double mean = sum / all.size();
    return (float) distance2rating(mean);
  }

  // early binding: all corners exist for each item.
  private Float getPreferenceValueCorner(long userID, long itemID)
  throws TasteException {
    Corner user = userDB.id2corner.get(userID + "");
    Corner item = itemDB.id2corner.get(itemID + "");
    double distance = manhattan(user.hashes, item.hashes);
    return (float) distance2rating(distance);
  }

  @Override
  public PreferenceArray getPreferencesForItem(long itemID)
  throws TasteException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PreferenceArray getPreferencesFromUser(long userID)
  throws TasteException {
    if (earlyBinding) {
      return getPreferencesFromUserCorner(userID);
    } else {
      return enumeratePreferencesFromUserCorner(userID);			
    }
  }

  // all item corners already point to neighbor items.
  private PreferenceArray getPreferencesFromUserCorner(long userID) {
    throw new UnsupportedOperationException();
  }

  // late binding- have to fabricate all neighboring oorners
  private PreferenceArray enumeratePreferencesFromUserCorner(long userID)
  throws NoSuchUserException {
    Corner main = userDB.id2corner.get((userID) + "");
    Set<Corner> all = cg.getHashSet(main.hashes.clone());
    int count = 0;
    for(Corner c: all) {
      Set<String> items = itemDB.corner2ids.get(c);
      if (null != items) {
        count += items.size();
      }
    }
    int prefIndex = 0;
    PreferenceArray prefs = new GenericUserPreferenceArray(count);
    for(Corner c: all) {
      Set<String> items = itemDB.corner2ids.get(c);
      if (null != items) {
        float dist = (float) distance2rating(manhattan(main.hashes, c.hashes));
        for(String itemID: items) {
          prefs.setUserID(prefIndex, userID);
          prefs.setItemID(prefIndex, Long.parseLong(itemID));
          prefs.setValue(prefIndex, dist);
        }
        prefIndex++;
      }
    }
    prefs.sortByValueReversed();
    return prefs;
  }

  double manhattan(int[] a, int[] b) {
    double sum = 0;
    for(int i = 0; i < a.length; i++) {
      sum += Math.abs(a[i] - b[i]);
    }
    // 0 times varianceEuclid is NaN !
    return (sum < 0.0000001) ? 0 : sum * varianceManhattan;
  }

  double manhattanD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < a.length; i++) {
      sum += Math.abs(a[i] - b[i]);
    }
    return sum * Math.sqrt(2) * varianceManhattan;
  }

  // rectangular distance
  double euclidD(double[] a, double[] b) {
    double sum = 0;
    for(int i = 0; i < a.length; i++) {
      sum += (a[i] - b[i]) * (a[i] - b[i]);
    }			
    return Math.sqrt(sum) * diagonal;
  }

  double distance2rating(double d) {
    if (d < 0)
      d = 0.0;
    if (d > 1) 
      d = 1.0;
    double e = (1-d) * scale + offset;
    return e;
  }

  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    return new LPIS(userDB.id2corner.keySet().iterator());
  }

  @Override
  public boolean hasPreferenceValues() {
    return false;
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

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    VertexTransitiveHasher hasher = new VertexTransitiveHasher(50, 0.70);
    CornerGen cg = new CornerGen(hasher, hasher.stretch);
    SimplexTextDataModel model = new SimplexTextDataModel("/tmp/lsh_hadoop/shortU.txt", cg);
    model.hashCode();
  }

}
