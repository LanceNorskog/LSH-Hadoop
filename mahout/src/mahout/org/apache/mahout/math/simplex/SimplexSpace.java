package org.apache.mahout.math.simplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.simplex.Hasher;


/*
 * Bag for a bunch of simplexes. Convenience methods for Simplex/Vector interactions.
 * Optional T: label for each simplex. NamedVector has a String for Vector.
 * Later: level-of-detail manipulations
 */

public class SimplexSpace<T> {
  final List<Simplex<T>> simplexes = new ArrayList<Simplex<T>>();
  final Map<T, Simplex<T>> key2simplexMap = new HashMap<T, Simplex<T>>();
  final Hasher hasher;
  final int dimensions;

  public SimplexSpace(Hasher hasher, int dimensions) {
    this.hasher = hasher;
    this.dimensions = dimensions;
  }
  
  public void addSimplex(Simplex<T> x) {
    simplexes.add(x);
  }
  
  public void addSimplex(Simplex<T> simplex, T id) {

  }
  
  public Simplex<T> getSimplex(Vector v) {
    if (v instanceof NamedVector) {
      String label = (String) ((NamedVector)v).getName();
      if (label instanceof String) {
        return newSimplex(v, hasher, (T) label);
      } else {
        throw new IllegalArgumentException("NamedVector is only compatible with Simplex<String>");
      }
    } else {
      return newSimplex(v, hasher, null);
    }
  }
  
  public Vector getVector(Simplex<T> simplex) {
    return new SimplexVector(simplex, hasher);
  }
  
  public double getDistance(T key1, T key2, DistanceMeasure measure) {
    Simplex<T> h1 = key2simplexMap.get(key1);
    Simplex<T> h2 = key2simplexMap.get(key2);
    if (null == h1 || null == h2)
      return -1;
    
    double d = hashDistance(h1, h2, measure);
    return d;
  }
  
  private double hashDistance(Simplex<T> h1, Simplex<T> h2, DistanceMeasure measure) {
    Vector v1 = new SimplexVector(h2, hasher);
    Vector v2 = new SimplexVector(h2, hasher);
    double distance = measure.distance(v1, v2);
    return distance;
  }
  
  
  public Simplex<T> newSimplex(Vector v, Hasher hasher, T label) {
    int[] hashes = new int[v.size()];
//    if (v.isDense()) {
      double[] values = new double[v.size()];
      boolean[] neighbors = new boolean[v.size()];
      getValues(v, values);
      hasher.hashDense(values, hashes);
      return new Simplex<T>(hashes, neighbors, label);
//    } 
/*    else {
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
    }*/
  }
  
  private static void getValues(Vector v, double[] values) {
    Iterator<Element> el = v.iterateNonZero();
    while(el.hasNext()) {
      Element e = el.next();
      values[e.index()] = e.get();
    }
  } 
  
}
