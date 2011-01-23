package org.apache.mahout.math;

import java.util.Iterator;

/*
 * Sparsify directly from random
 * For each entry, sparsify factor defines whether entry exists.
 * Needs non-negative iterator
 */

public class RandomSparsifiedMatrix extends FabricatedMatrix {

  public Vector getColumn(int column) {
    // TODO Auto-generated method stub
    return null;
  }

  public double getQuick(int row, int column) {
    // TODO Auto-generated method stub
    return 0;
  }

  public Vector getRow(int row) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Iterator<MatrixSlice> iterator() {
    // TODO Auto-generated method stub
    return super.iterator();
  }

}
