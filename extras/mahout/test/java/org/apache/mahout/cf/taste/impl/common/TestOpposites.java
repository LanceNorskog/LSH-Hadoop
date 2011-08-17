package org.apache.mahout.cf.taste.impl.common;

import java.io.PrintStream;

import org.apache.mahout.math.Arrays;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;

public class TestOpposites {
  
  private static final String[] SEASONS = new String[] {"s1","s2","s3","s4","s5","s6"};
  private static final String[] BOYS = new String[] {"ben","tom","john","fred"};
  
  static double[][] tv = {
    {5,5,0,5},
    {5,0,3,4},
    {3,4,0,3},
    {0,0,5,3},
    {5,4,4,5},
    {5,4,5,5}
  };
  
  public static void main(String[] args) {
    Matrix mtv = new DenseMatrix(tv);
    SingularValueDecomposition svd = new SingularValueDecomposition(mtv);
    Matrix u = svd.getU();
    Matrix vt = svd.getV().transpose();
    double[] sValues = svd.getSingularValues();
    System.out.println("TV ratings matrix: ");
    printMatrix(mtv, SEASONS, BOYS);
    System.out.println("U matrix left singular: ");
    printMatrix(u, SEASONS, new String[] {"","","","","",""});
    System.out.println("U2 matrix: ");
    printMatrix(u, SEASONS, new String[4]);
    System.out.println("Singular values: " + Arrays.toString(sValues));
    System.out.println("Vtranspose2 matrix: ");
    printMatrix(vt, BOYS, new String[6]);
    //    System.out.println("covariance matrix:");
    //    printMatrix(svd.getCovariance(0.0001), 4, 4);
    Matrix bob = new DenseMatrix(new double[][] {{5,5,0,0,0,5}});
    Matrix love = new DenseMatrix(new double[][] {{5,5,5,5,5,5}});
    Matrix hate = new DenseMatrix(new double[][] {{1,1,1,1,1,1}});
    newUser(svd, u, "Bob", bob);
    newUser(svd, u, "Love", love);
    newUser(svd, u, "Hate", hate);
    
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
  
  private static void printMatrix(Matrix m, String[] rowLabels, String[] colLabels) {
    PrintStream ps = System.out ; // new PrintStream(f);
    int numRows = rowLabels[0] != null ? m.numRows() : 2;
    int numColumns = colLabels[0] != null ? m.numCols() : 2;
    ps.print("\tlabels");
    for(int i = 0; i < numColumns; i++) 
      ps.print("," + colLabels[i]);
    ps.println();
    for(int r = 0; r < numRows; r++) {
      ps.print("\t" + rowLabels[r] + ",");
      for(int c = 0; c < numColumns; c++) {
        double v = m.get(r, c);
        ps.print(dub(v));
        if (c != numColumns - 1)
          ps.print(",");
      }
      ps.println();
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
