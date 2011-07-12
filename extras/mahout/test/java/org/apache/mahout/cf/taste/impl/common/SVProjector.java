package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
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


public class SVProjector {
  
  static int SOURCE_DIMENSIONS = 4;
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
    MetadataModel<String> movieNames = new MetadataModel<String>(new HashMap<Long,String>(), "movies");
    model = new GroupLensDataModel(new File(args[0]), movieNames, null, null);
    SemanticVectorFactory svf = new SemanticVectorFactory(model, SOURCE_DIMENSIONS);
    SOURCE_DIMENSIONS = svf.getDimensions();
    project(svf, model, measure, movieNames, args[1] + "_" + SOURCE_DIMENSIONS);
    
  }
  
  // stats on distances for user/item distances for actual prefs
  private static void project(SemanticVectorFactory svf, DataModel model,
      DistanceMeasure measure, MetadataModel<String> itemsMetadata, String path) throws TasteException, IOException {
    String pathX = path + "x" + TARGET_DIMENSIONS;
    List<NamedVector> itemsOrig = new ArrayList<NamedVector>();
    List<NamedVector> itemsRP = new ArrayList<NamedVector>();
    LongPrimitiveIterator itemIter = model.getItemIDs();
    report("Creating Random Matrix: " + TARGET_DIMENSIONS + "x" + SOURCE_DIMENSIONS);
    Matrix rp = getRandomMatrixLinear(TARGET_DIMENSIONS, SOURCE_DIMENSIONS);
    
    report("Creating Item vecs: "+model.getNumItems());
    
    while(itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      itemsOrig.add((NamedVector) svf.projectItemDense(itemID, itemID + ""));
    }
    report("SVD full items: ");
    svdOrig(itemsOrig);
    
    printVectors(path + "_all_items.csv", SOURCE_DIMENSIONS, itemsOrig, itemsMetadata);
    // Decimate down to SAMPLES # vectors
//    itemsOrig = decimate(itemsOrig, SAMPLES);
    report("SVD "+ SAMPLES + " items: ");
    		
//    svdOrig(itemsOrig);
    itemsRP = decimate(itemsRP, SAMPLES);
    
    report("Projecting Item vecs: "+SAMPLES);
    projectVectors(itemsOrig, itemsRP, rp);
    //        printVectors(path + "_items.csv", itemsOrig);
    printVectors(pathX + "_items.csv", TARGET_DIMENSIONS, itemsRP, itemsMetadata);
    
//    reProject(pathX, itemsOrig, itemsRP);    
    
    
    //    distances(path, pathX, origItemMap, itemsRP);
    
  }
  
  private static void svdOrig(List<NamedVector> itemsOrig) {
    Matrix origMatrix = setVectorList(itemsOrig);
    SingularValueDecomposition svd = new SingularValueDecomposition(origMatrix);
    double[] singulars = svd.getSingularValues();
    report("Singulars:\n");
    System.out.println("\t" + Arrays.toString(singulars));
    double sum = 0;
    double running = 0;
    for(int i = 0; i < singulars.length; i++) {
      sum += singulars[i];
    }
    System.out.println("\tSum: " + sum);
    int buckets[] = new int[]{-1,-1,-1,-1};
    for(int i = 0; i < singulars.length; i++) {
      running += singulars[i]/sum;
      if (buckets[3] == -1 && running > 0.99) {
        buckets[3] = i;
      } 
      if (buckets[2] == -1 & running > 0.95) {
        buckets[2] = i;
      } 
      if (buckets[1] == -1 && running > 0.90) {
        buckets[1] = i;
      } 
      if (buckets[0]== -1 & running > 0.80) {
        buckets[0] = i;
      }
    }
    System.out.print("\t0.80, 0.90, 0.95, 0.99 #: " + Arrays.toString(buckets));
    double[] percentages = new double[3];
    percentages[0] =  buckets[1]/(0.0001 + buckets[0]);
    percentages[1] = buckets[2]/(0.00001 + buckets[1]);
    percentages[2] = buckets[3]/(0.00001 + buckets[2]);
    System.out.println(", % delta: " + Arrays.toString(percentages));
  }
  
  /*
   * Use Ted Dunning's trick of projecting again, using the largest singular vectors
   */
  private static void reProject(String pathX, List<NamedVector> itemsOrig, List<NamedVector> itemsRP)
      throws TasteException, IOException, FileNotFoundException {
    report("Reprojection:");
    Matrix q = getQ(itemsRP);
    VectorList itemsOrigQ = null;
    itemsOrigQ = setVectorList(itemsOrig);
    Map<String,Integer> labels = itemsOrigQ.getRowLabelBindings();
    
    // SAMPLES x TARGET_DIMS
    Matrix origRPQ = q.transpose().times(itemsOrigQ);
    SingularValueDecomposition svd = new SingularValueDecomposition(origRPQ);
    double[] singulars = svd.getSingularValues();
    report("Singulars:\n");
    System.out.println("\t" + Arrays.toString(singulars));
    
    report("Done");
    Matrix u = svd.getU();
    printMatrix(pathX + "_svd_items.csv", origRPQ.transpose(), labels);
  }

  private static void reProjectAll(String pathX, List<NamedVector> itemsOrig)
      throws TasteException, IOException, FileNotFoundException {
    report("Reprojection:");
    Matrix q = getQ(itemsOrig);
    VectorList itemsOrigQ = null;
    itemsOrigQ = setVectorList(itemsOrig);
    Map<String,Integer> labels = itemsOrigQ.getRowLabelBindings();
    
    // SAMPLES x TARGET_DIMS
    Matrix origRPQ = q.transpose().times(itemsOrigQ);
    origRPQ = itemsOrigQ.times(q.transpose());
    SingularValueDecomposition svd = new SingularValueDecomposition(origRPQ);
    double[] singulars = svd.getSingularValues();
    report("Singulars:\n");
    System.out.println("\t" + Arrays.toString(singulars));
    
    report("Done");
    Matrix u = svd.getU();
    printMatrix(pathX + "_svd_items.csv", origRPQ.transpose(), labels);
  }
  private static VectorList setVectorList(List<NamedVector> itemsOrig) {
    VectorList itemsOrigQ;
    itemsOrigQ = new VectorList(itemsOrig.size(), SOURCE_DIMENSIONS);
    for(int i = 0; i < itemsOrig.size(); i++)
      itemsOrigQ.assignRow(i, itemsOrig.get(i));
    return itemsOrigQ;
  }
  
  private static void distances(String path, String pathX,
      List<NamedVector> origItemMap, List<NamedVector> itemsRP) throws IOException {
    report("Distances:");
    origItemMap = decimate(origItemMap, SAMPLES);
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
//    printMatrix(path + "_items_distances.csv", itemDistancesOrig);
//    printMatrix(pathX + "_items_distances.csv", itemDistancesRP);
//    printMatrix(path + "_items_distance_ratio_matrix.csv", itemDistancesRatio);
//    printDistancesRatio(pathX + "_items_distances.csv", itemDistancesOrig, itemDistancesRP);
  }

  // Get orthonormal basis for input "matrix"
  private static Matrix getQ(List<NamedVector> itemsRP) {
    Matrix rpMat = new VectorList(SOURCE_DIMENSIONS, itemsRP.get(0).size());
    for(int i = 0; i < itemsRP.size(); i++) {
      rpMat.assignRow(i, itemsRP.get(i));
    }
    QRDecomposition decomp = new QRDecomposition(rpMat);
    Matrix q = decomp.getQ();
    return q;
  }
  
  private static void projectVectors(
      List<NamedVector> vecs, List<NamedVector> rVecs, Matrix rp) throws TasteException,
      IOException, FileNotFoundException {
    
    Iterator<NamedVector> iter = vecs.iterator();
    while(iter.hasNext()) {
      NamedVector vid = iter.next();
      NamedVector vr = new NamedVector(rp.times(vid), vid.getName());
      rVecs.add(vr);
    }
  }
  
  private static void printVectors(String path, int dims,
      List<NamedVector> rVecs, MetadataModel<String> itemNames) throws TasteException,
      IOException, FileNotFoundException {
    
    File psFile = new File(path);
    psFile.delete();
    psFile.createNewFile();
    PrintStream ps = new PrintStream(psFile);
    int count = 1; // Excel starts at 1
    Iterator<NamedVector> iter = rVecs.iterator();
    // #,1,values...   1 is a hack for KNime
    ps.print("id,name");
    for(int i = 0; i < dims; i++) {
      ps.print(",v" + i);
    }
    ps.println();
    while(iter.hasNext()) {
      Vector vr = iter.next();
      NamedVector nv = (NamedVector) vr;
      long id = Long.parseLong(nv.getName());
      String itemName = itemNames.getData(id);
      ps.print(id);
      ps.print("," + itemName.trim().replaceAll(",", "\",\""));
      for(int i = 0; i < dims; i++) {
        ps.print(",");
        ps.print(vr.get(i));
      }
      ps.println();
    }
    ps.close(); 
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
  private static void findDistances(List<NamedVector> fullMap,
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
  
  private static void printMatrix(String matrixPath,
       Matrix matrix, Map<String,Integer> labels) throws IOException {
    File f = new File(matrixPath);
    f.delete();
    f.createNewFile();
    PrintStream ps = new PrintStream(f);
    for(String label: labels.keySet()) {
      int r = labels.get(label);
      ps.print(r + "," + label.trim().replaceAll(",", "\",\"") + ",");
      for(int c = 0; c < matrix.columnSize(); c++) {
        double v = matrix.get(r, c);
        ps.print(v);
        if (c != matrix.columnSize() - 1)
          ps.print(",");
      }
      ps.println();
    }
    
    
  }
  
  private static List<NamedVector> decimate(List<NamedVector> fullMap, int samples) {
    Random rnd = new MurmurHashRandom(0);
    int size = fullMap.size();
    if (samples >= size)
      return fullMap;
    List<NamedVector> sublist = new ArrayList<NamedVector>();
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
    Random rnd = new MurmurHashRandom(500);
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
    Random rnd = new MurmurHashRandom(500);
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
    Random rnd = new MurmurHashRandom(500);
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
        else 
          throw new IllegalStateException("rnd.nextInt(2) returned: " + test);
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
