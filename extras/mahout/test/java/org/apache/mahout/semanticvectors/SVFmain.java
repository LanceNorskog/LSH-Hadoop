package org.apache.mahout.semanticvectors;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;


public class SVFmain {
  
  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_100k/ratings.dat"));
    int dimensions = 200;
    DistanceMeasure measure = new ManhattanDistanceMeasure();
    double rescale;
    
    SemanticVectorFactory svf = new SemanticVectorFactory(model, dimensions, new Random(0));
    //    Vector v = svf.getUserVector(100, 20, 50);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    //    Vector v2 = svf.getItemVector(1282, 10, 20);
    //    System.out.println("count: " + svf.count + ", skip: " + svf.skip);
    dimensions = svf.getDimensions();
    Vector unitVector = new DenseVector(dimensions);
    Vector zeroVector = new DenseVector(dimensions);
    for(int i = 0; i < dimensions; i++) 
      unitVector.set(i, 1.0);
    rescale = measure.distance(zeroVector, unitVector);
    checkUserDistances(svf, model, measure, rescale);
    System.out.println();
    checkItemDistances(svf, model, measure, rescale);
  }
  
  private static void checkUserDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 10, 40);
    }
    showDistributions(va, measure, rescale);
  }
  
  private static void checkItemDistances(SemanticVectorFactory svf, DataModel model, DistanceMeasure measure, double rescale) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      va[i] = svf.getItemVector(itemID, 10, 40);
    }
    showDistributions(va, measure, rescale);
  }
  
  private static void showDistributions(Vector[] va,
      DistanceMeasure measure, double rescale) {
    int[] buckets = new int[21];
    double delta = 0;
    int count = 0;
    double max = 0;
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < va.length; j++) {
        if ((null == va[i]) || (va[j] == null))
          continue;
        double distance = measure.distance(va[i], va[j]);
        if (max < distance)
          max = distance;
        
        distance /= rescale;
//        distance = Distributions.normal2linear(distance);
        buckets[(int) (distance*20)]++;
        for(int k = 0; k < va[i].size(); k++) {
          double d = (Math.abs(va[i].get(k) - va[j].get(k)));
          delta += d;
          count++;
        }
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
    System.out.println("average point dist: " + (delta/count) + ", max: " + max);
  }
  

  
}
