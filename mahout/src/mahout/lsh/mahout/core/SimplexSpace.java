/**
 * 
 */
package lsh.mahout.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lsh.core.Hasher;

import org.apache.hadoop.io.IntWritable;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
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
 * 
 * Have to store payloads at this level instead of in Hash objects.
 * Needed for 'findneighbors'.
 */
public class SimplexSpace<T> {
  final Hasher hasher;
  Map<T, Vector> id2vectorMap = new HashMap<T, Vector>();
  Map<T, Hash> id2hashMap = new HashMap<T, Hash>();
  Map<Hash, Set<T>> payloadSetMap = new HashMap<Hash, Set<T>>();
  Map<Hash, Set<Vector>> vectorSetMap = new HashMap<Hash, Set<Vector>>();
  Map<Hash, Integer> countMap = new HashMap<Hash,Integer>();
  final int dimensions;
  public double distance = 0.0001;
  public boolean doUnhash = true;
  private final DistanceMeasure measure;
  int lod = 0;
  final boolean doMapVectors;
  final boolean doCount;
  int maxhash = -Integer.MAX_VALUE;
  int minhash = Integer.MAX_VALUE;
  
  public SimplexSpace(Hasher hasher, int dimensions) {
    this.hasher = hasher;
    this.dimensions = dimensions;
    this.measure = null;
    doMapVectors = false;
    doCount = false;
    allocate();
  }
  
  public SimplexSpace(Hasher hasher, int dimensions, DistanceMeasure measure, boolean mapVectors, boolean count) {
    this.hasher = hasher;
    this.dimensions = dimensions;
    this.measure = measure;
    doMapVectors = mapVectors;
    doCount = count;
    allocate();
    }
  
  void allocate() {
    id2vectorMap = new HashMap<T, Vector>(50000);
    id2hashMap = new HashMap<T, Hash>(50000);
    payloadSetMap = new HashMap<Hash, Set<T>>(50000);
    vectorSetMap = new HashMap<Hash, Set<Vector>>(50000);
    countMap = new HashMap<Hash,Integer>(50000);

  }
  // populate hashes
  public void addVector(Vector v, T payload) {
    Hash hash = getHashLOD(v);
    addHash(v, hash, payload);
  }
  
  public void addHash(Vector v, Hash hash, T payload) {
    Iterator<Integer> it = hash.iterator();
    int lod2 = (int) Math.pow(2.0,hash.getLOD());
    while (it.hasNext()) {
      Integer index = it.next();
      if (null != index) {
        int value = hash.getValue(index);
        if (hash.getLOD() > 0) {
          value /= lod2;
        }
        if (value > maxhash)
          maxhash = value;
        if (value < minhash)
          minhash = value;
      }
    }
    if (doCount) {
      if (!countMap.containsKey(hash)) {
        countMap.put(hash, null);
      } else {
        Integer counter = countMap.get(hash);
        if (null == counter) {
          counter = 1;
        }
        else hashCode();
        counter++;
        countMap.put(hash, counter);
      }
    } else {
      Set<Vector> vectorKeys = vectorSetMap.get(hash);
      Set<T> payloadKeys = null;
      if (null != payload)
        payloadKeys = payloadSetMap.get(hash);
      if (null == vectorKeys) {
        vectorKeys = new HashSet<Vector>();
        vectorSetMap.put(hash, vectorKeys);
        if (null != payload) {
          payloadKeys = new HashSet<T>();
          payloadSetMap.put(hash, payloadKeys);
        }
      } else
        vectorKeys.hashCode();
      vectorKeys.add(v);
      if (null != payload) {
        id2vectorMap.put(payload, v);
        id2hashMap.put(payload, hash);
        payloadKeys.add(payload);
      }
    }
  }
  
  /*
   * Mask hash value to the current level of detail
   */
  public Hash getHashLOD(Vector v) {
    int[] hashes;
    if (v.isDense()) {
      double[] values = new double[dimensions];
      getValues(v, values);
      hashes = hasher.hash(values);
      return new DenseHash(hashes, lod);
    } else {
      hashes = new int[dimensions];
      double[] d = new double[1];
      int[] h = new int[1];
      Iterator<Element> el = v.iterateNonZero();
      while(el.hasNext()) {
        Element e = el.next();
        d[0] = e.get();
        h = hasher.hash(d);
        hashes[e.index()] = h[0];
      }
      return new SparseHash(hashes, lod);
    }
  }
  
  private void getValues(Vector v, double[] values) {
    Iterator<Element> el = v.iterateNonZero();
    while(el.hasNext()) {
      Element e = el.next();
      values[e.index()] = e.get();
    }
  }    
  
  /*
   * Enumerate other co-resident hashes.
   * Do not add input hash.
   */
  
    public FastIDSet findNeighborsIDSet(T payload) {
      Hash hash = id2hashMap.get(payload);
      if (null == hash)
        return null;
      FastIDSet others = new FastIDSet(id2hashMap.size());
      Set<T> hashKeys = payloadSetMap.get(hash);
      for(T otherID: hashKeys) {
        if (! otherID.equals(payload)){
          long id = (Long) otherID;
          others.add(id);
        }
      }
      return others;
    }
    
    public Map<T, Set<T>> findNeighbors(T other) {
      Hash hash = id2hashMap.get(other);
      if (null == hash)
        return null;
      Map<T, Set<T>> others = new HashMap<T, Set<T>>();
      Set<T> hashKeys = payloadSetMap.get(hash);
      for(T otherID: hashKeys) {
        if (! otherID.equals(other))
          hashKeys.add(other);
      }
      return others;
    }
    
    public double getDistance(T payload1, T payload2, DistanceMeasure measure) {
      if (null == measure)
        measure = this.measure;
      Hash h1 = id2hashMap.get(payload1);
      Hash h2 = id2hashMap.get(payload2);
      if (null == h1 || null == h2)
        return -1;
      
      double d = hashDistance(h1, h2, measure);
      return d;
    }
    
    public double getDistance(T payload1, T payload2, SimplexSpace<T> otherSpace, DistanceMeasure measure) {
      if (null == measure)
        measure = this.measure;
      Hash h1 = id2hashMap.get(payload1);
      Hash h2 = otherSpace.id2hashMap.get(payload2);
      if (null == h1 || null == h2) {
        return -1;
      }
      
      double d = hashDistance(h1, h2, measure);
      if (d != 0.0)
        System.out.println(d);
      return d;
    }
  
  private double hashDistance(Hash h1, Hash h2, DistanceMeasure measure) {
//    double[] d1 = new double[dimensions];
//    double[] d2 = new double[dimensions];
//    int[] hashes1 = h1.getHashes();
//    int[] hashes2 = h2.getHashes();
//    if (doUnhash) {
//      hasher.unhash(hashes1, d1);
//      hasher.unhash(hashes2, d2);
//    } else {
//      for(int i = 0; i < dimensions; i++) {
//        d1[i] = hashes1[i];
//        d2[i] = hashes2[i];
//      }
//    }
    Vector v1 = new HashVector(h1, hasher);
    Vector v2 = new HashVector(h2, hasher);
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
  }
  
  public void stDevCounts() {
    CompactRunningAverageAndStdDev std = new CompactRunningAverageAndStdDev();
    for(Hash h: vectorSetMap.keySet()) {
      std.addDatum(vectorSetMap.get(h).size());
    }
    System.out.println("Entries: " + vectorSetMap.size());
    System.out.println("Mean: " + std.getAverage());
    System.out.println("Stdev: " + std.getStandardDeviation());
  }
  
  //  public double stDevCenters() {
  //    FullRunningAverageAndStdDev std = new FullRunningAverageAndStdDev();
  //    for(Hash<T> h: vectorSetMap.keySet()) {
  //      Vector center = new DenseVector(dimensions);
  //      Set<Vector> vectors = vectorSetMap.get(h);
  //      for (Vector v: vectors) {
  //        v.addTo(center);
  //      }
  //      center = center.divide(vectors.size());
  //      double distance = 0;
  //      for (Vector v: vectors) {
  //        distance += measure.distance(v, center);
  //      }
  //      std.addDatum(distance);
  //    }
  //    System.out.println("Entries: " + hashSetMap.size());
  //    System.out.println("Mean: " + std.getAverage());
  //    System.out.println("Stdev: " + std.getStandardDeviation());
  //    return std.getStandardDeviation();
  //  }
  
  public double stDevCenters() {
    FullRunningAverageAndStdDev std = new FullRunningAverageAndStdDev();
    
    for(Hash h: vectorSetMap.keySet()) {
      //      List<FullRunningAverageAndStdDev> devs = new ArrayList<FullRunningAverageAndStdDev>();
      //      for(int i = 0; i < dimensions; i++) {
      Vector center = new DenseVector(dimensions);
      Set<Vector> vectors = vectorSetMap.get(h);
      for (Vector v: vectors) {
        v.addTo(center);
      }
      center = center.divide(vectors.size());
      RunningAverage avg = new FullRunningAverage();
      for (Vector v: vectors) {
        avg.addDatum(Math.abs(measure.distance(v, center)));
      }
      std.addDatum(avg.getAverage());
    }
    //    System.out.println("Entries: " + hashSetMap.size());
    //    System.out.println("Mean: " + std.getAverage());
    //    System.out.println("Stdev: " + std.getStandardDeviation());
    return std.getStandardDeviation();
  }
  
  @Override
  public String toString() {
    String x = "";
    if (null != id2vectorMap) {
      x += "ID{";
      Iterator<T> lpi = id2vectorMap.keySet().iterator();
      while (lpi.hasNext()) {
        T key = lpi.next();
        x += key.toString() + ",";
      }
      x += "}";
    }
    int count = 0;
    if (null != vectorSetMap) {
      x += "HASH{";
      for(Hash h: vectorSetMap.keySet()) {
        Set<Vector> hs = vectorSetMap.get(h);
        if (null == hs) {
          x += "0,";
          count++;
        } else {
          x += hs.size() + ",";
          count += hs.size();
        }
      }
      x += "}";
    }
    if (doCount) {
      x += "COUNT{";
      for(Hash h: countMap.keySet()) {
        Integer hs = countMap.get(h);
        if (null == hs)
          x += "1,";
        else
          x += hs + ",";
      }
      x += "}";
    } 
    x += "TOTAL{" + count + "}";
    return x;
  }
  
  // all hashes with more than one item
  public int getNonSingleHashes() {
    if (doCount) {
      int multi = 0;
      for(Hash h: countMap.keySet()) {
        Integer i = countMap.get(h);
        if (null == i) {
          continue;
        }
        multi++;
      }
      return multi;
    } else {
      int multi = 0;
      for(Set<Vector> x: vectorSetMap.values()) {
        int count = x.size();
        if (count > 1)
          multi++;
      }
      return multi;
    }
  }
  
  public int getCount() {
    if (doCount) {
      int count = 0;
      for(Hash h: countMap.keySet()) {
        Integer i = countMap.get(h);
        if (null == i) {
          count++;
          continue;
        }
        if (null != i)
          count += i;
      }
      return count;
    } else {
      int count = 0;
      for (Set<Vector> s: vectorSetMap.values()) {
        count += s.size();
      }
      return count;
    }
  }
  
  // maximum number of items in a hash
  public int getMaxHashes() {
    if (doCount) {
      int max = 0;
      for(Hash h: countMap.keySet()) {
        Integer i = countMap.get(h);
        if (null != i) {
          if (i > max)
            max = i;
        } 
      }
      return max;
    } else {
      int max = 0;
      for(Set<Vector> s: vectorSetMap.values()) {
        Integer i = s.size();
        if (null != i) {
          if (i > max)
            max = i;
        } 
      }
      return max;
    }
  }
  
  public int getMinHash() {
    return minhash;
  }
  
  public int getMaxHash() {
    return maxhash;
  }

}
