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
import org.apache.mahout.math.VectorList;

/*
 * Sort data model matrix by number of preferences, both user and item.
 */

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
    int numPrefs = sorter.getUserCounts(model, userList, userCounts);
    int total2 = sorter.getItemCounts(model, itemList, itemCounts);
    sorter.sortPrefs(model.getUserIDs(), userList, userCounts);  
    sorter.sortPrefs(model.getItemIDs(), itemList, itemCounts);  
    //    sorter.printItems(itemList, itemCounts, movieNames);
    GroupLensWriter modelWriter = new GroupLensWriter("/tmp/lsh_hadoop/gl_ratings.dat");
    //    int items80 = sorter.findHighPercentile(itemList, itemCounts, numPrefs);
    sorter.printRatings(model, modelWriter, userList, itemList, userCounts, itemCounts, 100);
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
  
  /* print X percentile of ratings, sign +1/-1 means 'least ratings' or 'most ratings' */
  void printRatings(DataModel model, GroupLensWriter modelWriter, List<Long> userList, ArrayList<Long> itemList, Map<Long,Integer> userCounts, Map<Long,Integer> itemCounts, int percentile) throws TasteException {
    int userI = 0;
    int itemI = 0;
    int userTotal = 0;
    for(Integer count: userCounts.values()) {
      userTotal += count;
    }
    int itemTotal = 0;
    for(Integer count: itemCounts.values()) {
      itemTotal += count;
    }
    int userMod = 1;
    int itemMod = 1;
    int numUsers = userList.size();
    int numItems = itemList.size();
    if (numUsers > numItems)
      userMod = numUsers/numItems;
    else if (numItems > numUsers)
      itemMod = numItems/numUsers;
    System.out.println("userMod/itemMod: " + userMod + "," + itemMod);
    
    while(userI < numUsers && itemI < numItems) {
      long userID = userList.get(userI);
      long itemID = itemList.get(itemI);
      int userPrefs = userCounts.get(userID);
      int itemPrefs = itemCounts.get(itemID);
      if (userPrefs > itemPrefs) {
        for(int x = 0; x < userMod; x++) {
          modelWriter.writeUserPrefs(userID, itemI, itemList, model, userI); // last for number hack
          userI++;
        }
      } else {
        for(int x = 0; x < itemMod; x++) {
          modelWriter.writeItemPrefs(userI, itemID, userList, model, itemI); // last for number hack
          itemI++;
        }
      }
    }
    while(userI < numUsers) {
      long userID = userList.get(userI);
      modelWriter.writeUserPrefs(userID, itemI, itemList, model, userI);
      userI++;
    }
    while(itemI < numItems) {
      long itemID = itemList.get(itemI);
      modelWriter.writeItemPrefs(userI, itemID, userList, model, itemI);
      itemI++;
    }
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
    ModelComparator userComparator = new ModelComparator(counts);
    Collections.sort(aList, userComparator);
  }
  
//  private void printItems(ArrayList<Long> itemList,
//      Map<Long,Integer> itemCounts, MetadataModel<String> movieNames) throws TasteException {
//    int total = 0;
//    for(int i = 0; i < itemList.size(); i++) {
//      long itemid = itemList.get(i);
//      int size = itemCounts.get(itemid);
//      String name = movieNames.getData(itemid);
//      System.out.println("item, #, name: " + itemid + "," + size + "," + name);
//      total += size;
//    }
//    System.out.println("Total prefs: " + total);
//  }
}

/*
 * Handle format for writing model out.
 */
class GroupLensWriter {
  PrintStream out;
  FastIDSet usersDone = new FastIDSet();
  FastIDSet itemsDone = new FastIDSet();
  int count = 0;
  
  GroupLensWriter(String file) throws IOException {
    File ratings = new File(file);
    if (ratings.isFile())
      ratings.delete();
    ratings.createNewFile();
    out = new PrintStream(ratings);
    out.println("rowid,user,item");
  }
  
  void close() {
    out.close();
  }
  
  void write(long userID, long itemID, float pref) {
    out.println(userID + "::" + itemID + "::" + pref);
  }
  
  // hack to just write the indexes instead, for sanity reasons
  
  void writeUserPrefs(long userID, int itemI, List<Long> itemList, DataModel model, int userI) throws TasteException {
    for(int i = 0; i < itemI; i++) {
      Float preference = model.getPreferenceValue(userID, itemList.get(i));
      if (null != preference)
        out.println(count++ + "," + userI + "," + i);
      //      out.println(userID + "::" + itemList.get(i) + "::" + preference.floatValue());
    }
  }
  
  void writeItemPrefs(int userI, long itemID, List<Long> userList, DataModel model, int itemI) throws TasteException {
    for(int i = 0; i < userI; i++) {
      Long userID = userList.get(i);
      Float preference = model.getPreferenceValue(userID, itemID);
      if (null != preference)
        out.println(count++ + "," + i + "," + itemI);
      //      out.println(userID + "::" + itemID + "::" + preference.floatValue());
    }
  }
  
}
