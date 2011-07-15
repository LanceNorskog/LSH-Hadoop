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
    int numPrefs = getUserCounts(model, userList, userCounts);
    int total2 = getItemCounts(model, itemList, itemCounts);
    sortPrefs(model.getUserIDs(), userList, userCounts);  
    sortPrefs(model.getItemIDs(), itemList, itemCounts);  
    printItems(itemList, itemCounts, movieNames);
  }
  
  private static int getUserCounts(DataModel model, ArrayList<Long> userList,
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

  private static int getItemCounts(DataModel model, ArrayList<Long> itemList,
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

  static void sortPrefs(LongPrimitiveIterator iter, ArrayList<Long> aList, Map<Long,Integer> counts)  {
    ModelComparator userComparator = new ModelComparator(counts);
    Collections.sort(aList, userComparator);
    aList.hashCode();
  }
  
  private static void printItems(ArrayList<Long> itemList,
      Map<Long,Integer> itemCounts, MetadataModel<String> movieNames) throws TasteException {
    int total = 0;
    for(int i = 0; i < itemList.size(); i++) {
      long itemid = itemList.get(i);
      int size = itemCounts.get(itemid);
      String name = movieNames.getData(itemid);
      System.out.println("item, #, name: " + itemid + "," + size + "," + name);
      total += size;
    }
    System.out.println("Total prefs: " + total);
  }

}
