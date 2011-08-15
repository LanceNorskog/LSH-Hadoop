package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.model.MetadataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Algebra;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MurmurHashRandom;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.QRDecomposition;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.Vector;

/*
 * Sort data model matrix by number of preferences, both user and item.
 */

// TODO: Add feature in output for pref is real or just default 3.0
// TODO: Assign color or something to this in diagrams

public class SortDataModel {
  
  static int SOURCE_DIMENSIONS = 200;
  static int SAMPLES = 200;
  static int TARGET_DIMENSIONS = 2;
  private DataModel model;
  
  public SortDataModel(DataModel model) {
    this.model = model;
  }
  
  public static void main(String[] args) throws IOException, TasteException {
    SortDataModel sorter;
    GroupLensDataModel model;
    
    if (args.length != 1)
      throw new TasteException("Usage: grouplens.dat");
    MetadataModel<String> movieNames = new MetadataModel<String>(new HashMap<Long,String>(), "movies");
    model = new GroupLensDataModel(new File(args[0]), movieNames, null, null);
    sorter = new SortDataModel(model);
    ArrayList<Long> userList = new ArrayList<Long>(model.getNumUsers());
    ArrayList<Long> itemList = new ArrayList<Long>(model.getNumItems());
    Map<Long,Integer> itemCounts = new HashMap<Long,Integer>();
    Map<Long,Integer> userCounts = new HashMap<Long,Integer>();
    int numUserPrefs = sorter.getUserCounts(model, userList, userCounts);
    int numItemPrefs = sorter.getItemCounts(model, itemList, itemCounts);
    
    sorter.sortPrefs(model.getUserIDs(), userList, userCounts);  
    sorter.sortPrefs(model.getItemIDs(), itemList, itemCounts);  
    //    sorter.printItems(itemList, itemCounts, movieNames);
    GroupLensWriter modelWriter = new GroupLensWriter(model, "/tmp/lsh_hadoop/gl_ratings_raw.dat");
    //    int items80 = sorter.findHighPercentile(itemList, itemCounts, numPrefs);
    sorter.printRawRatings(model, modelWriter, userList, itemList, userCounts, itemCounts);
    modelWriter = new GroupLensWriter(model, "/tmp/lsh_hadoop/gl_ratings_upward.dat");
    sorter.printSortedRatings(model, modelWriter, userList, itemList, userCounts, itemCounts);
    modelWriter.close();
  }
  
  /* 
   * index of highest user/item below 80th percentile of sums of ordered user/items
   * aList[i] = user/items sorted by # of preferences
   * counts.get(user/item) -> # of preferences
   * return index of first over the line
   */
  //  private int findHighPercentile(ArrayList<Long> aList, Map<Long,Integer> counts, int numPrefs) {
  //    int sum = 0;
  //    int percentile = (int) (numPrefs * 0.8);
  //    
  //    for(int i = 0; i < aList.size(); i++) {
  //      long id = aList.get(i);
  //      int prefs = counts.get(id);
  //      if (sum + prefs > percentile)
  //        return i;
  //    }
  //    return counts.size() - 1;
  //  }
  
  /* print all ratings in .dat format, as per DataModel order */
  private void printRawRatings(GroupLensDataModel model, GroupLensWriter modelWriter, ArrayList<Long> userList, ArrayList<Long> itemList, Map<Long,Integer> userCounts, Map<Long,Integer> itemCounts) throws TasteException {
    LongPrimitiveIterator userIter = model.getUserIDs();
    while(userIter.hasNext()) {
      long userID = userIter.nextLong();
      LongPrimitiveIterator itemIter = model.getItemIDs();
      while(itemIter.hasNext()) {
        long itemID = itemIter.nextLong();
        modelWriter.write(userID, itemID);
      }
    }
  }
  
  /* print ratings in user/item count sorted order - neither goes backwards in count */
  // walk corners plus and plus
  private void printSortedRatings(GroupLensDataModel model, GroupLensWriter modelWriter, ArrayList<Long> userSorted, ArrayList<Long> itemSorted, Map<Long,Integer> userCounts, Map<Long,Integer> itemCounts) throws TasteException {
    int userI = 0;
    int itemI = 0;
    int count = 0;
    int square = Math.max(userSorted.size(), itemSorted.size());
    
    while(true) {
      for(int u = 0; u < userI; u++) {
        modelWriter.write(userSorted.get(u), itemSorted.get(itemI));
      }
      for(int i = 0; i < itemI; i++) {
        modelWriter.write(userSorted.get(userI), itemSorted.get(i));
      }
      modelWriter.write(userSorted.get(userI), itemSorted.get(itemI));
      userI++;
      if (userI == userSorted.size())
        break;
      itemI++;
      if (itemI == itemSorted.size())
        break;
    }
    if (userI == userSorted.size()) {
      for(int i = itemI + 1; i < itemSorted.size(); i++) {
        for(int u = 0; u < userI; u++) {
          modelWriter.write(userSorted.get(u), itemSorted.get(i));
        }
//        modelWriter.write(userSorted.get(userI), itemSorted.get(i));
      }
    } else if (itemI == itemSorted.size()) {
      for(int u = userI; u < userSorted.size(); u++) {
        modelWriter.write(userSorted.get(u), itemSorted.get(itemI - 1));
      }
    } else
    this.hashCode();
    
  }
  
  private int getUserCounts(DataModel model, ArrayList<Long> userList,
      Map<Long,Integer> userCounts) throws TasteException {
    int total = 0;
    LongPrimitiveIterator userIter = model.getUserIDs();
    while(userIter.hasNext()) {
      long userID = userIter.nextLong();
      int size = model.getPreferencesFromUser(userID).length();
      userList.add(userID);
      userCounts.put(userID, size);
      total += size;
    }
    return total;
  }
  
  private int getItemCounts(DataModel model, ArrayList<Long> itemList,
      Map<Long,Integer> itemCounts) throws TasteException {
    int total = 0;
    LongPrimitiveIterator itemIter = model.getItemIDs();
    while(itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      int size = model.getPreferencesForItem(itemID).length();
      itemList.add(itemID);
      itemCounts.put(itemID, size);
      total += size;
    }
    return total;
  }
  
  /* 
   * aList[i] = id in order of #prefs
   */
  void sortPrefs(LongPrimitiveIterator iter, ArrayList<Long> aList, Map<Long,Integer> counts)  {
    ModelComparator modelComparator = new ModelComparator(counts);
    Collections.sort(aList, modelComparator);
  }
  
}

/*
 * Write GroupLens Data
 * TODO: timestamp
 */
class GroupLensWriter {
  private static final Float FLAT = 3.0f;
  PrintStream out;
  int count = 0;
  final DataModel model;
  
  GroupLensWriter(DataModel model, String file) throws IOException {
    this.model = model;
    File ratings = new File(file);
    if (ratings.isFile())
      ratings.delete();
    ratings.createNewFile();
    out = new PrintStream(ratings);
  }
  
  void close() {
    out.close();
  }
  
  void write(long userID, long itemID) {
    try {
      Float pref = model.getPreferenceValue(userID, itemID);
      if (pref == null)
        pref = 1.0f;
      out.println(userID + "::" + itemID + "::" + pref); // + "::" + count++);
    } catch (TasteException e) {
      
    }
  }
  
  void writeSimple(int userI, int itemI, long userID, long itemID) {
    try {
      Float pref = model.getPreferenceValue(userID, itemID);
      if (pref != null)
        out.println(userI + "::" + itemI + "::" + pref + "::" + count++);
    } catch (TasteException e) {
      
    }
  }
  
  // sort by user, like input
  
  /*  void writeUserPrefs(long userID, int itemI, List<Long> itemList, DataModel model, int userI) throws TasteException {
    for(int i = 0; i < itemI; i++) {
      Long itemID = itemList.get(i);
      write(userID, itemID);
    }
  }
  
  void writeItemPrefs(int userI, long itemID, List<Long> userList, DataModel model, int itemI) throws TasteException {
    for(int i = 0; i < userI; i++) {
      Long userID = userList.get(i);
      write(userID, itemID);
    }
  }*/
}
