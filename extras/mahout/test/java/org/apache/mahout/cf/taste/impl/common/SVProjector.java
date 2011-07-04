package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.MurmurHashRandom;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Algebra;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;


public class SVProjector {
  
  static int SOURCE_DIMENSIONS = 200;
  static int SAMPLES = 200;
  static int TARGET_DIMENSIONS = 2;
  
  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model;
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double rescale;
    
    if (args.length != 2)
      throw new TasteException("Usage: grouplens.dat file_prefix");
    model = new GroupLensDataModel(new File(args[0]));
    SemanticVectorFactory svf = new SemanticVectorFactory(model, SOURCE_DIMENSIONS);
    SOURCE_DIMENSIONS = svf.getDimensions();
    Vector unitVector = new DenseVector(SOURCE_DIMENSIONS);
    Vector zeroVector = new DenseVector(SOURCE_DIMENSIONS);
    for(int i = 0; i < SOURCE_DIMENSIONS; i++) 
      unitVector.set(i, 1.0);
    rescale = measure.distance(zeroVector, unitVector);
    project(svf, model, measure, rescale, args[1] + "_" + SOURCE_DIMENSIONS);
    
  }
  
  // stats on distances for user/item distances for actual prefs
  private static void project(SemanticVectorFactory svf, DataModel model,
      DistanceMeasure measure, double rescale, String path) throws TasteException, IOException {
    String pathX = path + "x" + TARGET_DIMENSIONS;
    List<Vector> origItemMap = new ArrayList<Vector>();
    List<Vector> itemsRP = new ArrayList<Vector>();
    LongPrimitiveIterator itemIter = model.getItemIDs();
    report("Creating Random Matrix: " + TARGET_DIMENSIONS + "x" + SOURCE_DIMENSIONS);
    Matrix rp = getRandomMatrixGaussian(TARGET_DIMENSIONS, SOURCE_DIMENSIONS);
    
    report("Creating Item vecs: "+model.getNumItems());
    
    while(itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      origItemMap.add(svf.projectItemDense(itemID));
    }
    //    report("Projecting Item vecs: "+SAMPLES);
    projectVectors(origItemMap, itemsRP, rp);
    
    //    printVectors(path + "_all_items.csv", origItemMap);
    // Train KNime tools from full item vector set
    //    printVectors(pathX + "_all_items.csv", itemsRP);
    
    // Decimate down to SAMPLES # vectors
    report("Decimating Item vecs: "+model.getNumItems());
    origItemMap = decimate(origItemMap, SAMPLES);
    itemsRP = decimate(itemsRP, SAMPLES);
    printVectors(path + "_items.csv", origItemMap);
    
    
    Matrix itemDistancesOrig = new DenseMatrix(SAMPLES, SAMPLES);
    Matrix itemDistancesRP = new DenseMatrix(SAMPLES, SAMPLES);
    Matrix itemDistancesRatio = new DenseMatrix(SAMPLES, SAMPLES);
    report("Calculating Item distances: "+SAMPLES);
    findDistances(origItemMap, itemDistancesOrig);
    findDistances(itemsRP, itemDistancesRP);
    report("Calculating Item distances: "+SAMPLES);
    findDistanceRatios(itemDistancesOrig,itemDistancesRP, itemDistancesRatio);
    report("Done");
    System.out.println("Item distances matrix norms: orig = " + Algebra.getNorm(itemDistancesOrig) + 
        ", projected = " + Algebra.getNorm(itemDistancesRP));
    printVectors(pathX + "_items.csv", itemsRP);    
    printMatrix(path + "_items_distances.csv", itemDistancesOrig);
    printMatrix(pathX + "_items_distances.csv", itemDistancesRP);
    printMatrix(path + "_items_distance_ratio_matrix.csv", itemDistancesRatio);
    printDistancesRatio(pathX + "_items_distances.csv", itemDistancesOrig, itemDistancesRP);
  }
  
  private static void findDistanceRatios(Matrix distancesOrig,
      Matrix distancesRP, Matrix distancesRatio) {
    
    //    double total = 0;
    //    for(int r = 0; r < distancesOrig.numRows(); r++) {
    //      for(int c = 0; c < distancesOrig.numCols(); c++) {
    //        double dist = distancesOrig.get(r, c) / distancesRP.get(r, c);
    //        if (dist > -111111111111111d && dist < 111111111111111d)
    //          total += dist;
    //      }
    //    }
    //    double mean = total / (distancesOrig.numRows() * distancesOrig.numCols());
    for(int r = 0; r < SAMPLES; r++) {
      for(int c = 0; c < SAMPLES; c++) {
        double ratio = distancesOrig.get(r, c) / distancesRP.get(r, c);
        if (Double.isNaN(ratio) || Double.isInfinite(ratio))
          ratio = 1;
        distancesRatio.set(r, c, ratio); 
      }
    }
  }
  
  private static void printDistancesRatio(String distancesPath,
      Matrix distancesFull, Matrix distancesP) throws IOException {
    File f = new File(distancesPath);
    f.delete();
    f.createNewFile();
    PrintStream ps = new PrintStream(f);
    ps.println("id,row,col,full,rp,ratio");
    int count = 0;
    for(int row = 0; row < distancesFull.numRows(); row++) {
      for(int col = 0; col < distancesFull.numCols(); col++) {
        ps.print(count + "," + row + "," + col + ",");
        double full = distancesFull.get(row, col);
        double projected = distancesP.get(row, col);
        double ratio = Double.NaN;
        if (! Double.isNaN(full) && !Double.isNaN(projected) && full != 0 && projected != 0)
          ratio = full / projected;
        ps.println(full + "," + projected + "," + ratio);
        count++;
      }
    }
    ps.println();
  }
  
  // not normalized
  private static void findDistances(List<Vector> fullMap,
      Matrix distances) {
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    double total = 0;
    for(int r = 0; r < fullMap.size(); r++) {
      for(int c = 0; c < fullMap.size(); c++) {
        double distance = measure.distance(fullMap.get(r), fullMap.get(c));
        distances.set(r, c, distance);
      }
    }
  }
  
  private static void printMatrix(String distanceMatrixPath,
      Matrix distances) throws IOException {
    File f = new File(distanceMatrixPath);
    f.delete();
    f.createNewFile();
    PrintStream ps = new PrintStream(f);
    for(int r = 0; r < distances.rowSize(); r++){
      for(int c = 0; c < distances.columnSize(); c++) {
        double distance = distances.get(r, c);
        ps.print(distance);
        if (c != distances.columnSize() - 1)
          ps.print(",");
      }
      ps.println();
    }
    
    
  }
  
  private static List<Vector> decimate(List<Vector> fullMap, int samples) {
    Random rnd = new MurmurHashRandom(0);
    int size = fullMap.size();
    if (samples >= size)
      return fullMap;
    List<Vector> sublist = new ArrayList<Vector>();
    for(int i = 0; i < samples; i++) {
      sublist.add(fullMap.get(i));
    }
    int n = samples;
    while(n < size) {
      int r = rnd.nextInt(samples);
      if (r > n) {
        int spot = rnd.nextInt(samples);
        sublist.set(spot, fullMap.get(n));
      }
      n++;
    }
    return sublist;
  }
  
  static long tod = 0;
  private static void report(String string) {
    long now = System.currentTimeMillis();
    if (tod != 0) {
      System.out.println(" (" + (now - tod) + ")");
    }
    tod = now;
    System.out.print(string);
  }
  
  private static Matrix getRandomMatrixLinear(int rows, int columns) {
    Matrix rm = new DenseMatrix(rows, columns);
    Random rnd = RandomUtils.getRandom(500);
    for(int r = 0; r < rows; r++) {
      for(int c = 0; c < columns; c++) {
        double value = rnd.nextDouble();
        rm.set(r, c, value);
      }
    }
    return rm;
  }
  
  private static Matrix getRandomMatrixGaussian(int rows, int columns) {
    Matrix rm = new DenseMatrix(rows, columns);
    Random rnd = new Random(500);
    for(int r = 0; r < rows; r++) {
      for(int c = 0; c < columns; c++) {
        double value = rnd.nextGaussian();
        rm.set(r, c, value);
        System.out.print('.');
      }
    }
    return rm;
  }
  
  private static Matrix getRandomMatrixPlusminus(int rows, int columns) {
    double sqrt3 = Math.sqrt(3);
    Matrix rm = new DenseMatrix(rows, columns);
    Random rnd = RandomUtils.getRandom(500);
    int buckets[] = new int[6];
    for(int r = 0; r < rows; r++) {
      for(int c = 0; c < columns; c++) {
        //        double value = Math.min(5.99999, 6 * rnd.nextDouble());
        int test = rnd.nextInt(2);
        //        System.out.print(test);
        if (test == 0)
          rm.set(r, c, -1);
        else if (test == 1)
          rm.set(r, c, 1);
        // else 0
        buckets[test] ++;
      }
    }
    System.out.println();
    System.out.println("buckets: " + Arrays.toString(buckets));
    return rm;
  }
  
  private static Matrix getRandomMatrixSqrt3(int rows, int columns) {
    double sqrt3 = Math.sqrt(3);
    Matrix rm = new DenseMatrix(rows, columns);
    Random rnd = new MurmurHashRandom(500);
    for(int r = 0; r < rows; r++) {
      for(int c = 0; c < columns; c++) {
        //        double value = Math.min(5.99999, 6 * rnd.nextDouble());
        int test = rnd.nextInt(6);
        System.out.print(test);
        //        if (test == 0)
        //          rm.set(r, c, -1);
        //        else if (test == 1)
        //          rm.set(r, c, 1);
        if (test == 0)
          rm.set(r, c, -sqrt3);
        else if (test == 1)
          rm.set(r, c, sqrt3);
      }
    }
    return rm;
  }
  
  private static void projectVectors(
      List<Vector> vecs, List<Vector> rVecs, Matrix rp) throws TasteException,
      IOException, FileNotFoundException {
    
    Iterator<Vector> iter = vecs.iterator();
    while(iter.hasNext()) {
      Vector vid = iter.next();
      Vector vr = rp.times(vid);
      rVecs.add(vr);
    }
  }
  
  private static void printVectors(String path,
      List<Vector> rVecs) throws TasteException,
      IOException, FileNotFoundException {
    
    File psFile = new File(path);
    psFile.delete();
    psFile.createNewFile();
    PrintStream ps = new PrintStream(psFile);
    int count = 1; // Excel starts at 1
    Iterator<Vector> iter = rVecs.iterator();
    // #,1,values...   1 is a hack for KNime
    while(iter.hasNext()) {
      Vector vr = iter.next();
      //      ps.print(count++);
      //      ps.print(",1,");
      for(int i = 0; i + 1 < vr.size(); i++) {
        ps.print(vr.get(i));
        ps.print(",");
      }
      ps.println(vr.get(vr.size() - 1));
    }
    ps.close(); 
  }
  
  private static double getMeanValue(PreferenceArray prefs) {
    double d = 0;
    for(int i = 0; i < prefs.length(); i++) {
      d += (prefs.getValue(i) - 1)/4f;
    }
    return d/prefs.length();
  }
  
  //  private static void showDistributions(Vector[] va,
  //      DistanceMeasure measure, double rescale, String userDistancesPath) {
  //    OnlineSummarizer tracker = new OnlineSummarizer();
  //    for(int i = 0; i < va.length;i++) {
  //      for(int j = i + 1; j < va.length; j++) {
  //        if ((null == va[i]) || (va[j] == null))
  //          continue;
  //        double distance = measure.distance(va[i], va[j]);
  //        distance /= rescale;
  //        tracker.add(distance);
  //      }
  //    }
  //  }
  
  
}
