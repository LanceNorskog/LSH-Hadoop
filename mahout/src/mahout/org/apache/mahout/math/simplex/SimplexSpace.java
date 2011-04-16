package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;


/*
 * Contain a set of simplexes in space.
 * Optional: key for each simplex
 */

public class SimplexSpace<T> {
  public static Simplex newSimplex(Vector v, Hasher hasher) {
    int[] hashes = new int[v.size()];
    if (v.isDense()) {
      double[] values = new double[v.size()];
      getValues(v, values);
      hasher.hashDense(values, hashes);
      return new Simplex(hashes);
    } else {
      // this only works with Orthonormal
      // vertextransitive has be changed to read all, then give each dimension
      double[] d = new double[1];
      int[] h = new int[1];
      Iterator<Element> el = v.iterateNonZero();
      while(el.hasNext()) {
        Element e = el.next();
        d[0] = e.get();
        hasher.hashDense(d, h);
        hashes[e.index()] = h[0];
      }
      return new Simplex(hashes);
    }
  }
  
  private static void getValues(Vector v, double[] values) {
    Iterator<Element> el = v.iterateNonZero();
    while(el.hasNext()) {
      Element e = el.next();
      values[e.index()] = e.get();
    }
  }  
  
  final List<Simplex> simplexes = new ArrayList<Simplex>();
  final Map<T, Simplex> key2simplexMap = new HashMap<T, Simplex>();
  final Hasher hasher;
  final int dimensions;

  
  public SimplexSpace(Hasher hasher, int dimensions) {
    this.hasher = hasher;
    this.dimensions = dimensions;
  }
  
  public void addSimplex(Simplex x) {
    simplexes.add(x);
  }
  
  public void addSimplex(Simplex simplex, T id) {

  }
  
  public Simplex getSimplex(Vector v) {
    return newSimplex(v, hasher);
  }
  
  public Vector getVector(Simplex simplex) {
    return new SimplexVector(simplex, hasher);
  }
  
  public double getDistance(T key1, T key2, DistanceMeasure measure) {
    Simplex h1 = key2simplexMap.get(key1);
    Simplex h2 = key2simplexMap.get(key2);
    if (null == h1 || null == h2)
      return -1;
    
    double d = hashDistance(h1, h2, measure);
    return d;
  }
  
  private double hashDistance(Simplex h1, Simplex h2, DistanceMeasure measure) {
    Vector v1 = new SimplexVector(h2, hasher);
    Vector v2 = new SimplexVector(h2, hasher);
    double distance = measure.distance(v1, v2);
    return distance;
  }
  
  
  
}
