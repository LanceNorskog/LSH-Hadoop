package org.apache.mahout.cf.taste.impl.eval;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class BubbleSortRecommenderEvaulator implements RecommenderEvaluator {
	float minPreference, maxPreference;
	boolean doCSV = false;

	@Override
	public double evaluate(RecommenderBuilder recommenderBuilder,
			DataModelBuilder dataModelBuilder, DataModel dataModel,
			double trainingPercentage, double evaluationPercentage)
	throws TasteException {
		return 0;
	}

	public double evaluate(Recommender recco,
			DataModel dataModel) throws TasteException {
		double scores = 0;
		LongPrimitiveIterator users = dataModel.getUserIDs();
//		if (doCSV)
//			System.out.println("user,count,match,scan,bubble,normal");
		if (doCSV)
			System.out.println("user,count,match,normal");

	    RunningAverageAndStdDev stdev = new CompactRunningAverageAndStdDev();
		while (users.hasNext()) {
			long userID = users.nextLong();
			// TreeMap preserves order of insertion
			// could use TreeSet ?
			Map<Long,Preference> prefMap = new TreeMap<Long, Preference>();
			PreferenceArray prefs = dataModel.getPreferencesFromUser(userID);
			int nprefs = prefs.length();
			buildMap(prefs, prefMap);
			Preference[] prefsDM = makeArray(prefs);
			Preference[] prefsR = fullRecommend(recco, dataModel, prefMap, nprefs, userID);
			// recommended list now contains only original prefs
			int dist = hamming(prefsDM, prefsR);
			int match = prefs.length() - hamming2(prefsDM, prefsR);
//			System.out.println("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
//			System.out.println("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
			double deltas = scan(prefsDM.clone(), prefsR.clone());
			double mean = ((double) deltas)/nprefs;
			double scanScore = Math.sqrt(deltas)/nprefs;
//			System.out.println("\tdeltas: " + deltas + ", mean: " + mean + ", score: " + bubbleScore);
			int swaps = sort(prefsDM.clone(), prefsR.clone());
			mean = ((double) swaps)/nprefs;
			double bubbleScore = Math.sqrt(mean)/nprefs;
//			System.out.println("\tswapped: " + swaps + ", mean: " + mean + ", score: " + bubbleScore);
//			scores += bubbleScore; //(scanScore + bubbleScore)/2;
			if (userID == 55) {
				normalWilcoxon(prefsDM, prefsR);
			}
			double normalW = normalWilcoxon(prefsDM, prefsR);
			double variance = normalW;
			if (doCSV)
				System.out.println(userID + "," + nprefs + "," + match + "," + variance);
			scores += variance;
			stdev.addDatum(variance);
			this.hashCode();
//	points gets more trash but need measure that finds it
		}
		return scores / dataModel.getNumUsers();
//		return std.getResult(); // scores / dataModel.getNumUsers();
	} 

	private Preference[] makeArray(PreferenceArray prefs) {
		int nprefs = prefs.length();
		Preference[] prefsA = new Preference[nprefs];
		Iterator<Preference> prefIter = prefs.iterator();
		for(int i = 0; i < nprefs; i++) {
			prefsA[i] = prefIter.next();
		}
		if (nprefs > 1) {
			Arrays.sort(prefsA, new PrefComparator());
		}
		return prefsA;
	}

	private Preference[] fullRecommend(Recommender recco, DataModel model, Map<Long, Preference> prefMap, int nitems,
			long userID) throws TasteException {
		Preference[] prefsR = new Preference[nitems];
		
		LongPrimitiveIterator iter = model.getItemIDs();
		for(int i = 0; i < nitems;) {
			Long itemID = iter.next();
			if (! prefMap.containsKey(itemID))
				continue;
			float value = recco.estimatePreference(userID, itemID);
			if (Float.isNaN(value))
				value = 3.0f;
			prefsR[i] = new GenericPreference(userID, itemID.longValue(), value);
			i++;
		}
		if (nitems > 1) {
			Arrays.sort(prefsR, new PrefComparator());
		}

		return prefsR;
	}

	private void buildMap(PreferenceArray prefs, Map<Long, Preference> prefMap) {
		for(int i = 0; i < prefs.length(); i++) {
			Long item = new Long(prefs.getItemID(i));
			Preference p = prefs.get(i);
			prefMap.put(item, p);
		}		
	}
	
	private int hamming(Preference[] prefsDM, Preference[] prefsR) {
		int count = 0;
		for(int i = 0; i < prefsDM.length; i++) {
			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
				count++;
		}
		return count;
	}
	
	private int hamming2(Preference[] prefsDM, Preference[] prefsR) {
		int count = 0;
		for(int i = 1; i < prefsDM.length - 1; i++) {
			if ((prefsDM[i].getItemID() != prefsR[i].getItemID())&&
				(prefsDM[i+1].getItemID() != prefsR[i].getItemID())&&
						(prefsDM[i-1].getItemID() != prefsR[i].getItemID()))
				count++;
		}
		return count;
	}

	/*
	 * Do bubble sort and return number of swaps needed to match preference lists
	 */
	int sort(Preference[] prefsDM, Preference[] prefsR) {
		int length = prefsDM.length;
		int swaps = 0;
		int sorted = 0;	
		boolean[] matches = new boolean[length];
		for(int i = 0; i < length ; i++) {
			matches[i] = (prefsDM[i].getItemID() == prefsR[i].getItemID());
		}

		while (sorted < length - 1) {
			if (prefsDM[sorted].getItemID() == prefsR[sorted].getItemID()) {
				sorted++;
				continue;
			} else {
				for(int j = sorted; j < length - 1; j++) {
					// do not swap anything already in place
					int jump = 1;
					if (matches[j])
						while ((j + jump < length) && matches[j + jump] && (j + jump) < length) {
							jump++;
						}
					if ((j + jump < length) && !(matches[j] && matches[j + jump])) {
						Preference tmp = prefsR[j];
						prefsR[j] = prefsR[j + 1];
						prefsR[j + 1] = tmp;
						swaps++;
//						if (swaps % 10000 == 0)
//							System.out.print(".");
					}
				}
			}
		}
		for(int i = 0; i < length; i++) {
			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
				throw new Error("Sorting error?");		
		}
		
		return swaps;
	}
	
	/*
	 * find total number of different spots
	 */
	double scan(Preference[] prefsDM, Preference[] prefsR) {
		double sum = 0;
		int nitems = prefsDM.length;
		
		for(int i = 0; i < nitems; i++) {
			double delta = 0;
			boolean found = false;
			for(int j = i; j >= 0; j--) {
				if (prefsDM[i].getItemID() == prefsR[j].getItemID()) {
					found = true;
					delta += i - j;
					break;
				}
			}
			if (! found) {
				for(int j = i; j < nitems; j++) {
					if (prefsDM[i].getItemID() == prefsR[j].getItemID()) {
						found = true;
						delta += j - i;
						break;
					}	
				}
			}
			if (!found) 
				this.hashCode();
			if (delta > 0)
				sum += delta; // Math.sqrt(1/delta);
			prefsDM.hashCode();
		}
		
		return sum;
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
		mean = getNormalMean(ranks, ranksAbs);
		mean = Math.abs(mean) / (Math.sqrt(nitems));
	  return mean;
	}


	/*
	 * get mean of deviation from hypothesized center of 0
	 */
	private double getNormalMean(double[] ranks, double[] ranksAbs) {
		int nitems = ranks.length;
		double sum = 0;
		for(int i = 0; i < nitems; i++) {
			sum += ranks[i];
		}
		double mean = sum / nitems;
		return mean;
	}

	/*
	 * vector Z is a list of distances between the correct value and the recommended value
	 * Z[i] = position i of correct itemID - position of correct itemID in recommendation list
	 * 	can be positive or negative
	 * 	the smaller the better - means recommendations are closer
	 * both are the same length, and both sample from the same set
	 */
	private void getVectorZ(Preference[] prefsDM, Preference[] prefsR, int[] vectorZ, int[] vectorZabs) {
		int nitems = prefsDM.length;
		for(int i = 0; i < nitems; i++) {
			long itemID = prefsDM[i].getItemID();
			for(int j = 0; j < nitems; j++) {
				long test = prefsR[j].getItemID();
				if (itemID == test) {
					vectorZ[i] = i - j;
					if (i != j)
						this.hashCode();
					break;
				}
			}	
		}
		for(int i = 0; i < nitems; i++) {
			vectorZabs[i] = Math.abs(vectorZ[i]);
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
			ranks[i] = (rank/count) * ((vectorZ[i] < 0) ? -1 : 1);	// better be at least 1
		}
		for(int i = 0; i < nitems; i++) {
			ranksAbs[i] = Math.abs(ranks[i]);
		}
	}
//
//	private void wilcoxonRanksRong(int[] vectorZ, int[] vectorZabs, double[] ranks, double[] ranksAbs) {
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
//			ranks[i] = rank/count;	// better be at least 1
//		}
//		for(int i = 0; i < nitems; i++) {
//			ranksAbs[i] = Math.abs(ranks[i]);
//		}
//	}

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
}