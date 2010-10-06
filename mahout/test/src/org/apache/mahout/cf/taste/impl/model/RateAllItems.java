/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

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
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.web.ReverseRescorer;


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
	 * 
	 * Usage: rateall ratings.dat corners points
	 */
	public static void main(String[] args) throws IOException, TasteException,
	InstantiationException, IllegalAccessException,
	ClassNotFoundException {
		DataModel glModel = new GroupLensDataModel(new File(args[0]));
		DataModel pointModel = new PointTextDataModel(args[2]);
//		Recommender recco;
//		recco = doReccoGL(pointModel);
//		recco = doReccoSlope1(pointModel);
		//		recco = doReccoKNN_LL_NegQO(glModel);
		//		recco = doReccoGLSimplex(args);
		//		recco = doReccoPearsonItem(glModel);

//		printAllRecommendations(recco);
		printAvgPrefs(pointModel);
//		printMinMaxPrefs(pointModel);
//		printMinMaxReccomendations(recco);
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
		recco = GLSimplexRecommenderBuilder.recommender;
		return recco;
	}

	private static Recommender doReccoKNN_LL_NegQO(DataModel glModel) {
		Recommender recco;
		ItemSimilarity similarity = new LogLikelihoodSimilarity(glModel);
		Optimizer optimizer = new NonNegativeQuadraticOptimizer();
		recco = new KnnItemBasedRecommender(glModel, similarity, optimizer, 6040);
		return recco;
	}

	private static Recommender doReccoGL(DataModel glModel)
	throws TasteException {
		return new GroupLensRecommender(glModel);
	}

	private static Recommender doReccoSlope1(DataModel glModel)
	throws TasteException {
		return new SlopeOneRecommender(glModel);
	}

	private static void printAllRecommendations(Recommender recco)
	throws TasteException {
		System.out.println("item,count,stddev,mean");
		DataModel model = recco.getDataModel();
		LongPrimitiveIterator items = model.getItemIDs();
		Random rnd = new Random();
		while (items.hasNext()) {
			StandardDeviation stddev = new StandardDeviation();
			long itemID = items.nextLong();
			double sum = 0;
			int count = 0;
			LongPrimitiveIterator users = model.getUserIDs();
			while (users.hasNext()) {
				long userID = users.nextLong();
				if ((rnd.nextInt() % 50) != 0)
					continue;
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
		items.hashCode();
	}

	private static void printMinMaxReccomendations(Recommender recco)
	throws TasteException {
		System.out.println("user,min,max");
		LongPrimitiveIterator users = recco.getDataModel().getUserIDs();
		while (users.hasNext()) {
			long userID = users.nextLong();
			List<RecommendedItem> ratings = recco.recommend(userID, 20000);
			float max = 0;
			int i = 0;
			for(; i < 5; i++)
				max += ratings.get(i).getValue();
			max = max/ratings.size();
			float min = 0;
			i = 1;
			for(; i <= 5; i++)
				min += ratings.get(ratings.size() - i).getValue();
			max = min/ratings.size();
			System.out.println(userID + "," + min + "," + max);
		}
		users.hashCode();
	}

	private static void printAvgPrefs(DataModel model)
	throws TasteException {
		System.out.println("item,count,stddev,mean");
		LongPrimitiveIterator items = model.getItemIDs();
		Random rnd = new Random();
		while (items.hasNext()) {
			StandardDeviation stddev = new StandardDeviation();
			long itemID = items.nextLong();
			double sum = 0;
			int count = 0;
			LongPrimitiveIterator users = model.getUserIDs();
			while (users.hasNext()) {
				long userID = users.nextLong();
//				if ((rnd.nextInt() % 20) != 0)
//					continue;

				float pref = model.getPreferenceValue(userID, itemID);
				if (pref != Float.NaN) {
					if (pref < 4.0)
						users.hashCode();
					sum += pref;
					stddev.increment((double) pref);
					count++;
				}
			}
			String rating = (sum > 0 && count > 0) ? Double.toString(sum / count) : "0";
			String stdstr = (stddev.getResult()) > 0 ? Double.toString(stddev.getResult()) : "0";
			System.out.println(itemID + "," + count + "," + stdstr + "," + rating);
		}
		PointTextDataModel x = (PointTextDataModel) model;
		System.out.println("Raw distances: " + (x.total /x.count));
		items.hashCode();
	}
	
	private static void printMinMaxPrefs(DataModel model)
	throws TasteException {
		System.out.println("item,min,max");
		LongPrimitiveIterator items = model.getItemIDs();
//		Random rnd = new Random();
		while (items.hasNext()) {
//			StandardDeviation stddev = new StandardDeviation();
			long itemID = items.nextLong();
			float min = 10;
			float max = -1;
			LongPrimitiveIterator users = model.getUserIDs();
			while (users.hasNext()) {
				long userID = users.nextLong();
//				if ((rnd.nextInt() % 20) != 0)
//					continue;

				float pref = model.getPreferenceValue(userID, itemID);
				if (min > pref)
					min = pref;
				if (max < pref)
					max = pref;
			}
			System.out.println(itemID + "," + min + "," + max);
		}
		items.hashCode();
	}

}


