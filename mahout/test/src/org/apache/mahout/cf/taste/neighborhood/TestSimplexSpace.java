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
import lsh.mahout.core.Hash;
import lsh.mahout.core.SimplexSpace;
import lsh.mahout.core.SparseHash;

public class TestSimplexSpace extends TestCase {
  int DIMENSIONS = 5;
  
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  @Test
  public void testEquals() {
    int limit = 100;
    Hasher hasher = new OrthonormalHasher(DIMENSIONS, 0.1d);
    // starting at LOD=9, the two hashes are equal- both are zero
    for(int i = 0; i <32; i++) {
System.out.println("#" + i);
      SimplexSpace<String> space = new SimplexSpace<String>(hasher, DIMENSIONS, null, false, true);
      space.setLOD(i);
      Vector d = new DenseVector(DIMENSIONS);
      Vector s = new RandomAccessSparseVector(DIMENSIONS, DIMENSIONS);
      checkEquals(space, d, d);
//      checkEquals(space, d, s);
//      checkEquals(space, s, d);
      checkEquals(space, s, s);
      checkNotEquals(space, d, s, i >= limit);
      checkNotEquals(space, s, d, i >= limit);
    }
  }
  
  private void checkEquals(SimplexSpace<String> space, Vector v1, Vector v2) {
    v1.set(0, 1.1);
    v1.set(1, 2.2);
    v1.set(4, 4.4);
    v2.set(0, 1.1);
    v2.set(1, 2.2);
    v1.set(4, 4.4);
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
    v2.set(2, 30);
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
  
  @Test
  public void testCollections() {
    int DIMENSIONS = 5;
    Hasher hasher = new OrthonormalHasher(DIMENSIONS, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, DIMENSIONS, null, false, true);
    Vector d1 = new DenseVector(DIMENSIONS);
    Vector d2 = new DenseVector(DIMENSIONS);
    Vector s1 = new RandomAccessSparseVector(DIMENSIONS, DIMENSIONS);
    Vector s2 = new RandomAccessSparseVector(DIMENSIONS, DIMENSIONS);
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
    int DIMENSIONS = 2;
    Hasher hasher = new OrthonormalHasher(DIMENSIONS, 0.1d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, DIMENSIONS, null, false, true);
    Vector v = new DenseVector(DIMENSIONS);
  }
  
  @Test
  public void testSparseEquals() {
    int dimensions = 20;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector s1 = new RandomAccessSparseVector(dimensions, dimensions);
    Vector s2 = new RandomAccessSparseVector(dimensions, dimensions);
    s1.set(1, 1.1);
    s1.set(3, 3.3);
    s2.set(1, 1.1);
    s2.set(6, 6.6);
    Hash h1 = space.getHashLOD(s1);
    Hash h2 = space.getHashLOD(s2);
    assertFalse(h1.hashCode() == h2.hashCode());
    assertFalse(h1.equals(h2));
    s1.set(6, 6.6);
    s2.set(3, 3.3);
    h1 = space.getHashLOD(s1);
    h2 = space.getHashLOD(s2);
    assertTrue(h1.hashCode() == h2.hashCode());
    assertTrue(h1.equals(h2));

  }
  
  @Test
  public void testSetValue() {
    int dimensions = 20;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector d = new DenseVector(dimensions);
    Vector s = new RandomAccessSparseVector(dimensions, dimensions);
    d.set(0, 0.00001);
    d.set(2, 2.2);
    d.set(5, 5.5);
    s.set(0, 0.00001);
    s.set(3, 3.3);
    s.set(6, 6.6);
    
    Hash hd = space.getHashLOD(d);
    Hash hs = space.getHashLOD(s);
    assertFalse(hd.hashCode() == hs.hashCode());
    assertFalse(hd.equals(hs));
    Integer zeroD = hd.getValue(0);
    Integer zeroS = hs.getValue(0);
    assertEquals(0, (int) zeroD);
    assertNull(zeroS);
    System.out.println("Dense:  " + hd.toString());
    System.out.println("Sparse: " + hs.toString());
    Integer three = hs.getValue(3);
    hd.setValue(3, three);
    Integer six = hs.getValue(6);
    hd.setValue(6, six);
    Integer two = hd.getValue(2);
    hs.setValue(2, two);
    Integer five = hd.getValue(5);
    hs.setValue(5, five);
    System.out.println("Dense:  " + hd.toString());
    System.out.println("Sparse: " + hs.toString());
    assertTrue(hd.hashCode() == hs.hashCode());
    assertTrue(hd.equals(hs));
}
  
  @Test
  public void testSetValueFail() {
    int dimensions = 20;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector d = new DenseVector(dimensions);
    Vector s = new RandomAccessSparseVector(dimensions, dimensions);
    d.set(0, 1.1);
    d.set(3, 2.2);
    s.set(0, 1.1);
    s.set(6, 2.2);
    Hash hd = space.getHashLOD(d);
    Hash hs = space.getHashLOD(s);
    assertFalse(hd.hashCode() == hs.hashCode());
    assertFalse(hd.equals(hs));
  }
  
  @Test
  public void testCopyConstructor() {
    int dimensions = 20;
    Hasher hasher = new OrthonormalHasher(dimensions, 0.01d);
    SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
    Vector s1 = new DenseVector(dimensions);
    Vector s2 = new RandomAccessSparseVector(dimensions, dimensions);
    s1.set(0, 1.1);
    s1.set(3, 2.2);
    s2.set(0, 1.1);
    s2.set(6, 2.2);
    Hash h1 = space.getHashLOD(s1);
    SparseHash h2 = (SparseHash) space.getHashLOD(s2);
    h2 = new SparseHash(h2, 0);
    assertFalse(h1.hashCode() == h2.hashCode());
    assertFalse(h1.equals(h2));
  }
  
}
