package hack;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import lsh.core.OrthonormalHasher;
import lsh.core.VertexTransitiveHasher;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.PreferenceBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Mode;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.SimplexSimilarity;
import org.apache.mahout.cf.taste.neighborhood.SimplexSpace;
import org.apache.mahout.cf.taste.neighborhood.SimplexUserNeighborhood;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.ChebyshevDistanceMeasure;
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
  static SimplexUserNeighborhood sun = null;
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
    return space.getNonSingleHashes();
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