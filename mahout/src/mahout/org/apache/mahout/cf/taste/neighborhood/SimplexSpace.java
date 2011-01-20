/**
 * 
 */
package org.apache.mahout.cf.taste.neighborhood;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lsh.core.Hasher;

import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

/**
 * @author lance
 *
 * Store LSH Hash<T> set of corners and users.
 * Corner is Hash<T> vector
 * LOD - Level Of Detail
 * 
 * Does not know of User or Item - type T is long
 */
public class SimplexSpace<T> {
  final Hasher hasher;
  Map<T, Hash<T>> idSetMap = new HashMap<T, Hash<T>>();
  Map<Hash<T>, Set<T>> hashSetMap = new HashMap<Hash<T>, Set<T>>();
  final int dimensions;
  public double distance = 0.0001;
  public boolean doUnhash = true;
  private final DistanceMeasure measure;
  int lod = 0;
  private int lodMask;

  public SimplexSpace(Hasher hasher, int dimensions) {
    this.hasher = hasher;
    this.dimensions = dimensions;
    this.measure = null;
  }

  public SimplexSpace(Hasher hasher, int dimensions, DistanceMeasure measure) {
    this.hasher = hasher;
    this.dimensions = dimensions;
    this.measure = measure;
  }

  // populate hashes
  public void addVector(T payload, Vector v) {
    double[] values = new double[dimensions];
    getValues(v, values);
    Hash<T> hash = getHashLOD(values, payload);
    idSetMap.put(payload, hash);
    Set<T> hashKeys = hashSetMap.get(hash);
    if (null == hashKeys) {
      hashKeys = new HashSet<T>();
      hashSetMap.put(hash, hashKeys);
    } else
      this.hashCode();
    hashKeys.add(payload);
  }

  /*
   * Mask hash value to the current level of detail
   */
  private Hash<T> getHashLOD(double[] values, T payload) {
    int[] hashes = hasher.hash(values);

    for(int i = 0; i < dimensions; i++) {
      hashes[i] &= ~lodMask;
    }
    return new Hash<T>(hashes, lod, payload);
  }

  private void getValues(Vector v, double[] values) {
    Iterator<Element> el = v.iterateNonZero();
    while(el.hasNext()) {
      Element e = el.next();
      values[e.index()] = e.get();
    }
  }    

  /*
   * Search for neighbors of given ID.
   *    expand - expand search N counts outward
   */

  //  public long[] findNeighbors(long id, int expand) {
  //    Hash central = idSetMap.get(id);
  //    if (null == central)
  //      return null;
  //    FastIDSet others = new FastIDSet();
  //    Hash cloned = (Hash) central.clone();
  //    int found = 0;
  //    for (int i = 0; i < dimensions; i++) {
  //      int last = found;
  //      cloned.hashes[i] = central.hashes[i] + 1;
  //      Set<Long> hashLongs = hashSetMap.get(cloned);
  //      if (null != hashLongs)
  //        for(Long otherID: hashLongs) {
  //          if (otherID != id)
  //            others.add(otherID);
  //        }
  //      cloned.hashes[i] = central.hashes[i] - 1;
  //      hashLongs = hashSetMap.get(cloned);
  //      if (null != hashLongs)
  //        for(Long otherID: hashLongs) {
  //          if (otherID != id)
  //            others.add(otherID);
  //        }     
  //      found = others.size();
  //      if (found > last) {
  //        System.out.println(i + "," + (found - last));
  //      }
  //    }
  //    long[] values = new long[others.size()];
  //    LongPrimitiveIterator lpi = others.iterator();
  //    for(int i = 0; i < others.size(); i++) {
  //      values[i] = lpi.nextLong();
  //    }
  //    return values;
  //  }

  /*
   * Enumerate other co-resident hashes.
   * Do not add input hash.
   */
  public FastIDSet findNeighbors(long other) {
    Hash<T> hash = idSetMap.get(other);
    if (null == hash)
      return null;
    FastIDSet others = new FastIDSet(idSetMap.size());
    Set<T> hashKeys = hashSetMap.get(hash);
    for(T otherID: hashKeys) {
      if (! otherID.equals(other)){
        long id = (Long) otherID;
        others.add(id);
      }
    }
    return others;
  }

  public Map<T, Set<T>> findNeighbors(T other) {
    Hash<T> hash = idSetMap.get(other);
    if (null == hash)
      return null;
    Map<T, Set<T>> others = new HashMap<T, Set<T>>();
    Set<T> hashKeys = hashSetMap.get(hash);
    for(T otherID: hashKeys) {
      if (! otherID.equals(other))
        hashKeys.add(other);
    }
    return others;
  }

  public double getDistance(long id1, long id2, DistanceMeasure measure) {
    if (null == measure)
      measure = this.measure;
    Hash<T> h1 = idSetMap.get(id1);
    Hash<T> h2 = idSetMap.get(id2);
    if (null == h1 || null == h2)
      return -1;

    double d = hashDistance(h1, h2, measure);
    return d;
  }

  public double getDistance(long id1, long id2, SimplexSpace<T> otherSpace, DistanceMeasure measure) {
    if (null == measure)
      measure = this.measure;
    Hash<T> h1 = idSetMap.get(id1);
    Hash<T> h2 = otherSpace.idSetMap.get(id2);
    if (null == h1 || null == h2) {
      return -1;
    }

    double d = hashDistance(h1, h2, measure);
    if (d != 0.0)
      System.out.println(d);
    return d;
  }

  private double hashDistance(Hash<T> h1, Hash<T> h2, DistanceMeasure measure) {
    double[] d1 = new double[dimensions];
    double[] d2 = new double[dimensions];
    if (doUnhash) {
      hasher.unhash(h1.hashes, d1);
      hasher.unhash(h2.hashes, d2);
    } else {
      for(int i = 0; i < h1.hashes.length; i++) {
        d1[i] = h1.hashes[i];
        d2[i] = h2.hashes[i];
      }
    }
    Vector v1 = new DenseVector(d1);
    Vector v2 = new DenseVector(d2);
    double distance = measure.distance(v1, v2);
    return distance;
  }

  public int getDimensions() {
    return dimensions;
  }

  public int getLOD() {
    return this.lod;
  }

  public void setLOD(int lod){
    this.lod = lod;
    int mask = 0;
    int x = 0;
    while(x < lod) {
      mask |= (1 << x);
      x++;
    }
    this.lodMask = mask;
  }

  public void stDev() {
    CompactRunningAverageAndStdDev std = new CompactRunningAverageAndStdDev();
    for(Hash<T> h: hashSetMap.keySet()) {
      std.addDatum(hashSetMap.get(h).size());
    }
    System.out.println("Entries: " + hashSetMap.size());
    System.out.println("Mean: " + std.getAverage());
    System.out.println("Stdev: " + std.getStandardDeviation());
  }

  @Override
  public String toString() {
    String x = "";
    if (null != idSetMap) {
      x += "ID{";
      Iterator<T> lpi = idSetMap.keySet().iterator();
      while (lpi.hasNext()) {
        T key = lpi.next();
        Hash<T> h = idSetMap.get(key);
        Set<T> ids = hashSetMap.get(h);
        x += ids.size() + ",";
      }
      x += "}";
    }
    if (null != hashSetMap) {
      x += "HASH{";
      for(Hash<T> h: hashSetMap.keySet()) {
        Set<T> hs = hashSetMap.get(h);
        if (null == hs)
          x += "0,";
        else
          x += hs.size() + ",";
      }
      x += "}";
    }
    return x;
  }
}
