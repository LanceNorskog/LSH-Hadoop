package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import lsh.core.OrthonormalHasher;
import lsh.core.VertexTransitiveHasher;
import lsh.mahout.core.SimplexSimilarity;
import lsh.mahout.core.SimplexSpace;
import lsh.mahout.core.SimplexUserNeighborhood;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.PreferenceBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Distribution;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.ManhattanDistanceMeasure;
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

public class TestPreferenceBasedRecommender {
  static final int SAMPLES = 50;
  static SimplexUserNeighborhood sun = null;

  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    //    if (args.length == 1)
    //      crossCompare(args);
    //    else
    trainingTestCompare(args);
  }

  // give two ratings.dat files, training and test
  private static void trainingTestCompare(String[] args) throws IOException, TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    DataModel glModelTraining = new SamplingDataModel(glModel, 0.0, 0.7, Distribution.USER); 
    DataModel glModelTest = new SamplingDataModel(glModel, 0.7, 1.0, Distribution.USER); 
    RecommenderEvaluator pbre = new PreferenceBasedRecommenderEvaluator();
    RunningAverage tracker = new CompactRunningAverage();

    //    Recommender trainingRecco = doEstimatingSimplexUser(glModelTraining);
    //    Recommender testRecco = doEstimatingSimplexUser(glModelTest);
    Recommender trainingRecco = doEstimatingUser(glModelTraining);
    Recommender testRecco = doEstimatingUser(glModelTest);
    pbre.evaluate(trainingRecco, testRecco, SAMPLES, tracker, MEANRANK);
    System.err.println("Training v.s Test score: " + tracker.getAverage());
  }

  private static void crossCompare(String[] args) throws IOException,
  TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    Recommender estimatingRecco = doEstimatingUser(glModel);
    //    Recommender slope1Recco = doSlope1Recco(glModel);
    //    Recommender pearsonRecco = doPearsonItemRecco(glModel);
    Recommender simplexRecco = doEstimatingSimplexUser(glModel);
    RecommenderEvaluator bsrv = new OrderBasedRecommenderEvaluator();
    RunningAverage tracker = null;

    tracker = new CompactRunningAverage();
    Formula formula = MEANRANK;
    bsrv.evaluate(estimatingRecco, simplexRecco, SAMPLES, tracker, formula);
    System.err.println("Estimating v.s. Simplex score: " + tracker.getAverage());
    System.out.println("Total hashes, subtracted hashes: " + sun.total + "," + sun.subtracted);
    System.out.println("Small space");
    sun.space.stDevCounts();
    if (null != sun.spaceLOD) {
      System.out.println("LOD space");
      sun.spaceLOD.stDevCounts();
    }
    tracker = new CompactRunningAverage();
//    bsrv.evaluate(estimatingRecco, pearsonRecco, SAMPLES, tracker, formula);
//    System.err.println("Estimating v.s. Pearson score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, pearsonRecco, SAMPLES, tracker, formula);
//    System.err.println("Slope1 v.s. Pearson score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, estimatingRecco, SAMPLES, tracker, formula);
//    System.err.println("Slope1 v.s. Estimating score: " + tracker.getAverage());

    // this is really slow.
//    Recommender knnLLRecco = doKNN_LL_NegQO_Recco(glModel);
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, knnLLRecco, SAMPLES, tracker, formula);
//    System.err.println("Slope1 v.s. KNN Log Likelihood score: " + tracker.getAverage());
  }

  /*
   * Recommender generators
   * These are all from examples in the web site and the book. Given that none of them 
   * generate similar recommendations, I'd say they are suspect.
   */

  private static Recommender doEstimatingSimplexUser(DataModel bcModel) throws TasteException {
    int DIMS = 100;
    //    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
    SimplexSpace<Long> userSpace = getSpace(DIMS);
    SimplexSpace<Long> userSpaceLOD = getSpace(DIMS);
    //    userSpace.doUnhash = false;
    //    userSpaceLOD.doUnhash = false;
    userSpaceLOD.setLOD(3);
    addUserSimplices(userSpace, userSpaceLOD, bcModel);
    SimplexSpace<Long> itemSpace = getSpace(DIMS);
    //    itemSpace.doUnhash = false;
    addItemSimplices(itemSpace, bcModel);
    UserSimilarity similarity = new CachingUserSimilarity(new SimplexSimilarity(userSpace, itemSpace, null), bcModel);
    UserNeighborhood neighborhood = new SimplexUserNeighborhood(userSpace, userSpaceLOD);
    sun = (SimplexUserNeighborhood) neighborhood;
    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  }

  private static SimplexSpace<Long> getSpace(int DIMS) {
    //    DistanceMeasure measure = new ChebyshevDistanceMeasure(); 
    //    DistanceMeasure measure = new ManhattanDistanceMeasure(); 
    //    DistanceMeasure measure = new MinkowskiDistanceMeasure(2.5); 
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    return new SimplexSpace<Long>(new OrthonormalHasher(DIMS, 0.05), DIMS, measure, false, false);
    //    return new SimplexSpace(new VertexTransitiveHasher(DIMS, 0.2), DIMS, measure);
    /*
     * LOD 8
     * mink 1.5
     * ortho 0.1
     * matches 90
     * 0.89
     */
    /*
     * LOD 8
     * mink 1.5
     * vertex 0.2
     * matches 59
     * score 0.69
     */
  }

  private static void addUserSimplices(SimplexSpace<Long> space, SimplexSpace<Long> spaceLOD, DataModel bcModel) throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions(), new Random(0));
    LongPrimitiveIterator lpi = bcModel.getUserIDs();
    while (lpi.hasNext()) {
      Long userID = lpi.nextLong();
      Vector sv = svf.getUserVector(userID, 3, 50);
      if (null != sv) {
        space.addVector(sv, userID);
        if (null != spaceLOD)
          spaceLOD.addVector(sv, userID);
      }
    }
  }

  private static void addItemSimplices(SimplexSpace<Long> space, DataModel bcModel) throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions(), new Random(0));
    LongPrimitiveIterator lpi = bcModel.getItemIDs();
    while (lpi.hasNext()) {
      Long itemID = lpi.nextLong();
      Vector sv = svf.getItemVector(itemID, 3, 50);
      if (null != sv)
        space.addVector(sv, itemID);
    }
  }

  private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
    //    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
    UserSimilarity similarity = new EuclideanDistanceSimilarity(bcModel);
    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  }

  private static Recommender doPearsonItemRecco(DataModel model)
  throws TasteException {
    Recommender recco;
    ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
    recco = new EstimatingItemBasedRecommender(model, similarity);
    return recco;
  }

  private static Recommender doSlope1Recco(DataModel model)
  throws TasteException {
    return new EstimatingSlopeOneRecommender(model);
  }
  // This is really slow, but try it if you like
  private static Recommender doKNN_LL_NegQO_Recco(DataModel model) {
    Recommender recco;
    ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
    Optimizer optimizer = new NonNegativeQuadraticOptimizer();
    recco = new EstimatingKnnItemBasedRecommender(model, similarity, optimizer, 6040);
    return recco;
  }


}
