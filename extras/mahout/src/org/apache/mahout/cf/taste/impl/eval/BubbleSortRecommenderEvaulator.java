//package org.apache.mahout.cf.taste.impl.eval;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.TreeMap;
//
//import lsh.hadoop.LSHDriver;
//
//import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
//import org.apache.mahout.cf.taste.common.TasteException;
//import org.apache.mahout.cf.taste.eval.DataModelBuilder;
//import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
//import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
//import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
//import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
//import org.apache.mahout.cf.taste.impl.model.GenericPreference;
//import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
//import org.apache.mahout.cf.taste.impl.model.PointTextRecommender;
//import org.apache.mahout.cf.taste.impl.model.SimplexRecommender;
//import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
//import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
//import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
//import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
//import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
//import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
//import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
//import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
//import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
//import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
//import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
//import org.apache.mahout.cf.taste.model.DataModel;
//import org.apache.mahout.cf.taste.model.Preference;
//import org.apache.mahout.cf.taste.model.PreferenceArray;
//import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
//import org.apache.mahout.cf.taste.recommender.RecommendedItem;
//import org.apache.mahout.cf.taste.recommender.Recommender;
//import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
//import org.apache.mahout.cf.taste.similarity.UserSimilarity;
//
///*
// * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
// */
//
//public class BubbleSortRecommenderEvaulator implements RecommenderEvaluator {
//	float minPreference, maxPreference;
//	boolean doCSV = false;
//
//	@Override
//	public double evaluate(RecommenderBuilder recommenderBuilder,
//			DataModelBuilder dataModelBuilder, DataModel dataModel,
//			double trainingPercentage, double evaluationPercentage)
//	throws TasteException {
//		return 0;
//	}
//
//	public double evaluate(Recommender recco,
//			DataModel dataModel) throws TasteException {
//		double scores = 0;
//		LongPrimitiveIterator users = dataModel.getUserIDs();
////		if (doCSV)
////			System.out.println("user,count,match,scan,bubble,normal");
//		if (doCSV)
//			System.out.println("user,count,match,normal");
//
//		StandardDeviation std = new StandardDeviation();
//		while (users.hasNext()) {
//			long userID = users.nextLong();
//			// TreeMap preserves order of insertion
//			// could use TreeSet ?
//			Map<Long,Preference> prefMap = new TreeMap<Long, Preference>();
//			PreferenceArray prefs = dataModel.getPreferencesFromUser(userID);
//			int nprefs = prefs.length();
//			buildMap(prefs, prefMap);
//			Preference[] prefsDM = makeArray(prefs);
//			Preference[] prefsR = fullRecommend(recco, dataModel, prefMap, nprefs, userID);
//			// recommended list now contains only original prefs
//			int dist = hamming(prefsDM, prefsR);
//			int match = prefs.length() - hamming2(prefsDM, prefsR);
////			System.out.println("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
////			System.out.println("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
//			double deltas = scan(prefsDM.clone(), prefsR.clone());
//			double mean = ((double) deltas)/nprefs;
//			double scanScore = Math.sqrt(deltas)/nprefs;
////			System.out.println("\tdeltas: " + deltas + ", mean: " + mean + ", score: " + bubbleScore);
//			int swaps = sort(prefsDM.clone(), prefsR.clone());
//			mean = ((double) swaps)/nprefs;
//			double bubbleScore = Math.sqrt(mean)/nprefs;
////			System.out.println("\tswapped: " + swaps + ", mean: " + mean + ", score: " + bubbleScore);
////			scores += bubbleScore; //(scanScore + bubbleScore)/2;
//			if (userID == 55) {
//				normalWilcoxon(prefsDM, prefsR);
//			}
//			double normalW = normalWilcoxon(prefsDM, prefsR);
//			double variance = normalW;
//			if (doCSV)
//				System.out.println(userID + "," + nprefs + "," + match + "," + variance);
//			scores += variance;
//			std.increment(variance);
//			this.hashCode();
////	points gets more trash but need measure that finds it
//		}
//		return scores / dataModel.getNumUsers();
////		return std.getResult(); // scores / dataModel.getNumUsers();
//	} 
//
//	private Preference[] makeArray(PreferenceArray prefs) {
//		int nprefs = prefs.length();
//		Preference[] prefsA = new Preference[nprefs];
//		Iterator<Preference> prefIter = prefs.iterator();
//		for(int i = 0; i < nprefs; i++) {
//			prefsA[i] = prefIter.next();
//		}
//		if (nprefs > 1) {
//			Arrays.sort(prefsA, new Check());
//		}
//		return prefsA;
//	}
//
//	private Preference[] fullRecommend(Recommender recco, DataModel model, Map<Long, Preference> prefMap, int nitems,
//			long userID) throws TasteException {
//		Preference[] prefsR = new Preference[nitems];
//		
//		LongPrimitiveIterator iter = model.getItemIDs();
//		for(int i = 0; i < nitems;) {
//			Long itemID = iter.next();
//			if (! prefMap.containsKey(itemID))
//				continue;
//			float value = recco.estimatePreference(userID, itemID);
//			if (Float.isNaN(value))
//				value = 3.0f;
//			prefsR[i] = new GenericPreference(userID, itemID.longValue(), value);
//			i++;
//		}
//		if (nitems > 1) {
//			Arrays.sort(prefsR, new Check());
//		}
//
//		return prefsR;
//	}
//
//	private void buildMap(PreferenceArray prefs, Map<Long, Preference> prefMap) {
//		for(int i = 0; i < prefs.length(); i++) {
//			Long item = new Long(prefs.getItemID(i));
//			Preference p = prefs.get(i);
//			prefMap.put(item, p);
//		}		
//	}
//	
//	private int hamming(Preference[] prefsDM, Preference[] prefsR) {
//		int count = 0;
//		for(int i = 0; i < prefsDM.length; i++) {
//			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
//				count++;
//		}
//		return count;
//	}
//	
//	private int hamming2(Preference[] prefsDM, Preference[] prefsR) {
//		int count = 0;
//		for(int i = 1; i < prefsDM.length - 1; i++) {
//			if ((prefsDM[i].getItemID() != prefsR[i].getItemID())&&
//				(prefsDM[i+1].getItemID() != prefsR[i].getItemID())&&
//						(prefsDM[i-1].getItemID() != prefsR[i].getItemID()))
//				count++;
//		}
//		return count;
//	}
//
//	/*
//	 * Do bubble sort and return number of swaps needed to match preference lists
//	 */
//	int sort(Preference[] prefsDM, Preference[] prefsR) {
//		int length = prefsDM.length;
//		int swaps = 0;
//		int sorted = 0;	
//		boolean[] matches = new boolean[length];
//		for(int i = 0; i < length ; i++) {
//			matches[i] = (prefsDM[i].getItemID() == prefsR[i].getItemID());
//		}
//
//		while (sorted < length - 1) {
//			if (prefsDM[sorted].getItemID() == prefsR[sorted].getItemID()) {
//				sorted++;
//				continue;
//			} else {
//				for(int j = sorted; j < length - 1; j++) {
//					// do not swap anything already in place
//					int jump = 1;
//					if (matches[j])
//						while ((j + jump < length) && matches[j + jump] && (j + jump) < length) {
//							jump++;
//						}
//					if ((j + jump < length) && !(matches[j] && matches[j + jump])) {
//						Preference tmp = prefsR[j];
//						prefsR[j] = prefsR[j + 1];
//						prefsR[j + 1] = tmp;
//						swaps++;
////						if (swaps % 10000 == 0)
////							System.out.print(".");
//					}
//				}
//			}
//		}
//		for(int i = 0; i < length; i++) {
//			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
//				throw new Error("Sorting error?");		
//		}
//		
//		return swaps;
//	}
//	
//	/*
//	 * find total number of different spots
//	 */
//	double scan(Preference[] prefsDM, Preference[] prefsR) {
//		double sum = 0;
//		int nitems = prefsDM.length;
//		
//		for(int i = 0; i < nitems; i++) {
//			double delta = 0;
//			boolean found = false;
//			for(int j = i; j >= 0; j--) {
//				if (prefsDM[i].getItemID() == prefsR[j].getItemID()) {
//					found = true;
//					delta += i - j;
//					break;
//				}
//			}
//			if (! found) {
//				for(int j = i; j < nitems; j++) {
//					if (prefsDM[i].getItemID() == prefsR[j].getItemID()) {
//						found = true;
//						delta += j - i;
//						break;
//					}	
//				}
//			}
//			if (!found) 
//				this.hashCode();
//			if (delta > 0)
//				sum += delta; // Math.sqrt(1/delta);
//			prefsDM.hashCode();
//		}
//		
//		return sum;
//	}
//
//	/*
//	 * Normal-distribution probability value for matched sets of values
//	 * http://comp9.psych.cornell.edu/Darlington/normscor.htm
//	 */
//	double normalWilcoxon(Preference[] prefsDM, Preference[] prefsR) {
//		double mean = 0;
//		int nitems = prefsDM.length;
//		
//		int[] vectorZ = new int[nitems];
//		int[] vectorZabs = new int[nitems];
//		double[] ranks = new double[nitems];
//		double[] ranksAbs = new double[nitems];
//		getVectorZ(prefsDM, prefsR, vectorZ, vectorZabs);
//		wilcoxonRanks(vectorZ, vectorZabs, ranks, ranksAbs);
//		mean = getNormalMean(ranks, ranksAbs);
//		mean = Math.abs(mean) / (Math.sqrt(nitems));
//	  return mean;
//	}
//
//
//	/*
//	 * get mean of deviation from hypothesized center of 0
//	 */
//	private double getNormalMean(double[] ranks, double[] ranksAbs) {
//		int nitems = ranks.length;
//		double sum = 0;
//		int count = 0;
//		for(int i = 0; i < nitems; i++) {
//			sum += ranks[i];
//		}
//		double mean = sum / nitems;
//		return mean;
//	}
//
//	/*
//	 * vector Z is a list of distances between the correct value and the recommended value
//	 * Z[i] = position i of correct itemID - position of correct itemID in recommendation list
//	 * 	can be positive or negative
//	 * 	the smaller the better - means recommendations are closer
//	 * both are the same length, and both sample from the same set
//	 */
//	private void getVectorZ(Preference[] prefsDM, Preference[] prefsR, int[] vectorZ, int[] vectorZabs) {
//		int nitems = prefsDM.length;
//		for(int i = 0; i < nitems; i++) {
//			long itemID = prefsDM[i].getItemID();
//			for(int j = 0; j < nitems; j++) {
//				long test = prefsR[j].getItemID();
//				if (itemID == test) {
//					vectorZ[i] = i - j;
//					if (i != j)
//						this.hashCode();
//					break;
//				}
//			}	
//		}
//		for(int i = 0; i < nitems; i++) {
//			vectorZabs[i] = Math.abs(vectorZ[i]);
//		}
//	}
//	
//	/*
//	 * Ranks are the position of the value from low to high, divided by the # of values.
//	 * I had to walk through it a few times.
//	 */
//	private void wilcoxonRanks(int[] vectorZ, int[] vectorZabs, double[] ranks, double[] ranksAbs) {
//		int nitems = vectorZ.length;
//		int[] sorted = vectorZabs.clone();
//		Arrays.sort(sorted);
//		int zeros = 0;
//		for(; zeros < nitems; zeros++) {
//			if (sorted[zeros] > 0) 
//				break;
//		}
//		for(int i = 0; i < nitems; i++) {
//			double rank = 0;
//			int count = 0;
//			int score = vectorZabs[i];
//			for(int j = 0; j < nitems; j++) {
//				if (score == sorted[j]) {
//					rank += (j + 1) - zeros;
//					count++;
//				} else if (score < sorted[j]) {
//					break;
//				}
//			}
//			ranks[i] = (rank/count) * ((vectorZ[i] < 0) ? -1 : 1);	// better be at least 1
//		}
//		for(int i = 0; i < nitems; i++) {
//			ranksAbs[i] = Math.abs(ranks[i]);
//		}
//	}
////
////	private void wilcoxonRanksRong(int[] vectorZ, int[] vectorZabs, double[] ranks, double[] ranksAbs) {
////		int nitems = vectorZ.length;
////		int[] sorted = vectorZabs.clone();
////		Arrays.sort(sorted);
////		int zeros = 0;
////		for(; zeros < nitems; zeros++) {
////			if (sorted[zeros] > 0) 
////				break;
////		}
////		for(int i = 0; i < nitems; i++) {
////			double rank = 0;
////			int count = 0;
////			int score = vectorZabs[i];
////			for(int j = 0; j < nitems; j++) {
////				if (score == sorted[j]) {
////					rank += (j + 1) - zeros;
////					count++;
////				} else if (score < sorted[j]) {
////					break;
////				}
////			}
////			ranks[i] = rank/count;	// better be at least 1
////		}
////		for(int i = 0; i < nitems; i++) {
////			ranksAbs[i] = Math.abs(ranks[i]);
////		}
////	}
//
//	@Override
//	public float getMaxPreference() {
//		return maxPreference;
//	}
//
//	@Override
//	public float getMinPreference() {
//		return minPreference;
//	}
//
//	@Override
//	public void setMaxPreference(float maxPreference) {
//		this.maxPreference = maxPreference;
//	}
//
//	@Override
//	public void setMinPreference(float minPreference) {
//		this.minPreference = minPreference;
//	}
//
//	/**
//	 * @param args
//	 * @throws TasteException 
//	 * @throws IOException 
//	 * @throws ClassNotFoundException 
//	 * @throws IllegalAccessException 
//	 * @throws InstantiationException 
//	 */
//	public static void main(String[] args) throws TasteException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
//		GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0]));
//		Recommender recco;
//		BubbleSortRecommenderEvaulator bsrv = new BubbleSortRecommenderEvaulator();
//
//		double score ;
//		recco = doEstimatingUser(glModel);
////		bsrv.doCSV = true;
//		score = bsrv.evaluate(recco, glModel);
//		System.out.println("Estimating score: " + score);
//		Recommender	prec = doPointText(args[1]);
//		score = bsrv.evaluate(prec, glModel);
//		System.out.println("Point score: " + score);
//		Recommender	pearsonrec = doReccoPearsonItem(glModel);
//		score = bsrv.evaluate(pearsonrec, glModel);
//		System.out.println("Pearson score: " + score);
//		Recommender	slope1rec = doReccoSlope1(glModel);
//		score = bsrv.evaluate(slope1rec, glModel);
//		System.out.println("Slope1 score: " + score);
////		Recommender srec = doSimplexDataModel(args[2]);
////		score = bsrv.evaluate(srec, glModel);
////		System.out.println("Simplex score: " + score);
//	}
//
//	private static Recommender doPointText(String pointsFile) throws IOException {
//		Recommender prec;
//		DataModel model = new PointTextDataModel(pointsFile);
//		prec = new PointTextRecommender(model);
//		return prec;
//	}
//	
//	private static Recommender doEstimatingUser(DataModel bcModel) throws TasteException {
//		   UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
//		    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
//		    return new EstimatingUserBasedRecommender(bcModel, neighborhood, similarity);
//
//	}
//
//	private static Recommender doReccoKNN_LL_NegQO(DataModel glModel) {
//		Recommender recco;
//		ItemSimilarity similarity = new LogLikelihoodSimilarity(glModel);
//		Optimizer optimizer = new NonNegativeQuadraticOptimizer();
//		recco = new EstimatingKnnItemBasedRecommender(glModel, similarity, optimizer, 6040);
//		return recco;
//	}
//
//	private static Recommender doReccoPearsonItem(DataModel glModel)
//	throws TasteException {
//		Recommender recco;
//		ItemSimilarity similarity = new PearsonCorrelationSimilarity(glModel);
//		recco = new EstimatingItemBasedRecommender(glModel, similarity);
//		return recco;
//	}
//
//	private static Recommender doReccoSlope1(DataModel glModel)
//	throws TasteException {
//		return new EstimatingSlopeOneRecommender(glModel);
//	}
//
//	private static SimplexRecommender doSimplexDataModel(String cornersfile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
//		Properties props = new Properties();
//		props.setProperty(LSHDriver.HASHER, "lsh.core.VertexTransitiveHasher");
//		props.setProperty(LSHDriver.DIMENSION, "100");
//		props.setProperty(LSHDriver.GRIDSIZE, "0.54");
//		SimplexRecommender rec = new SimplexRecommender(props, cornersfile);
//		return rec;
//	}
//
//
//}
//
//class Check implements Comparator<Preference> {
//
//	@Override
//	public int compare(Preference p1, Preference p2) {
//		if (p1.getValue() > p2.getValue())
//			return 1;
//		else if (p1.getValue() < p2.getValue())
//			return -1;
//		else 
//			return 0;
//	}
//	
//}

