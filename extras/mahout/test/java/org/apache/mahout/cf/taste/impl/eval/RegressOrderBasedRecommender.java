package org.apache.mahout.cf.taste.impl.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.PreferenceBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel;
import org.apache.mahout.cf.taste.impl.model.VectorDataModel;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Distribution;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
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
import org.apache.mahout.semanticvectors.SemanticVectorFactory;


import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula;

import static org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula.*;

/*
 * Compare preferences returned by Recommenders and DataModels.
 * 
 * Usage: TestPreferenceBasedRecommender /path/of/GL/ratings.dat   <-- compare different recommenders
 *                            training.dat test.dat     <-- compare training and test data
 */

public class RegressOrderBasedRecommender {
  static final int SAMPLES = 50;
  
  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (args.length == 2)
      crossCompare(args);
    else
      trainingTestCompare(args);
  }
  
  // give two ratings.dat files, training and test
  private static void trainingTestCompare(String[] args) throws IOException, TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    DataModel glModelTraining = new SamplingDataModel(glModel, 0.0, 0.8, Distribution.HOLOGRAPHIC); 
    DataModel glModelTest = new SamplingDataModel(glModel, 0.6, 1.0, Distribution.HOLOGRAPHIC); 
    RecommenderEvaluator obre = new OrderBasedRecommenderEvaluator();
    RunningAverage tracker = new FullRunningAverage();
    
    //    Recommender trainingRecco = doEstimatingSimplexUser(glModelTraining);
    //    Recommender testRecco = doEstimatingSimplexUser(glModelTest);
    Recommender trainingRecco = doEstimatingUser(glModelTraining);
    Recommender testRecco = doEstimatingUser(glModelTest);
    obre.evaluate(trainingRecco, testRecco, SAMPLES, tracker, MEANRANK);
    System.err.println("Training v.s Test score: " + tracker.getAverage());
  }
  
  private static void crossCompare(String[] args) throws IOException,
  TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    VectorDataModel vdModel = doGenericSemanticV(glModel);
    Recommender estimatingRecco = doEstimatingUser(glModel);
    //    Recommender slope1Recco = doSlope1Recco(glModel);
    Recommender pearsonRecco = doPearsonItemRecco(glModel);
    Recommender simplexRecco = doPearsonItemRecco(glModel);
    RecommenderEvaluator obre = new OrderBasedRecommenderEvaluator();
    RunningAverage tracker = null;
    
    tracker = new FullRunningAverage();
    Formula formula = MEANRANK;
    //    bsrv.evaluate(estimatingRecco, simplexRecco, SAMPLES, tracker, formula);
    //    System.err.println("Estimating v.s. Simplex score: " + tracker.getAverage());
    //    System.out.println("Total hashes, subtracted hashes: " + sun.total + "," + sun.subtracted);
    tracker = new FullRunningAverage();
    obre.evaluate(estimatingRecco, pearsonRecco, SAMPLES, tracker, formula);
    System.err.println("Estimating v.s. Pearson score: " + tracker.getAverage());
    //    tracker = new FullRunningAverage();
    //    bsrv.evaluate(slope1Recco, pearsonRecco, SAMPLES, tracker, formula);
    //    System.err.println("Slope1 v.s. Pearson score: " + tracker.getAverage());
    //    tracker = new FullRunningAverage();
    //    bsrv.evaluate(slope1Recco, estimatingRecco, SAMPLES, tracker, formula);
    //    System.err.println("Slope1 v.s. Estimating score: " + tracker.getAverage());
    
    // this is really slow.
    //    Recommender knnLLRecco = doKNN_LL_NegQO_Recco(glModel);
    //    tracker = new FullRunningAverage();
    //    bsrv.evaluate(slope1Recco, knnLLRecco, SAMPLES, tracker, formula);
    //    System.err.println("Slope1 v.s. KNN Log Likelihood score: " + tracker.getAverage());
  }
  
  /*
   * Recommender generators
   * These are all from examples in the web site and the book. Given that none of them 
   * generate similar recommendations, I'd say they are suspect.
   */
  
  //  private static Recommender doEstimatingSimplexUser(DataModel bcModel) throws TasteException {
  //    int DIMS = 100;
  //    //    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
  //    SimplexSpace<Long> userSpace = getSpace(DIMS);
  //    SimplexSpace<Long> userSpaceLOD = getSpace(DIMS);
  //    //    userSpace.doUnhash = false;
  //    //    userSpaceLOD.doUnhash = false;
  //    userSpaceLOD.setLOD(3);
  //    addUserSimplices(userSpace, userSpaceLOD, bcModel);
  //    SimplexSpace<Long> itemSpace = getSpace(DIMS);
  //    //    itemSpace.doUnhash = false;
  //    addItemSimplices(itemSpace, bcModel);
  //    UserSimilarity similarity = new CachingUserSimilarity(new SimplexSimilarity(userSpace, itemSpace, null), bcModel);
  //    UserNeighborhood neighborhood = new SimplexUserNeighborhood(userSpace, userSpaceLOD);
  //    sun = (SimplexUserNeighborhood) neighborhood;
  //    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  //  }
  //  
  //  private static SimplexSpace<Long> getSpace(int DIMS) {
  //    //    DistanceMeasure measure = new ChebyshevDistanceMeasure(); 
  //    //    DistanceMeasure measure = new ManhattanDistanceMeasure(); 
  //    //    DistanceMeasure measure = new MinkowskiDistanceMeasure(2.5); 
  //    DistanceMeasure measure = new EuclideanDistanceMeasure();
  //    return new SimplexSpace<Long>(new OrthonormalHasher(DIMS, 0.05), DIMS, measure, false, false);
  //    //    return new SimplexSpace(new VertexTransitiveHasher(DIMS, 0.2), DIMS, measure);
  //    /*
  //     * LOD 8
  //     * mink 1.5
  //     * ortho 0.1
  //     * matches 90
  //     * 0.89
  //     */
  //    /*
  //     * LOD 8
  //     * mink 1.5
  //     * vertex 0.2
  //     * matches 59
  //     * score 0.69
  //     */
  //  }
  //  
  //  private static void addUserSimplices(SimplexSpace<Long> space, SimplexSpace<Long> spaceLOD, DataModel bcModel) throws TasteException {
  //    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions(), new Random(0));
  //    LongPrimitiveIterator lpi = bcModel.getUserIDs();
  //    while (lpi.hasNext()) {
  //      Long userID = lpi.nextLong();
  //      Vector sv = svf.getUserVector(userID, 3, 50);
  //      if (null != sv) {
  //        space.addVector(sv, userID);
  //        if (null != spaceLOD)
  //          spaceLOD.addVector(sv, userID);
  //      }
  //    }
  //  }
  //  
  //  private static void addItemSimplices(SimplexSpace<Long> space, DataModel bcModel) throws TasteException {
  //    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions(), new Random(0));
  //    LongPrimitiveIterator lpi = bcModel.getItemIDs();
  //    while (lpi.hasNext()) {
  //      Long itemID = lpi.nextLong();
  //      Vector sv = svf.getItemVector(itemID, 3, 50);
  //      if (null != sv)
  //        space.addVector(sv, itemID);
  //    }
  //  }
  
  private static VectorDataModel doGenericSemanticV(DataModel dataModel) throws TasteException {
    int dimensions = 2;
    SemanticVectorFactory svf = new SemanticVectorFactory(dataModel, dimensions);
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    Map<Long,Vector> itemVecs = new HashMap<Long,Vector>();
    VectorDataModel vdm = new VectorDataModel(dimensions, measure);
    
    int minimum = 5;
    LongPrimitiveIterator itemIDs = dataModel.getItemIDs();
    while (itemIDs.hasNext()) {
      Long itemID = itemIDs.next();
      Vector itemV = svf.projectItemDense(itemID, minimum);
      itemVecs.put(itemID, itemV);
    }
    
    LongPrimitiveIterator userIDs = dataModel.getUserIDs();
    while (userIDs.hasNext()) {
      Long userID = userIDs.next();
      Vector userV = svf.getRandomUserVector(userID);
      PreferenceArray pa = dataModel.getPreferencesFromUser(userID);
      Iterator<Preference> prefiter = pa.iterator();
      while(prefiter.hasNext()) {
        Preference pref = prefiter.next();
        long itemID = pref.getItemID();
        Vector itemV = itemVecs.get(itemID);
        vdm.addUser(userID, userV);  
        vdm.addItem(itemID, itemV);  
      }
    }
    
    return vdm;
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
