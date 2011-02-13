package org.apache.mahout.cf.taste.neighborhood;

import java.util.HashMap;
import java.util.Map;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

import junit.framework.TestCase;
import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;

public class TestHashes extends TestCase {
  
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
//  @Test
//  public void testEquals() {
//    int dimensions = 2;
//    int limit = 9;
//    Hasher hasher = new OrthonormalHasher(dimensions, 0.1d);
//    // starting at LOD=9, the two hashes are equal- both are zero
//    for(int i = 0; i <32; i++) {
//      SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
//      space.setLOD(i);
//      Vector d = new DenseVector(dimensions);
//      Vector s = new RandomAccessSparseVector(dimensions, dimensions);
//      checkEquals(space, d, d);
//      checkEquals(space, d, s);
//      checkEquals(space, s, d);
//      checkEquals(space, s, s);
//      checkNotEquals(space, d, s, i >= limit);
//      checkNotEquals(space, s, d, i >= limit);
//    }
//  }
  
  private void checkEquals(SimplexSpace<String> space, Vector v1, Vector v2) {
    v1.set(0, 1.1);
    v1.set(1, 2.2);
    v2.set(0, 1.1);
    v2.set(1, 2.2);
    Hash h1 = space.getHashLOD(v1);
    Hash h2 = space.getHashLOD(v2);
    assertTrue(h1.equals(h1));
    assertTrue(h1.equals(h2));
    assertTrue(h2.equals(h1));
    assertTrue(h2.equals(h2));
  }
  
  private void checkNotEquals(SimplexSpace<String> space, Vector v1, Vector v2, boolean eq) {
    v1.set(0, 1.1);
    v1.set(1, 2.2);
    v2.set(0, 50);
    v2.set(1, 30);
    Hash h1 = space.getHashLOD(v1);
    Hash h2 = space.getHashLOD(v2);
    assertTrue(null != h1);
    assertTrue(null != h2);
    if (eq) {
      assertTrue(h2.equals(h1));
      assertTrue(h1.equals(h2)); 
    } else {
      assertFalse(h2.equals(h1));
      assertFalse(h1.equals(h2));
    }
  }
  
  public void testCollections() {
    int dimensions = 2;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector d1 = new DenseVector(dimensions);
    Vector d2 = new DenseVector(dimensions);
    Vector s1 = new RandomAccessSparseVector(dimensions, dimensions);
    Vector s2 = new RandomAccessSparseVector(dimensions, dimensions);
    checkCollection(space, d1, d2);
    checkCollection(space, d1, s1);
    checkCollection(space, s1, d1);
    checkCollection(space, s1, s2);
  }
  
  
  private void checkCollection(SimplexSpace<String> space, Vector v1, Vector v2) {
    Map<Hash,String> m = new HashMap<Hash,String>();
    v1.set(0, 1.1);
    v1.set(1, 2.2);
    v2.set(0, 1.1);
    v2.set(1, 2.2);
    Hash h1 = space.getHashLOD(v1);
    Hash h2 = space.getHashLOD(v2);
     m.put(h1, "a");
    assertTrue(m.containsKey(h1));
    assertTrue(m.containsKey(h2));
    m.clear();
    m.put(h2, "b");
    assertTrue(m.containsKey(h1));
    assertTrue(m.containsKey(h2));
    m.clear();
    m.put(h1, "a");
    m.put(h2, "b");
    assertEquals(m.get(h1), "b");
    Vector v = new DenseVector(2);
    v.set(1,3);
    Hash h = space.getHashLOD(v);
    assertFalse(m.containsKey(h));
  }
  
  public void testDenseCount() {
    int dimensions = 2;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.1d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector v = new DenseVector(dimensions);
  }
  
  
  
}
