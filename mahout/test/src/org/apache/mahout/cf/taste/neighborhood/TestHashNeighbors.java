package org.apache.mahout.cf.taste.neighborhood;

import java.util.HashMap;
import java.util.Map;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

import junit.framework.TestCase;

import lsh.mahout.core.DenseHash;
import lsh.mahout.core.DenseHashNeighbors;
import lsh.mahout.core.Hash;
import lsh.mahout.core.Hasher;
import lsh.mahout.core.OrthonormalHasher;
import lsh.mahout.core.SimplexSpace;
import lsh.mahout.core.SparseHash;

public class TestHashNeighbors extends TestCase {
  int DIMENSIONS = 5;
  
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  @Test
  public void testDense() {
    int dimensions = 2;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    int[] hashes1 = new int[dimensions];
    int[] hashes2 = new int[dimensions];
    hashes1[0] = 0;
    hashes1[1] = 0;
    hashes2[0] = 0;
    hashes2[1] = 1;
    Hash dh1 = new DenseHash(hashes1);
    Hash dh2 = new DenseHash(hashes2);
    DenseHashNeighbors dhn = new DenseHashNeighbors(dimensions, hasher.getNeighbors(), 2);
    dhn.addHash(dh1);
    dhn.addHash(dh2);
    Hash[] nabes0 = dhn.getNeighbors(dh1, 0);
    Hash[] nabes1 = dhn.getNeighbors(dh2, 1);
    assertTrue(nabes0.length == dimensions);
    assertTrue(nabes1.length == dimensions);
    assertTrue(null == nabes1[0]);
  }
  
}
