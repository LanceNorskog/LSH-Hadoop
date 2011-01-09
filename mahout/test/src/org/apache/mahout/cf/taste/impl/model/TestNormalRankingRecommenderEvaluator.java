package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Properties;

import lsh.hadoop.LSHDriver;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
import org.apache.mahout.cf.taste.impl.model.SimplexRecommender;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import working.OrderBasedRecommenderEvaluator;
import working.OrderBasedRecommenderEvaluator.Formula;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class TestNormalRankingRecommenderEvaluator {
  float minPreference, maxPreference;
  boolean doCSV = false;

  /**
   * @param args
   * @throws TasteException 
   * @throws IOException 
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   */
  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    Recommender estimatingRecco = doEstimatingUser(glModel);
//    Recommender slope1Recco = doReccoSlope1(glModel);
//    Recommender pearsonRecco = doReccoPearsonItem(glModel);
//    Recommender reccoKNN = doReccoKNN_LL_NegQO(glModel);
//    Recommender pointRecco = doPointText(args[1]);
//    DataModel pointModel = pointRecco.getDataModel();
//    PointTextDataModel pointModelTraining = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_7k/part-r-00000");
//    PointTextDataModel pointModelTest = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_3k/part-r-00000");
//    PointTextDataModel pointModel = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_10k/part-r-00000");
    PointTextDataModel pointModelBig = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_10k_500d/part-r-00000");

    int samples = 500;
    Formula formula = Formula.MEANRANK;
    OrderBasedRecommenderEvaluator bsrv = new OrderBasedRecommenderEvaluator();
    RunningAverage tracker = new CompactRunningAverage();

    //		WANT: each recommends top N out of all. Find common items. COunt up distances between items. sqrt.
    //		add factor for number of singulars. This is score.
    //		Add bubblesort? needs same set.

//    bsrv.evaluate(pointRecco, pointModel, samples, tracker, "point_point");
//    System.err.println("Point score: " + tracker.getAverage());
//    bsrv.evaluate(pointModel, pointModel, samples, tracker, "training_point");
//    System.err.println("Point/self score: " + tracker.getAverage());
//    bsrv.evaluate(pointModelTraining, pointModelTest, samples, tracker, "training_test");
//    System.err.println("training v.s. test score: " + tracker.getAverage());
//    System.out.flush();
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(estimatingRecco, pointModel, samples, tracker, "estimating_point");
//    System.err.println("Estimating v.s. point model core: " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(25);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(50);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(75);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(100);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(150);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    tracker = new CompactRunningAverage();
    pointModelBig.setDimensions(200);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(250);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(300);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(350);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(400);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(450);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
    pointModelBig.setDimensions(500);
    bsrv.evaluate(estimatingRecco, pointModelBig, samples, formula, tracker, "estimating_point200");
    System.err.println("Estimating v.s. point model big score (" + pointModelBig.getDimensions() + "): " + tracker.getAverage());
   
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(pointModel, pointModel200, samples, formula, tracker, "point150_point200");
//    System.err.println("point150 v.s. point200 score: " + tracker.getAverage());

//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(estimatingRecco, pearsonRecco, samples, formula, tracker, "estimating_pearson");
//    System.err.println("Estimating v.s pearson score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(estimatingRecco, pointModelTraining, samples, formula, tracker, "estimating_training");
//    System.err.println("Estimating v.s. training score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, pointModel, samples, formula, tracker, "slope1_point");
//    System.err.println("Slope1 v.s. point model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, pointModelTraining, samples, formula, tracker, "slope1_training");
//    System.err.println("Slope1 v.s. point training model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(pearsonRecco, pointModel, samples, tracker, "pearson_point");
//    System.err.println("Pearson1 v.s. point model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(pearsonRecco, pointModelTraining, samples, tracker, "pearson_training");
//    System.err.println("Pearson1 v.s. point training model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(reccoKNN, pointModel, samples, tracker, "knn_point");
//    System.err.println("KNN v.s. point model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(reccoKNN, pointModelTraining, samples, tracker, "knn_training");
//    System.err.println("KNN v.s. point training model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, pearsonRecco, samples, tracker, "slope1_pearson");
//    System.err.println("Slope1 v.s. Pearson score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(slope1Recco, estimatingRecco, samples, tracker, "slope1_estimating");
//    System.err.println("Slope1 v.s. Estimating score: " + tracker.getAverage());
//    System.exit(0);
//    tracker = new CompactRunningAverage();
//    Recommender simplexRecco = doSimplexDataModel(args[2]);
//    DataModel simplexModel = simplexRecco.getDataModel();
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(simplexRecco, pointModel, samples, tracker, "simplex_point");
//    System.err.println("Simplex v.s. point model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(simplexRecco, simplexModel, samples, tracker, "simplex_simplex");
//    System.err.println("Simplex v.s. simplex model score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(simplexRecco, slope1Recco, samples, tracker, "simplex_slope1");
//    System.err.println("Simplex v.s. Slope1 score: " + tracker.getAverage());
//    tracker = new CompactRunningAverage();
//    bsrv.evaluate(simplexRecco, pearsonRecco, samples, tracker, "simplex_pearson");
//    System.err.println("Simplex v.s. Pearson score: " + tracker.getAverage());
  }

   static PointTextDataModel doPointTextDataModel(String pointsFile) throws IOException {
    PointTextDataModel model = new PointTextDataModel(pointsFile);
    return model;
  }
//
//  private static Recommender doPointText(String pointsFile) throws IOException {
//    Recommender prec;
//    DataModel model = new PointTextDataModel(pointsFile);
//    prec = new (model);
//    return prec;
//  }

  private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 1.0);
    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);

  }

  private static Recommender doReccoKNN_LL_NegQO(DataModel glModel) {
    Recommender recco;
    ItemSimilarity similarity = new LogLikelihoodSimilarity(glModel);
    Optimizer optimizer = new NonNegativeQuadraticOptimizer();
    recco = new EstimatingKnnItemBasedRecommender(glModel, similarity, optimizer, 6040);
    return recco;
  }

  private static Recommender doReccoPearsonItem(DataModel glModel)
  throws TasteException {
    Recommender recco;
    ItemSimilarity similarity = new PearsonCorrelationSimilarity(glModel);
    recco = new EstimatingItemBasedRecommender(glModel, similarity);
    return recco;
  }

  private static Recommender doReccoSlope1(DataModel glModel)
  throws TasteException {
    return new EstimatingSlopeOneRecommender(glModel);
  }

  private static SimplexRecommender doSimplexDataModel(String cornersfile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    Properties props = new Properties();
    props.setProperty(LSHDriver.HASHER, "lsh.core.VertexTransitiveHasher");
    props.setProperty(LSHDriver.DIMENSION, "150");
    props.setProperty(LSHDriver.GRIDSIZE, "0.0001");
    SimplexRecommender rec = new SimplexRecommender(props, cornersfile);
    return rec;
  }

}
