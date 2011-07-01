package org.apache.mahout.cf.taste.impl.eval;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.Hamming;
import org.apache.mahout.cf.taste.impl.eval.MeanRank;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Distribution;
import org.apache.mahout.cf.taste.impl.recommender.knn.ConjugateGradientOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;
//import org.apache.mahout.semanticvectors.SemanticVectorFactory;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/*
 * Compare preferences returned by Recommenders and DataModels.
 * 
 * Usage: TestPreferenceBasedRecommender /path/of/GL/ratings.dat   <-- compare different recommenders
 *                            training.dat test.dat     <-- compare training and test data
 */

public class RegressOrderBasedRecommender2 {
  static final int SAMPLES = 50;
  
  public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (args.length == 2)
      compareTrainingTest(args);
    else
      compareSubsamples(args);
  }
  
  private static void compareTrainingTest(String[] args) throws IOException,
  TasteException {
    GroupLensDataModel glModelTraining = new GroupLensDataModel(new File(args[0])); 
    GroupLensDataModel glModelTest = new GroupLensDataModel(new File(args[1])); 
    OrderBasedRecommenderEvaluator obre = new MeanRank();
    Recommender trainingRecco = doSlope1Recco(glModelTraining);
    Recommender testRecco = doSlope1Recco(glModelTest);
    double delta = obre.evaluate(trainingRecco, testRecco);
    System.err.println("Training v.s Test score: " + delta);
  }
  
  private static void compareSubsamples(String[] args) throws IOException, TasteException {
    GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); 
    DataModel glModelTraining = new SamplingDataModel(glModel, 0.0, 0.8, Distribution.USER); 
    DataModel glModelTest = new SamplingDataModel(glModel, 0.7, 1.0, Distribution.USER); 
    OrderBasedRecommenderEvaluator obre = new MeanRank();
    printSizes("Training: ", glModelTraining);
    printSizes("Test: ", glModelTest);
    
    //    Recommender trainingRecco = doEstimatingSimplexUser(glModelTraining);
    //    Recommender testRecco = doEstimatingSimplexUser(glModelTest);
    Recommender trainingRecco = doSlope1Recco(glModelTraining);
    Recommender testRecco = doSlope1Recco(glModelTest);
    double delta = obre.evaluate(trainingRecco, testRecco);
    System.err.println("Training v.s Test score: " + delta);
  }
  
  private static void printSizes(String string, DataModel glModelTraining) {
    System.out.println(string);
//    iter 
    
  }

  private static Recommender buildKNNRecommender(DataModel dataModel) throws TasteException {
    ItemSimilarity similarity = new EuclideanDistanceSimilarity(dataModel);
    Optimizer optimizer = new ConjugateGradientOptimizer();
    return new KnnItemBasedRecommender(dataModel, similarity, optimizer, 5);
  }
  
  static long seed = 0;
  private static DataModel sampleModel(GroupLensDataModel glModel, double lower,
      double upper) throws TasteException {
    //    boolean first = users.size() == 0;
    Random rnd = new Random(seed);
    seed = rnd.nextLong();
    
    FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>(glModel.getNumUsers());
    LongPrimitiveIterator iter = glModel.getUserIDs();
    while(iter.hasNext()) {
      long userID = iter.nextLong();
      double r = rnd.nextDouble();
      if (r > lower && r <= upper) {
        //        if (first || users.contains(userID)) {
        PreferenceArray prefs = glModel.getPreferencesFromUser(userID);
        userData.put(userID, prefs);
        //          if (first)
        //            users.add(userID); 
      }
    }
    
    DataModel stripped = new GenericDataModel(userData);
    return stripped;
  }
  
//  private static DataModel sampleModel(GroupLensDataModel glModel, double lower,
//      double upper) throws TasteException {
//    //    boolean first = users.size() == 0;
//    Random rnd = new Random(seed);
//    seed = rnd.nextLong();
//    
//    FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>(glModel.getNumUsers());
//    LongPrimitiveIterator iter = glModel.getUserIDs();
//    while(iter.hasNext()) {
//      long userID = iter.nextLong();
//      double r = rnd.nextDouble();
//      if (r > lower && r <= upper) {
//        //        if (first || users.contains(userID)) {
//        PreferenceArray prefs = glModel.getPreferencesFromUser(userID);
//        userData.put(userID, prefs);
//        //          if (first)
//        //            users.add(userID); 
//      }
//    }
//    
//    DataModel stripped = new GenericDataModel(userData);
//    return stripped;
//  }

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
  
  //  private static VectorDataModel doGenericSemanticV(DataModel dataModel) throws TasteException {
  //    int dimensions = 2;
  //    SemanticVectorFactory svf = new SemanticVectorFactory(dataModel, dimensions);
  //    DistanceMeasure measure = new EuclideanDistanceMeasure();
  //    Map<Long,Vector> itemVecs = new HashMap<Long,Vector>();
  //    VectorDataModel vdm = new VectorDataModel(dimensions, measure);
  //    
  //    int minimum = 5;
  //    LongPrimitiveIterator itemIDs = dataModel.getItemIDs();
  //    while (itemIDs.hasNext()) {
  //      Long itemID = itemIDs.next();
  //      Vector itemV = svf.projectItemDense(itemID, minimum);
  //      itemVecs.put(itemID, itemV);
  //    }
  //    
  //    LongPrimitiveIterator userIDs = dataModel.getUserIDs();
  //    while (userIDs.hasNext()) {
  //      Long userID = userIDs.next();
  //      Vector userV = svf.getRandomUserVector(userID);
  //      PreferenceArray pa = dataModel.getPreferencesFromUser(userID);
  //      Iterator<Preference> prefiter = pa.iterator();
  //      while(prefiter.hasNext()) {
  //        Preference pref = prefiter.next();
  //        long itemID = pref.getItemID();
  //        Vector itemV = itemVecs.get(itemID);
  //        vdm.addUser(userID, userV);  
  //        vdm.addItem(itemID, itemV);  
  //      }
  //    }
  //    
  //    return vdm;
  //  }
  //  
  //  private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
  //    //    UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
  //    UserSimilarity similarity = new EuclideanDistanceSimilarity(bcModel);
  //    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
  //    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
  //  }
  //  
//    private static Recommender doPearsonItemRecco(DataModel model)
//    throws TasteException {
//      Recommender recco;
//      ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
//      recco = new EstimatingItemBasedRecommender(model, similarity);
//      return recco;
//    }
  //  
  private static Recommender doSlope1Recco(DataModel model)
  throws TasteException {
    return new SlopeOneRecommender(model);
  }
  // This is really slow, but try it if you like
  //  private static Recommender doKNN_LL_NegQO_Recco(DataModel model) {
  //    Recommender recco;
  //    ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
  //    Optimizer optimizer = new NonNegativeQuadraticOptimizer();
  //    recco = new EstimatingKnnItemBasedRecommender(model, similarity, optimizer, 6040);
  //    return recco;
  //  }
  
  
}
