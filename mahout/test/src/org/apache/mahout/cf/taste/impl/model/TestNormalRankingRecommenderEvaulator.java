package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import lsh.hadoop.LSHDriver;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.NormalRankingRecommenderEvaulator;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaulator;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
import org.apache.mahout.cf.taste.impl.model.PointTextRecommender;
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
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class TestNormalRankingRecommenderEvaulator {
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
//		GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0])); doesn't sort by prefs!
//		Recommender pointRecco = doPointText(args[1]);
//        DataModel pointModel = pointRecco.getDataModel();
        DataModel pointModelTraining = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_7k/part-r-00000");
        DataModel pointModelTest = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_3k/part-r-00000");
//        DataModel pointModelBoth = doPointTextDataModel("/tmp/lsh_hadoop/GL_points_10k/part-r-00000");

		Random random = new Random(0);
		int samples = 50;
		OrderBasedRecommenderEvaulator bsrv = new OrderBasedRecommenderEvaulator();
		bsrv.csvOut = System.out;
		
//		WANT: each recommends top N out of all. Find common items. COunt up distances between items. sqrt.
//		add factor for number of singulars. This is score.
//		Add bubblesort? needs same set.

		double score ;
//        random.setSeed(0);
//        score = bsrv.evaluate(pointRecco, pointModel, random, samples, "point_point");
//        System.err.println("Point score: " + score);
//        random.setSeed(0);
//        score = bsrv.evaluate(pointModel, pointModel, random, samples, "training_point");
//        System.err.println("Point/self score: " + score);
		random.setSeed(0);
		score = bsrv.evaluate(pointModelTraining, pointModelTest, random, samples, "training_test");
		System.err.println("training v.s. test score: " + score);
//		Recommender estimatingRecco = doEstimatingUser(glModel);
//		random.setSeed(0);
//		score = bsrv.evaluate(estimatingRecco, pointModel, random, samples, "estimating_point");
//		System.err.println("Estimating score: " + score);
//		random.setSeed(0);
//		score = bsrv.evaluate(estimatingRecco, pointModelTraining, random, samples, "estimating_training");
//		System.err.println("Estimating training score: " + score);
//		
//		Recommender slope1Recco = doReccoSlope1(glModel);
//		random.setSeed(0);
//		score = bsrv.evaluate(slope1Recco, pointModel, random, samples, "slope1_point");
//		System.err.println("Slope1 v.s. point model score: " + score);
//        random.setSeed(0);
//        score = bsrv.evaluate(slope1Recco, pointModelTraining, random, samples, "slope1_training");
//        System.err.println("Slope1 v.s. point training model score: " + score);
//        Recommender pearsonRecco = doReccoPearsonItem(glModel);
//        random.setSeed(0);
//        score = bsrv.evaluate(pearsonRecco, pointModel, random, samples, "pearson_point");
//        System.err.println("Pearson1 v.s. point model score: " + score);
//        random.setSeed(0);
//        score = bsrv.evaluate(pearsonRecco, pointModelTraining, random, samples, "pearson_training");
//        System.err.println("Pearson1 v.s. point training model score: " + score);
//		bsrv.doCSV = true;
//        Recommender reccoKNN = doReccoKNN_LL_NegQO(glModel);
//        random.setSeed(0);
//        score = bsrv.evaluate(reccoKNN, pointModel, random, samples, "knn_point");
//        System.err.println("KNN v.s. point model score: " + score);
//        random.setSeed(0);
//        score = bsrv.evaluate(reccoKNN, pointModelTraining, random, samples, "knn_training");
//        System.err.println("KNN v.s. point training model score: " + score);
//		score = bsrv.evaluate(slope1Recco, pearsonRecco, random, samples, "slope1_pearson");
//		System.err.println("Slope1 v.s. Pearson score: " + score);
//		needs namedvectors
//		Recommender simplexRecco = doSimplexDataModel(args[2]);
//		DataModel simplexModel = simplexRecco.getDataModel();
//		score = bsrv.evaluate(simplexRecco, pointModel, random, samples, "simplex_point");
//		System.err.println("Simplex v.s. point model score: " + score);
//		score = bsrv.evaluate(simplexRecco, simplexModel, random, samples);
//		System.err.println("Simplex v.s. simplex model score: " + score);
//		score = bsrv.evaluate(simplexRecco, slope1Recco, random, samples);
//		System.err.println("Simplex v.s. Slope1 score: " + score);
//		score = bsrv.evaluate(pearsonRecco, simplexRecco, random, samples);

//		System.err.println("Simplex v.s. Pearson score: " + score);
	}

	private static PointTextDataModel doPointTextDataModel(String pointsFile) throws IOException {
		PointTextDataModel model = new PointTextDataModel(pointsFile);
		return model;
	}

	private static Recommender doPointText(String pointsFile) throws IOException {
		Recommender prec;
		DataModel model = new PointTextDataModel(pointsFile);
		prec = new PointTextRecommender(model);
		return prec;
	}

	private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
		UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
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

/*
 * Sort preference by preference, and then by item ID
 * Simplex ranking spits out a lot of identical ratings, 
 * so this gets better order comparison
 */
class PrefCheck implements Comparator<Preference> {
	final int sign;
	double epsilon = 0.0001;
	
	PrefCheck(int sign) {
		this.sign = sign;
	}

	@Override
	public int compare(Preference p1, Preference p2) {
		double v1 = p1.getValue() * sign;
		if (p1.getValue()*sign > ((p2.getValue() - epsilon)*sign))
			return 1;
		else if (p1.getValue()*sign < (p2.getValue() + epsilon)*sign)
			return -1;
		else {
			// break ties by item id
			if (p1.getItemID() > p2.getItemID())
				return 1;
			else if (p1.getItemID() < p2.getItemID())
				return -1;
			else
				return 0;
		}
	}

}
