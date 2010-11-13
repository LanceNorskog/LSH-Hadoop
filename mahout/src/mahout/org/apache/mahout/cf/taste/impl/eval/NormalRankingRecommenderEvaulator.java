package org.apache.mahout.cf.taste.impl.eval;

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

public class NormalRankingRecommenderEvaulator implements RecommenderEvaluator {
	float minPreference, maxPreference;
	boolean doCSV = false;
	

	@Override
	public double evaluate(RecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel dataModel,
			double trainingPercentage, double evaluationPercentage)
	throws TasteException {
		return 0;
	}

	/*
	 * Get randomly sampled recommendations
	 * 
	 */
	public double evaluate(Recommender recco,
			DataModel dataModel, Random rnd, int samples) throws TasteException {
		double scores = 0;
		int allItems = dataModel.getNumItems();
		LongPrimitiveIterator users = dataModel.getUserIDs();
		if (doCSV)
			System.out.println("user,sampled,match,normal");

		int foundusers = 0;
		while (users.hasNext()) {
			long userID = users.nextLong();
			allItems = Math.min(allItems, samples);
			List<RecommendedItem> recs;
			try {
				recs = recco.recommend(userID, allItems);
			} catch (NoSuchUserException e) {
				continue;
			}
			int sampled = recs.size();
			if (sampled == 0)
				continue;
			foundusers++;
			Preference[] prefsR = new Preference[sampled];
			Preference[] prefsDM = new Preference[sampled];
			getPrefsArray(recs, userID, prefsR, rnd);
			getMatchesFromDataModel(userID, prefsDM, recs, dataModel);
			int match = sampled - sloppyHamming(prefsDM, prefsR);
			double normalW = normalWilcoxon(prefsDM, prefsR);
			double variance = normalW/sampled;
			if (doCSV)
				System.out.println(userID + "," + sampled + "," + match + "," + variance);
			scores += variance;
			this.hashCode();
			//	points gets more trash but need measure that finds it
		}
		return scores / foundusers;
	} 

	/*
	 * Get randomly sampled recommendations
	 * 
	 */
	public double evaluate(Recommender recco1,
			Recommender recco2, Random rnd, int samples) throws TasteException {
		double scores = 0;
		int allItems = recco1.getDataModel().getNumItems();
		LongPrimitiveIterator users = recco1.getDataModel().getUserIDs();
		if (doCSV)
			System.out.println("user,sampled,match,normal");

		int foundusers = 0;
		while (users.hasNext()) {
			long userID = users.nextLong();
			allItems = Math.min(allItems, samples);
			List<RecommendedItem> recs;
			try {
				recs = recco1.recommend(userID, allItems);
			} catch (NoSuchUserException e) {
				continue;
			}
			int sampled = recs.size();
			if (sampled == 0)
				continue;
			foundusers++;
			Preference[] prefsR = new Preference[sampled];
			Preference[] prefsDM = new Preference[sampled];
			getPrefsArray(recs, userID, prefsR, rnd);
			getMatchesFromRecommender(userID, prefsDM, recs, recco2);
			int match = sampled - sloppyHamming(prefsDM, prefsR);
			double normalW = normalWilcoxon(prefsDM, prefsR);
			double variance = normalW/sampled;
			if (doCSV)
				System.out.println(userID + "," + sampled + "," + match + "," + variance);
			scores += variance;
			this.hashCode();
			//	points gets more trash but need measure that finds it
		}
		return scores / foundusers;
	} 

	/*
	 * Fill subsampled array from full list of recommended items 
	 * Some recommenders give short lists
	 */
	private Preference[] getPrefsArray(List<RecommendedItem> recs, long userID, Preference[] prefs, Random rnd) {
		int nprefs = prefs.length;
		if (nprefs > recs.size()) 
			this.hashCode();
		int found = 0;
		while (found < nprefs) {
			double sample = rnd.nextDouble();
			int n = (int) Math.min(sample * (nprefs - 1), nprefs - 1);
			if (null == prefs[found]) {
				prefs[found] = new GenericPreference(userID, recs.get(found).getItemID(), recs.get(found).getValue());
				found++;
			}
		}
		Arrays.sort(prefs, new PrefCheck(-1));
		return prefs;
	}

	private Preference[] getMatchesFromDataModel(Long userID, Preference[] prefs, List<RecommendedItem> recs,
			DataModel dataModel) throws TasteException {
		int nprefs = prefs.length;
		Iterator<RecommendedItem> it = recs.iterator();
		for(int i = 0; i < nprefs; i++) {
			RecommendedItem rec = it.next();
			Float value = dataModel.getPreferenceValue(userID, rec.getItemID());
			prefs[i] = new GenericPreference(userID, rec.getItemID(), value);
		}
		Arrays.sort(prefs, new PrefCheck(-1));
		return prefs;
	}

	private Preference[] getMatchesFromRecommender(Long userID, Preference[] prefs, List<RecommendedItem> recs,
			Recommender recco2) throws TasteException {
		int nprefs = prefs.length;
		Iterator<RecommendedItem> it = recs.iterator();
		for(int i = 0; i < nprefs; i++) {
			RecommendedItem rec = it.next();
			Float value = recco2.estimatePreference(userID, rec.getItemID());
			prefs[i] = new GenericPreference(userID, rec.getItemID(), value);
		}
		Arrays.sort(prefs, new PrefCheck(-1));
		return prefs;
	}

	private int sloppyHamming(Preference[] prefsDM, Preference[] prefsR) {
		int count = 0;
		try {
			for(int i = 1; i < prefsDM.length - 1; i++) {
				long itemID = prefsR[i].getItemID();
				if ((prefsDM[i].getItemID() != itemID) &&
						(prefsDM[i+1].getItemID() != itemID)&&
						(prefsDM[i-1].getItemID() != itemID)) {
					count++;
				} else {
//					System.out.println("xxx");
					this.hashCode();
				}
			}
		} catch (Exception e) {
			this.hashCode();
		}
		return count;
	}

	/*
	 * Normal-distribution probability value for matched sets of values
	 * http://comp9.psych.cornell.edu/Darlington/normscor.htm
	 */
	double normalWilcoxon(Preference[] prefsDM, Preference[] prefsR) {
		double mean = 0;
		int nitems = prefsDM.length;

		int[] vectorZ = new int[nitems];
		int[] vectorZabs = new int[nitems];
		double[] ranks = new double[nitems];
		double[] ranksAbs = new double[nitems];
		getVectorZ(prefsDM, prefsR, vectorZ, vectorZabs);
		wilcoxonRanks(vectorZ, vectorZabs, ranks, ranksAbs);
		// Mean of abs values is W+, Mean of signed values is W-
//		mean = getMeanRank(ranks);
//		mean = Math.abs(mean) * (Math.sqrt(nitems));
		mean = Math.min(getMeanWplus(ranks), getMeanWminus(ranks));
		mean = mean * (Math.sqrt(nitems));
		return mean;
	}

	/*
	 * vector Z is a list of distances between the correct value and the recommended value
	 * Z[i] = position i of correct itemID - position of correct itemID in recommendation list
	 * 	can be positive or negative
	 * 	the smaller the better - means recommendations are closer
	 * both are the same length, and both sample from the same set
	 * 
	 * destructive to prefsDM and prefsR arrays - allows N log N instead of N^2 order
	 */
	private void getVectorZ(Preference[] prefsDM, Preference[] prefsR, int[] vectorZ, int[] vectorZabs) {
		int nitems = prefsDM.length;
		int bottom = 0;
		int top = nitems - 1;
		for(int i = 0; i < nitems; i++) {
			long itemID = prefsDM[i].getItemID();
			for(int j = bottom; j <= top; j++) {
				if (prefsR[j] == null)
					continue;
				long test = prefsR[j].getItemID();
				if (itemID == test) {
					vectorZ[i] = i - j;
					vectorZabs[i] = Math.abs(i - j);
					if (j == bottom) {
						bottom++;
					} else if (j == top) {
						top--;
					} else {
						prefsR[j] = null;
					}
					break;
				}
			}	
		}
	}

	/*
	 * Ranks are the position of the value from low to high, divided by the # of values.
	 * I had to walk through it a few times.
	 */
	private void wilcoxonRanks(int[] vectorZ, int[] vectorZabs, double[] ranks, double[] ranksAbs) {
		int nitems = vectorZ.length;
		int[] sorted = vectorZabs.clone();
		Arrays.sort(sorted);
		int zeros = 0;
		for(; zeros < nitems; zeros++) {
			if (sorted[zeros] > 0) 
				break;
		}
		for(int i = 0; i < nitems; i++) {
			double rank = 0;
			int count = 0;
			int score = vectorZabs[i];
			for(int j = 0; j < nitems; j++) {
				if (score == sorted[j]) {
					rank += (j + 1) - zeros;
					count++;
				} else if (score < sorted[j]) {
					break;
				}
			}
			if (vectorZ[i] != 0) {
				ranks[i] = (rank/count) * ((vectorZ[i] < 0) ? -1 : 1);	// better be at least 1
				ranksAbs[i] = Math.abs(ranks[i]);
			}
		}
		
		this.hashCode();
	}

	/*
	 * get mean of deviation from hypothesized center of 0
	 */
	private double getMeanRank(double[] ranks) {
		int nitems = ranks.length;
		double sum = 0;
		for(int i = 0; i < nitems; i++) {
			sum += ranks[i];
		}
		double mean = sum / nitems;
		return mean;
	}

	private double getMeanWplus(double[] ranks) {
		int nitems = ranks.length;
		double sum = 0;
		for(int i = 0; i < nitems; i++) {
			if (ranks[i] > 0)
				sum += ranks[i];
		}
		double mean = sum / nitems;
		return mean;
	}

	private double getMeanWminus(double[] ranks) {
		int nitems = ranks.length;
		double sum = 0;
		for(int i = 0; i < nitems; i++) {
			if (ranks[i] < 0)
				sum += -ranks[i];
		}
		double mean = sum / nitems;
		return mean;
	}

	@Override
	public float getMaxPreference() {
		return maxPreference;
	}

	@Override
	public float getMinPreference() {
		return minPreference;
	}

	@Override
	public void setMaxPreference(float maxPreference) {
		this.maxPreference = maxPreference;
	}

	@Override
	public void setMinPreference(float minPreference) {
		this.minPreference = minPreference;
	}

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
		Recommender pointRecco = doPointText(args[1]);
		DataModel pointModel = pointRecco.getDataModel();
		Random random = new Random(0);
		int samples = 50;
		NormalRankingRecommenderEvaulator bsrv = new NormalRankingRecommenderEvaulator();
//		bsrv.doCSV = true;

		double score ;
//		random.setSeed(0);
//		score = bsrv.evaluate(pointRecco, pointModel, random, samples);
//		System.out.println("Point score: " + score);
		
		
//		This cannot find uncommons maths from the command line!
		Recommender estimatingRecco = doEstimatingUser(glModel);
		random.setSeed(0);
		score = bsrv.evaluate(estimatingRecco, pointModel, random, samples);
		System.out.println("Estimating score: " + score);
		
		Recommender pearsonRecco = doReccoPearsonItem(glModel);
//		random.setSeed(0);
//		score = bsrv.evaluate(pearsonRecco, pointModel, random, samples);
//		System.out.println("Pearson v.s. point model score: " + score);
		Recommender slope1Recco = doReccoSlope1(glModel);
		random.setSeed(0);
		score = bsrv.evaluate(slope1Recco, pointModel, random, samples);
		System.out.println("Slope1 v.s. point model score: " + score);
		score = bsrv.evaluate(pearsonRecco, slope1Recco, random, samples);
		System.out.println("Slope1 v.s. Pearson score: " + score);
//		needs namedvectors
		Recommender simplexRecco = doSimplexDataModel(args[2]);
		DataModel simplexModel = simplexRecco.getDataModel();
		score = bsrv.evaluate(simplexRecco, pointModel, random, samples);
		System.out.println("Simplex v.s. point model score: " + score);
//		score = bsrv.evaluate(simplexRecco, simplexModel, random, samples);
//		System.out.println("Simplex v.s. simplex model score: " + score);
//		score = bsrv.evaluate(simplexRecco, slope1Recco, random, samples);
//		System.out.println("Simplex v.s. Slope1 score: " + score);
//		score = bsrv.evaluate(pearsonRecco, simplexRecco, random, samples);

//		System.out.println("Simplex v.s. Pearson score: " + score);
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

