package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.model.MetadataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomProjector;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.Vector;

/*
 * Testbed for: find singular vectors and cluster items at end.
 * To find hates, recenter everything and cluster at opposite end.
 */

public class SVOpposites {
  
  static int SOURCE_DIMENSIONS = 2;
  static int SAMPLES = 4;
  static int TARGET_DIMENSIONS = 2;
  
  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model;
    
    if (args.length != 2)
      throw new TasteException("Usage: grouplens.dat file_prefix");
    MetadataModel<String> movieNames = new MetadataModel<String>(new HashMap<Long,String>(), "movies");
    MetadataModel<String[]> movieGenres = new MetadataModel<String[]>(new HashMap<Long,String[]>(), "movies");
    model = new GroupLensDataModel(new File(args[0]), movieNames, null, movieGenres);
    SemanticVectorFactory svf = new SemanticVectorFactory(model, SOURCE_DIMENSIONS);
    opposites(svf, model, movieNames, movieGenres, args[1] + "_" + SOURCE_DIMENSIONS);
    svf.hashCode();
    System.out.println("All done");
  }
  
  static void opposites(SemanticVectorFactory svf, DataModel model,
      MetadataModel<String> movieNames, MetadataModel<String[]> movieGenres, String path) throws TasteException, IOException {
    String pathX = path + "x" + TARGET_DIMENSIONS;
    List<NamedVector> itemsOrig = new ArrayList<NamedVector>();
//    LongPrimitiveIterator itemIter = model.getItemIDs();
//    report("Creating Item vecs: "+ model.getNumItems());
//    
//    while(itemIter.hasNext()) {
//      long itemID = itemIter.nextLong();
//      itemsOrig.add((NamedVector) svf.projectItemDense(itemID, itemID + ""));
//    }
    
    itemsOrig.add(new NamedVector(new DenseVector(new double[] {1,2}),"1"));
    itemsOrig.add(new NamedVector(new DenseVector(new double[] {3,5}),"2"));
    itemsOrig.add(new NamedVector(new DenseVector(new double[] {2,7}),"3"));
    itemsOrig.add(new NamedVector(new DenseVector(new double[] {1,5}),"4"));
    
//    itemsOrig = decimate(itemsOrig, SAMPLES);
    printItems(path + "_items.csv", SOURCE_DIMENSIONS, itemsOrig, movieNames, movieGenres);
//    RandomProjector rp = RandomProjector.getProjector(0);
     
//    List<NamedVector> itemsRP = new ArrayList<NamedVector>();
//    projectVectors(itemsOrig, itemsRP , rp, TARGET_DIMENSIONS);
//    itemsRP.hashCode();
    Map<NamedVector,List<NamedVector>> clusters = new HashMap<NamedVector,List<NamedVector>>();
    clusters = cluster(itemsOrig, TARGET_DIMENSIONS);
    //    displayClusters(clusters, movieNames, movieGenres);
    printMagnets(pathX, TARGET_DIMENSIONS, clusters, movieNames, movieGenres);
    printClusters(pathX + "_clusters.csv", TARGET_DIMENSIONS, clusters, movieNames, movieGenres);
    clusters.hashCode();
 }
 
  /*
   * Use endpoints of singular vectors as seeds for clusters.
   * Seeds and items do not move; this is merely about distance
   * SVD
   * vectors = left U * Singular
   * recenter vectors
   * 
   * Use Singular values to give lengths to vectors (if needed).
   * 
   */
  static Map<NamedVector,List<NamedVector>> cluster(List<NamedVector> items, int dims)
      throws TasteException, IOException, FileNotFoundException {
    report("SVD-based clustering:");
    Matrix itemsM = getMatrix(items); 
    // SAMPLES x TARGET_DIMS
    SingularValueDecomposition svd = new SingularValueDecomposition(itemsM);
    System.out.println("Rank: " + svd.rank());
    
    Matrix u = svd.getU();
    Matrix left = u; // .viewPart(0, dims, 0, dims);
    Matrix vt = svd.getV();
    Matrix right = vt; // v.viewPart(0, dims, 0, dims); 
    
    System.out.println("U [] = " + Arrays.toString(u.size()) + ", Vt [] = " + Arrays.toString(vt.size()));
    System.out.println("\tSingulars: " + Arrays.toString(svd.getSingularValues()));
    report("Finding nearby vectors:\n");
    OnlineSummarizer tracker = new OnlineSummarizer();
    List<Vector> magnets = getMagnets(items, left, right, 4);
    List<NamedVector> svdItems = getRows(items, left);
    Map<NamedVector,List<NamedVector>> clusters = assignToClusters(svdItems, magnets, tracker);
    clusters.hashCode();
    report("Done: summary" + toStats(tracker) + "\n" );
    return clusters;
  }
  
  private static List<NamedVector> getRows(List<NamedVector> items, Matrix left) {
    List<NamedVector> featureSpaces = new ArrayList<NamedVector>();
    for(int i = 0; i < SAMPLES; i++) {
      Vector v = left.getRow(i);
      featureSpaces.add(new NamedVector(v, items.get(i).getName()));
    }
    return featureSpaces;
  }

  private static List<NamedVector> project(List<NamedVector> items, Matrix left) {
    List<NamedVector> featureSpace = new ArrayList<NamedVector>(); 
    System.out.println("Original item vectors");
    for(NamedVector v: items) {
      System.out.println();
      Vector pr = left.times(v);
      featureSpace.add(new NamedVector(pr, v.getName()));
    }
    return featureSpace;
  }
  
  /*
   * Given a list of magnet vectors, assign each vector to closest magnet.
   * 
   * Magnet is a map, but can't find a map that preserves order in walking,
   * so preserves order of singular vectors as name.
   */
  static Map<NamedVector,List<NamedVector>> assignToClusters(
      List<NamedVector> items, List<Vector> magnets, OnlineSummarizer tracker) {
    Map<NamedVector,List<NamedVector>> clusters;
    clusters = new LinkedHashMap<NamedVector,List<NamedVector>>(); 
    for(int r = 0; r < magnets.size(); r++) {
      NamedVector v = new NamedVector(magnets.get(r), r + "");
      clusters.put(v, new ArrayList<NamedVector>());
    }
    for(NamedVector v: items) {
      Vector cluster = findClosestVector(v, clusters.keySet(), tracker);
      List<NamedVector> pile = clusters.get(cluster);
      pile.add(v);
    }
//    System.out.println("cluster sizes: " + clusters.get(0).size() + ", " + clusters.get(1).size());
    return clusters;
  }
  
  static Vector findClosestVector(Vector v,
      Set<? extends Vector> keys, OnlineSummarizer tracker) {
    double min = Double.MAX_VALUE;
    Vector closest = null;
    for(Vector candidate: keys) {
      double dist = v.getDistanceSquared(candidate);
      if (dist < min) {
        closest = candidate;
        min = dist;
      } else {
        v.hashCode();
      }
    }
    tracker.add(min);
    return closest;
  }
  
  /*
   * Get list of magnets for clusters. In order of singular sizes.
   * Use right-hand matrix rows (features).
   */
  
  static List<Vector> getMagnets(List<NamedVector> items, Matrix left, Matrix right, int max) {
    List<Vector> magnets = new ArrayList<Vector>();
    for(int r = 0; r < right.numCols() && r < max; r++) {
      Vector v = right.getColumn(r);
//      Vector vp = left.times(v);
      magnets.add(v);
//      magnets.add(vp);
    }
    return magnets;
  }
  
  static Matrix getMatrix(List<NamedVector> itemsOrig) {
    Matrix items = new DenseMatrix(itemsOrig.size(), itemsOrig.get(0).size());
    
    Map<String, Integer> bindings = new HashMap<String,Integer>();
    for(int c = 0; c < itemsOrig.size(); c++) {
      NamedVector v = itemsOrig.get(c);
      items.assignRow(c, v);
      bindings.put(v.getName(), c);
    }
    items.setRowLabelBindings(bindings);
    return items;
  }
  
  private static void projectVectors(
      List<NamedVector> vecs, List<NamedVector> rVecs, RandomProjector rp, int dims) throws TasteException,
      IOException, FileNotFoundException {
    
    Iterator<NamedVector> iter = vecs.iterator();
    while(iter.hasNext()) {
      NamedVector vid = iter.next();
      NamedVector vr = new NamedVector(rp.times(vid.getDelegate(), dims), vid.getName());
      rVecs.add(vr);
    }
  }
  
  /*
  static void normalize(List<NamedVector> vecs) {
    OnlineSummarizer tracker = getSummary(vecs);
    AbstractVector longest = (AbstractVector) getLongest(vecs).getDelegate();
    String stats = toStats(tracker);
    stats.hashCode();
    
    final double lengthSq = longest.getLengthSquared();
    double factor = Math.sqrt(lengthSq);
    DoubleDoubleFunction shrink = new TimesFunction();
    for(NamedVector v: vecs) {
      v.assign(shrink, 1/factor);
    }
    tracker = getSummary(vecs);
    stats = toStats(tracker);
    stats.hashCode();
  }
*/  
  static String toStats(OnlineSummarizer tracker) {
    return "{mean=" + tracker.getMean() + ",median=" + tracker.getMedian() + ", min=" + tracker.getMin() + ", max=" + tracker.getMax() + "}";
  }
  
  static OnlineSummarizer getSummary(List<NamedVector> vecs) {
    OnlineSummarizer tracker = new OnlineSummarizer();
    for(Vector v: vecs) {
      double length = v.getLengthSquared();
      tracker.add(length);
    }
    return tracker;
  }
  
  static NamedVector getLongest(List<NamedVector> vecs) {
    double max = Double.MIN_VALUE;
    NamedVector longest = null;
    for(NamedVector v: vecs) {
      double dist = v.getLengthSquared();
      if (dist > max) {
        longest = v;
        max = dist;
      }
    }
    return longest;
  }
  
  static void printItems(String path, int dims,
      Collection<NamedVector> rVecs, MetadataModel<String> itemNames, MetadataModel<String[]> movieGenres) throws TasteException,
      IOException, FileNotFoundException {
    File psFile = new File(path);
    psFile.delete();
    psFile.createNewFile();
    PrintStream ps = new PrintStream(psFile);
    Iterator<NamedVector> iter = rVecs.iterator();
    // #,1,values...   1 is a hack for KNime
    ps.print("id,name");
    //    ps.print("id,name,genre");
    for(int i = 0; i < dims; i++) {
      ps.print(",v" + i);
    }
    ps.println();
    while(iter.hasNext()) {
      Vector vr = iter.next();
      NamedVector nv = (NamedVector) vr;
      long id = Long.parseLong(nv.getName());
      String itemName = id + "";
      String itemGenre = id + "";
      if (itemNames != null && itemNames.containsKey(id)) {
        itemName = itemNames.getData(id);
      }
      if (movieGenres != null && movieGenres.containsKey(id)) {
        itemGenre = movieGenres.getData(id)[0];
      }
      ps.print(id);
      ps.print("," + itemName.trim().replaceAll(",", "\",\""));
      //      ps.print("," + itemGenre.trim().replaceAll(",", "\",\""));
      for(int i = 0; i < dims; i++) {
        ps.print(",");
        ps.print(dub(vr.get(i)));
      }
      ps.println();
    }
    ps.close(); 
  }
  
    
  private static void printMagnets(String path, int dims,
      Map<NamedVector,List<NamedVector>> clusters,
      MetadataModel<String> movieNames, MetadataModel<String[]> movieGenres) throws FileNotFoundException, TasteException, IOException {
    Collection<NamedVector> magnets = clusters.keySet();
    // really, want, magnitudes from singular values
    printItems(path + "_magnets.csv", dims, magnets, null, null);
  }
  
   static void printClusters(String path, int dims,
      Map<NamedVector,List<NamedVector>> clusters, MetadataModel<String> movieNames, MetadataModel<String[]> movieGenres) throws IOException, TasteException {
    File psFile = new File(path);
    psFile.delete();
    psFile.createNewFile();
    PrintStream ps = new PrintStream(psFile);
    // #,1,values...   1 is a hack for KNime
    ps.print("type,id,cluster,name,genre");
    for(int i = 0; i < dims; i++) {
      ps.print(",v" + i);
    }
    ps.println();
    for(NamedVector magnet: clusters.keySet()) {
      ps.print("m,m" + magnet.getName() + ",m" + magnet.getName() + ",,");
      for(int i = 0; i < dims; i++) {
        ps.print(",");
        ps.print(dub(magnet.get(i)));
      }     
      ps.println();
      List<NamedVector> cluster = clusters.get(magnet);
      Iterator<NamedVector> iter = cluster.iterator();
      
      while(iter.hasNext()) {
        
        NamedVector vr = iter.next();
        NamedVector nv = (NamedVector) vr;
        Long id = Long.parseLong(nv.getName());
        String itemName = id + "";
        String itemGenre = "";
        if (movieNames != null && movieNames.containsKey(id)) {
          itemName = movieNames.getData(id);
        }
        if (movieGenres != null && movieGenres.containsKey(id)) {
          itemGenre = movieGenres.getData(id)[0];
        }
        ps.print("v,v" + id + ",m" + magnet.getName());
        ps.print("," + itemName.trim().replaceAll(",", "\",\""));
        ps.print("," + itemGenre.trim().replaceAll(",", "\",\""));
        for(int i = 0; i < dims; i++) {
          ps.print(",");
          ps.print(dub(vr.get(i)));
        }
        ps.println();
      }
    }
    
    ps.flush();
    ps.close(); 
  }
  
  static void displayClusters(Map<NamedVector,List<NamedVector>> clustered, MetadataModel<String> movieNames, MetadataModel<String[]> movieGenres) throws TasteException {
    for(Vector cluster: clustered.keySet()) {
      List<NamedVector> vecs = clustered.get(cluster);
      System.out.println("Cluster #" + cluster.toString());
      for(NamedVector v: vecs) {
        Long itemId = Long.parseLong(v.getName());
        String movieName = movieNames.getData(itemId);
        System.out.println("\t itemID: " + v.getName() + ", " + movieName);
        System.out.println("\t\t " + cluster.getDistanceSquared(v) + " :: " + v.toString());
      }
    }
  }
  
  private static String dub(double dub) {
    if (dub > -100000 && dub < 100000)
      return Double.toString(dub + 0.00000001).substring(0, 6);
    else return "NaN";
  }
  
  // first N SAMPLES
  static List<NamedVector> decimate(List<NamedVector> fullMap, int samples) {
    int size = fullMap.size();
    if (samples >= size)
      return fullMap;
    List<NamedVector> sublist = new ArrayList<NamedVector>();
    for(int i = 0; i < samples; i++) {
      sublist.add(fullMap.get(i));
    }
    return sublist;
  } 
  
  static long tod = 0;
  static void report(String string) {
    long now = System.currentTimeMillis();
    if (tod != 0) {
      System.out.println(" (" + (now - tod) + ")");
    }
    tod = now;
    System.out.print(string);
  }
  
}
