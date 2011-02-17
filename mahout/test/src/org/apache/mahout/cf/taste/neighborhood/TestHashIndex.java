package org.apache.mahout.cf.taste.neighborhood;

import junit.framework.TestCase;

public class TestHashIndex extends TestCase {
  
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testCheckAndSetHash() {
    int dimensions = 5;
    DenseHashIndex dhi = new DenseHashIndex(dimensions);
    int[] hashes = new int[dimensions];
    hashes[0] = 3;
    hashes[2] = 4;
    SparseHash sp1 = new SparseHash(hashes);
    hashes = new int[dimensions];
    hashes[0] = 2;
    hashes[1] = 5;
    SparseHash sp2 = new SparseHash(hashes);
    
  }
  
  public void testCheckHash() {
    fail("Not yet implemented");
  }
  
}
