/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import lsh.hadoop.LSHDriver;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommender;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * @author lance
 * 
 * Create CSV file of item,nprefs,averagepref
 * Walk all items against all users
 */
public class RateAllItems {

	/**
	 * @param args
	 * @throws IOException
	 * @throws TasteException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws IOException, TasteException,
	InstantiationException, IllegalAccessException,
	ClassNotFoundException {
		DataModel glModel = new GroupLensDataModel(new File(args[0]));
		Recommender recco;
//		 recco = doReccoGL(glModel);
//		recco = doReccoKNN_LL_NegQO(glModel);
//		recco = doReccoGLSimplex(args);
		recco = doReccoPearsonItem(glModel);

		printAllRecommendations(recco);
	}

	private static Recommender doReccoPearsonItem(DataModel glModel)
			throws TasteException {
		Recommender recco;
		ItemSimilarity similarity = new PearsonCorrelationSimilarity(glModel);
		recco = new GenericItemBasedRecommender(glModel, similarity);
		return recco;
	}

	private static Recommender doReccoGLSimplex(String[] args)
			throws TasteException {
		Recommender recco;
		GLSimplexRecommenderBuilder.init(args[1]);
		GLSimplexRecommenderBuilder recb = new GLSimplexRecommenderBuilder();
		recco = recb.buildRecommender(null);
		return recco;
	}

	private static Recommender doReccoKNN_LL_NegQO(DataModel glModel) {
		Recommender recco;
		ItemSimilarity similarity = new LogLikelihoodSimilarity(glModel);
		Optimizer optimizer = new NonNegativeQuadraticOptimizer();
		 recco = new KnnItemBasedRecommender(glModel, similarity, optimizer, 6040);
		 return recco;
	}

	private static GroupLensRecommender doReccoGL(DataModel glModel)
			throws TasteException {
		return new GroupLensRecommender(glModel);
	}

	private static void printAllRecommendations(Recommender recco)
	throws TasteException {
		System.out.println("item,count,stddev,mean");
		DataModel model = recco.getDataModel();
		LongPrimitiveIterator items = model.getItemIDs();
		while (items.hasNext()) {
			StandardDeviation stddev = new StandardDeviation();
			long itemID = items.nextLong();
			double sum = 0;
			int count = 0;
			LongPrimitiveIterator users = model.getUserIDs();
			while (users.hasNext()) {
				long userID = users.nextLong();

				float pref = recco.estimatePreference(userID, itemID);
				if (pref != Float.NaN) {
					sum += pref;
					stddev.increment((double) pref);
					count++;
				}
			}
			String rating = (sum > 0 && count > 0) ? Double.toString(sum / count) : "0";
			String stdstr = (stddev.getResult()) > 0 ? Double.toString(stddev.getResult()) : "0";
			System.out.println(itemID + "," + count + "," + stdstr + "," + rating);
		}
	}

}


