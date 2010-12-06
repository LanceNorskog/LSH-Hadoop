package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaulator;
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
 */

public class TestOrderBasedRecommenderEvaulator {
  float minPreference, maxPreference;
  boolean doCSV = false;

  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    Recommender estimatingRecco = doEstimatingUser(glModel);
    Recommender slope1Recco = doReccoSlope1(glModel);
    Recommender pearsonRecco = doReccoPearsonItem(glModel);

    int samples = 500;
    OrderBasedRecommenderEvaulator bsrv = new OrderBasedRecommenderEvaulator();
    bsrv.csvOut = System.out;
    double score;
    score = bsrv.evaluate(estimatingRecco, pearsonRecco, samples, "estimating_pearson");
    System.err.println("Estimating v.s pearson score: " + score);
    score = bsrv.evaluate(slope1Recco, pearsonRecco, samples, "slope1_pearson");
    System.err.println("Slope1 v.s. Pearson score: " + score);
    score = bsrv.evaluate(slope1Recco, estimatingRecco, samples, "slope1_estimating");
    System.err.println("Slope1 v.s. Estimating score: " + score);
  }

  private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  }

  // This is really slow, but try it if you like
  private static Recommender doReccoKNN_LL_NegQO(DataModel model) {
    Recommender recco;
    ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
    Optimizer optimizer = new NonNegativeQuadraticOptimizer();
    recco = new EstimatingKnnItemBasedRecommender(model, similarity, optimizer, 6040);
    return recco;
  }

  private static Recommender doReccoPearsonItem(DataModel model)
  throws TasteException {
    Recommender recco;
    ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
    recco = new EstimatingItemBasedRecommender(model, similarity);
    return recco;
  }

  private static Recommender doReccoSlope1(DataModel model)
  throws TasteException {
    return new EstimatingSlopeOneRecommender(model);
  }

}
