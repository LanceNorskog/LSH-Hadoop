package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
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

/*
 * Compare contents and order of recommendation returned by Recommenders and DataModels.
 * 
 * Usage: CompareRecommenders /path/of/GL/ratings.dat
 *                            training.dat test.dat
 */

public class CompareRecommenders {
  static final int SAMPLES = 500;

  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (args.length == 1)
      crossCompare(args);
    else
      trainingTestCompare(args);
  }

  // give two ratings.dat files, training and test
  private static void trainingTestCompare(String[] args) throws IOException, TasteException {
    GroupLensDataModel glModelTraining = new GroupLensDataModel(new File(args[0])); 
    GroupLensDataModel glModelTest = new GroupLensDataModel(new File(args[1])); 
    OrderBasedRecommenderEvaluator bsrv = new OrderBasedRecommenderEvaluator(System.out);
    RunningAverage tracker = new CompactRunningAverage();

    Recommender trainingRecco = doEstimatingUser(glModelTraining);
    Recommender testRecco = doEstimatingUser(glModelTest);
    bsrv.evaluate(testRecco, trainingRecco, SAMPLES, tracker, "estimating_pearson");
    System.err.println("Training v.s Test score: " + tracker.getAverage());
  }

  private static void crossCompare(String[] args) throws IOException,
      TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    Recommender estimatingRecco = doEstimatingUser(glModel);
    Recommender slope1Recco = doSlope1Recco(glModel);
    Recommender pearsonRecco = doPearsonItemRecco(glModel);
    Recommender knnLLRecco = doKNN_LL_NegQO_Recco(glModel);
    OrderBasedRecommenderEvaluator bsrv = new OrderBasedRecommenderEvaluator(System.out);
    RunningAverage tracker = new CompactRunningAverage();

    bsrv.evaluate(estimatingRecco, pearsonRecco, SAMPLES, tracker, "estimating_pearson");
    System.err.println("Estimating v.s pearson score: " + tracker.getAverage());
    bsrv.evaluate(slope1Recco, pearsonRecco, SAMPLES, tracker, "slope1_pearson");
    System.err.println("Slope1 v.s. Pearson score: " + tracker.getAverage());
    bsrv.evaluate(slope1Recco, estimatingRecco, SAMPLES, tracker, "slope1_estimating");
    System.err.println("Slope1 v.s. Estimating score: " + tracker.getAverage());
    
    // this is really slow.
    bsrv.evaluate(slope1Recco, knnLLRecco, SAMPLES, tracker, "slope1_knn_ll");
    System.err.println("Slope1 v.s. KNN Log Likelihood score: " + tracker.getAverage());
  }

  private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  }

  // This is really slow, but try it if you like
  private static Recommender doKNN_LL_NegQO_Recco(DataModel model) {
    Recommender recco;
    ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
    Optimizer optimizer = new NonNegativeQuadraticOptimizer();
    recco = new EstimatingKnnItemBasedRecommender(model, similarity, optimizer, 6040);
    return recco;
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

}
