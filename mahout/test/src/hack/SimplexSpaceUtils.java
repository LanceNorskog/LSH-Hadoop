package hack;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import lsh.mahout.core.OrthonormalHasher;
import lsh.mahout.core.SimplexSpace;
import lsh.mahout.core.VertexTransitiveHasher;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
import org.apache.mahout.common.distance.MinkowskiDistanceMeasure;
//import org.apache.mahout.common.distance.MinkowskiDistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;

import working.SemanticVectorFactory;

import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula;

import static org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula.*;

/*
 * Compare preferences returned by Recommenders and DataModels.
 * 
 * Usage: TestPreferenceBasedRecommender /path/of/GL/ratings.dat   <-- compare different recommenders
 *                            training.dat test.dat     <-- compare training and test data
 */

public class SimplexSpaceUtils {
  static lsh.mahout.core.SimplexUserNeighborhood sun = null;
  static int DIMS = 200;
  static double SIZE = 0.008;
  
  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    System.out.println("lod,countOrtho,countVT,ratio,stdevOrtho,stdevVT,ratio");
    for(int lod = 0; lod <= 19; lod++) {
      stats(glModel, lod);
    }
  }

  private static void stats(GroupLensDataModel glModel, int lod) throws TasteException {
    SimplexSpace<Long> spaceOrtho = getSpaceOrthonormal(DIMS);
    spaceOrtho.setLOD(lod);
    int countOrtho = loadSpace(glModel, spaceOrtho);
    double stdevOrtho = getStdev(spaceOrtho);
    SimplexSpace<Long> spaceVT = getSpaceVT(DIMS);
    spaceVT.setLOD(lod);
    int countVT = loadSpace(glModel, spaceVT);
    double stdevVT = getStdev(spaceVT);
//    System.out.println("LOD: " + lod + ", Ortho: " + countOrtho + ", VT: " + countVT + ", ratio:" + ((double) countOrtho)/countVT);
    System.out.println(lod + "," + countOrtho + "," + countVT + "," + (((double) countOrtho)/countVT) +
      "," + stdevOrtho + "," + stdevVT + "," + (stdevOrtho/stdevVT));
  }
  
  private static double getStdev(SimplexSpace<Long> space) {
    double value = space.stDevCenters();
    return value;
  }

  // give two ratings.dat files, training and test
  
  private static int loadSpace(DataModel model, SimplexSpace<Long> space) throws TasteException {
    addSimplices(space, model);
    return space.getNonSingleHashes(false);
  }
  
  private static SimplexSpace<Long> getSpaceOrthonormal(int DIMS) {
  DistanceMeasure measure = new MinkowskiDistanceMeasure(2.5);
//    DistanceMeasure measure = new EuclideanDistanceMeasure();
    return new SimplexSpace<Long>(new OrthonormalHasher(DIMS, SIZE), DIMS, measure, false, false);
  }
  
  private static SimplexSpace<Long> getSpaceVT(int DIMS) {
    DistanceMeasure measure = new MinkowskiDistanceMeasure(2.5);
//    DistanceMeasure measure = new ChebyshevDistanceMeasure();
//    DistanceMeasure measure = new ManhattanDistanceMeasure();
//    DistanceMeasure measure = new EuclideanDistanceMeasure();
    return new SimplexSpace<Long>(new VertexTransitiveHasher(DIMS, SIZE), DIMS, measure, false, false);
  }
  
  private static void addSimplices(SimplexSpace<Long> space, DataModel bcModel) throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions(), new Random(0));
    LongPrimitiveIterator lpi = bcModel.getUserIDs();
    while (lpi.hasNext()) {
      Long userID = lpi.nextLong();
      Vector sv = svf.getUserVector(userID, 3, 50);
      if (null != sv) {
        space.addVector(sv, userID);
      }
    }
  }
  
}