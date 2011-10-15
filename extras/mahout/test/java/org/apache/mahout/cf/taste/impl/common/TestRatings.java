package org.apache.mahout.cf.taste.impl.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.model.MetadataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.math.Arrays;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;

/*
 * Testbed for axes of interest
 * forget translation bullshit
 * 
 */

public class TestRatings {
  
  public static void main(String[] args) throws IOException, TasteException {
    File ratingsFile = new File("/Users/Public/Downloads/Datasets/grouplens/ml-data-10k/ratings.dat");
    MetadataModel<String> itemNames = new MetadataModel<String>(new HashMap<Long,String>(), "movies");
    MetadataModel<String[]> itemGenres = new MetadataModel<String[]>(new HashMap<Long,String[]>(), "movies");
    DataModel model = new GroupLensDataModel(ratingsFile, itemNames, null, itemGenres);
    Matrix mtv = getMatrixDataModel(model, itemNames);
    
    SingularValueDecomposition svd = new SingularValueDecomposition(mtv);
    Matrix u = svd.getU();
    Matrix vt = svd.getV().transpose();
    double[] sValues = svd.getSingularValues();
    System.out.println("Singular values: " + Arrays.toString(sValues));
    System.out.println("Vtranspose2 matrix: ");
    printItemVectors(System.out, vt, mtv.getColumnLabelBindings(), itemNames);
    
  }
  
  private static Matrix getMatrixDataModel(DataModel model, MetadataModel<String> itemIDs) throws TasteException {
    int rows = model.getNumUsers();
    int columns = model.getNumItems();
    Matrix m = new DenseMatrix(rows, columns);
    Map<String,Integer> userLabels = new LinkedHashMap<String,Integer>();
    Map<String,Integer> itemLabels = new LinkedHashMap<String,Integer>();
    setLabels(model, m, userLabels, itemLabels);
    LongPrimitiveIterator users = model.getUserIDs();
    while(users.hasNext()) {
      long userID = users.nextLong();
      int row = userLabels.get(Long.toString(userID));
      FastIDSet itemset = model.getItemIDsFromUser(userID);
      LongPrimitiveIterator items = itemset.iterator();
      while(items.hasNext()) {
        long itemID = items.nextLong();
        int column = userLabels.get(Long.toString(itemID));
        float pref = model.getPreferenceValue(userID, itemID);
        m.setQuick(row, column, (double) pref);
      }
    }
    return m;
  }

  private static void setLabels(DataModel model, Matrix m,
      Map<String,Integer> userLabels, Map<String,Integer> itemLabels)
      throws TasteException {
    {
     int row = 0;
    LongPrimitiveIterator users = model.getUserIDs();
    while(users.hasNext()) {
      long userid = users.nextLong();
      String id = Long.toString(userid);
      userLabels.put(id, row++);
    }
    int column = 0;
    LongPrimitiveIterator items = model.getItemIDs();
    while(items.hasNext()) {
      long itemid = items.nextLong();
      String id = Long.toString(itemid);
      itemLabels.put(id, column++);
    }
    m.setRowLabelBindings(userLabels);
    m.setColumnLabelBindings(itemLabels);
    }
  }
  
  private static void newUser(SingularValueDecomposition svd, Matrix u,
      String name, Matrix bob) {
    Matrix bobXu = bob.times(u);
    System.out.println(name +" * u matrix:");
    printMatrix(bobXu, 1, 2);
    Matrix s = getSingularValuesDiagonalInverse(svd);
    Matrix bobXuXsingular = bobXu.times(s);
    System.out.println(name + " * u * singular inverse: ");
    printMatrix(bobXuXsingular, 1, 2);
  }
  
  private static Matrix getSingularValuesDiagonalInverse(
      SingularValueDecomposition svd) {
    Matrix s;
    s = svd.getS();
    for(int i = 0; i < s.numRows(); i++) {
      s.set(i,  i, 1/s.get(i, i));
    }
    return s;
  }
  
  /*
   * x.y vectors with movie name
   */
  private static void printItemVectors(PrintStream out, Matrix m, 
      Map<String,Integer> labels, MetadataModel<String> meta) throws TasteException {
    int columns = m.columnSize();
    for(String label: labels.keySet()) {
      long userID = Long.parseLong(label);
      String name = meta.getData(userID);
      int column = labels.get(label);
      out.print(name);
      out.print(',');
      out.print(m.getQuick(0, column));
      out.print(',');
      out.print(m.getQuick(1, column));
    }
  }
  
  private static void printMatrix(Matrix matrix, int rows, int columns) {
    PrintStream ps = System.out ; // new PrintStream(f);
    for(int r = 0; r < matrix.numRows() && r < rows; r++) {
      ps.print("\t");
      for(int c = 0; c < matrix.columnSize() && c < columns; c++) {
        double v = matrix.get(r, c);
        ps.print(dub(v));
        if (c != matrix.columnSize() - 1)
          ps.print(",");
      }
      ps.println();
    }
  }
  
  private static String dub(double dub) {
    String s = Double.toString(dub);
    if (s.length() > 6)
      return s.substring(0, 6);
    return s;
  }
  
}
