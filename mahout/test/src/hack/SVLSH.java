package hack;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import lsh.mahout.core.Hasher;
import lsh.mahout.core.OrthonormalHasher;
import lsh.mahout.core.SimplexSpace;
import lsh.mahout.core.VertexTransitiveHasher;
import lsh.mahout.core2.Simplex;
import lsh.mahout.core2.SimplexIterator;
import lsh.mahout.core2.SimplexVector;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.distance.ChebyshevDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.semanticvectors.SemanticVectorFactory;


public class SVLSH {
static  double rescaleMan;
static double rescaleTaxi;

  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_10k/ratings.dat"));
    int dimensions = 200;
    
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
    rescaleMan = (new ManhattanDistanceMeasure()).distance(zeroVector, unitVector);
    rescaleTaxi = (new ChebyshevDistanceMeasure()).distance(zeroVector, unitVector);
    rescaleTaxi = dimensions * 10;
    System.out.println("User-User distances");
    userUserDistances(svf, model);
    System.out.println();
    System.out.println("Item-Item distances");
    itemItemDistances(svf, model);
    System.out.println();
    System.out.println("User-Item distances");
    userItemDistances(svf, model);
  }
  
  private static void userUserDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 10, 40);
    }
    countall(va, null);
  }
  
  private static void itemItemDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      va[i] = svf.getItemVector(itemID, 10, 40);
    }
    countall(va, null);
  }
  
  private static void userItemDistances(SemanticVectorFactory svf, DataModel model) throws TasteException {
    Vector[] va = new Vector[model.getNumUsers()];
    LongPrimitiveIterator users = model.getUserIDs();
    for(int i = 0; i < model.getNumUsers(); i++) {
      int userID = (int) users.nextLong();
      va[i] = svf.getUserVector(userID, 10, 40);
    }
    Vector[] vb = new Vector[model.getNumItems()];
    LongPrimitiveIterator items = model.getItemIDs();
    for(int i = 0; i < model.getNumItems(); i++) {
      int itemID = (int) items.nextLong();
      vb[i] = svf.getItemVector(itemID, 10, 40);
    }
    countall(va, vb);
  }
  
  private static void countall(Vector[] va, Vector[] vb) {
    System.out.println("Semantic Vectors");
    showDistributions(va, vb, new ManhattanDistanceMeasure(), rescaleMan);
    System.out.println("Simplex");
    int dimension = 0;
    dimension = simplexify(va, dimension);
    if (null != vb)
      simplexify(vb, dimension);
    showDistributions(va, vb, new ChebyshevDistanceMeasure(), rescaleTaxi);
  }

  private static int simplexify(Vector[] va, int dimension) {
    for(int i = 0; i < va.length; i++) {
      if (null == va[i])
        continue;
      dimension = va[i].size();
      Hasher hasher = new VertexTransitiveHasher(dimension, 0.01);
      SimplexIterator sit = new SimplexIterator(hasher, va[i]);
      Simplex sx = sit.next();  // first is always main
      Vector sxv = new SimplexVector(sx, hasher);
      va[i] = sxv;
    }
    return dimension;
  }
  
  private static void showDistributions(Vector[] va,
      Vector[] vb, DistanceMeasure measure, double rescale) {
    int[] buckets = new int[21];
    double delta = 0;
    int count = 0;
    double max = 0;
    if (null == vb)
      vb = va;
    for(int i = 0; i < va.length;i++) {
      for(int j = i + 1; j < vb.length; j++) {
        if ((null == va[i]) || (vb[j] == null))
          continue;
        double distance = measure.distance(va[i], vb[j]);
        if (max < distance)
          max = distance;
        
        distance /= rescale;
//        distance = Distributions.normal2linear(distance);
          buckets[(int) (distance*19)]++;
        delta += distance;
        count++;
      }
    }
    for(int i = 0; i < 20; i++) {
      System.out.println(buckets[i]);
    }
    System.out.println("average point dist: " + (delta/(count * 1.0)) + ", max: " + max);
  }
  

  
}
