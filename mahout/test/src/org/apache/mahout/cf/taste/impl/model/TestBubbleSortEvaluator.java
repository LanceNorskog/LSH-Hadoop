package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lsh.hadoop.LSHDriver;

//import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.eval.BubbleSortRecommenderEvaulator;
import org.apache.mahout.cf.taste.impl.eval.EstimatingItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingKnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingSlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.eval.EstimatingUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
import org.apache.mahout.cf.taste.impl.model.PointTextRecommender;
import org.apache.mahout.cf.taste.impl.model.SimplexRecommender;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class TestBubbleSortEvaluator {
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
		Recommender recco;
		BubbleSortRecommenderEvaulator bsrv = new BubbleSortRecommenderEvaulator();

		double score ;
		recco = doEstimatingUser(glModel);
//		bsrv.doCSV = true;
		score = bsrv.evaluate(recco, glModel);
		System.out.println("Estimating score: " + score);
		Recommender	prec = doPointText(args[1]);
		score = bsrv.evaluate(prec, glModel);
		System.out.println("Point score: " + score);
		Recommender	pearsonrec = doReccoPearsonItem(glModel);
		score = bsrv.evaluate(pearsonrec, glModel);
		System.out.println("Pearson score: " + score);
		Recommender	slope1rec = doReccoSlope1(glModel);
		score = bsrv.evaluate(slope1rec, glModel);
		System.out.println("Slope1 score: " + score);
//		Recommender srec = doSimplexDataModel(args[2]);
//		score = bsrv.evaluate(srec, glModel);
//		System.out.println("Simplex score: " + score);
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
		props.setProperty(LSHDriver.DIMENSION, "100");
		props.setProperty(LSHDriver.GRIDSIZE, "0.54");
		SimplexRecommender rec = new SimplexRecommender(props, cornersfile);
		return rec;
	}


}

class Check implements Comparator<Preference> {

	@Override
	public int compare(Preference p1, Preference p2) {
		if (p1.getValue() > p2.getValue())
			return 1;
		else if (p1.getValue() < p2.getValue())
			return -1;
		else 
			return 0;
	}
	
}