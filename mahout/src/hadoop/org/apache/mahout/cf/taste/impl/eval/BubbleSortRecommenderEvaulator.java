package org.apache.mahout.cf.taste.impl.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
import org.apache.mahout.cf.taste.impl.model.PointTextRecommender;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/*
 * Evaluate recommender by comparing order of all raw prefs with order in recommender's output for that user.
 */

public class BubbleSortRecommenderEvaulator implements RecommenderEvaluator {
	float minPreference, maxPreference;

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
//			System.out.println("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
//			int swaps = sort(prefsDM, prefsR);
//			double mean = ((double) swaps)/nprefs;
//			System.out.println("\tswapped: " + swaps + ", mean: " + mean + ", sqrt: " + (Math.sqrt(mean)/nprefs));
			double deltas = scan(prefsDM, prefsR);
			double mean = ((double) deltas)/nprefs;
			double score = Math.sqrt(deltas)/nprefs;
//			System.out.println("\tdeltas: " + deltas + ", mean: " + mean + ", sqrt: " + score);
			scores += score;
		}
		return scores / dataModel.getNumUsers();
	}

	private Preference[] makeArray(PreferenceArray prefs) {
		int nprefs = prefs.length();
		Preference[] prefsA = new Preference[nprefs];
		Iterator<Preference> prefIter = prefs.iterator();
		for(int i = 0; i < nprefs; i++) {
			prefsA[i] = prefIter.next();
		}
		if (nprefs > 1) {
			Arrays.sort(prefsA, new Check());
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
			Arrays.sort(prefsR, new Check());
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

	private void filterMap(List<RecommendedItem> prefListR,
			Map<Long, Preference> prefMap) {
		int i = 0;
		while (i < prefMap.size()) {
			RecommendedItem rec = prefListR.get(i);
			Long itemID = rec.getItemID();
			if (! prefMap.containsKey(itemID)) 
				prefListR.remove(i);
			else
				i++;
		}
		int len = prefListR.size();
		int len2 = prefMap.size();
		this.hashCode();
	}
	
	private void makeArrays(Map<Long, Preference> prefMap, List<RecommendedItem> prefListR, Long[] prefsDM, Long[] prefsR) {
		Iterator<Long> iterDM = prefMap.keySet().iterator();
		for(int i = 0; i < prefsDM.length; i++) {
			prefsDM[i] = iterDM.next();
		}
		Iterator<RecommendedItem> iterR = prefListR.iterator();
		for(int i = 0; i < prefsR.length; i++) {
			RecommendedItem itemR = iterR.next();
			prefsR[i] = itemR.getItemID();
		}
	}
	
	private void initArray( PreferenceArray prefs, List<RecommendedItem> prefListR, Preference[] prefsDM, Preference[] prefsR, DataModel dataModel, Recommender recco, long userID) throws TasteException {
		int pr = prefs.length();
		int plr = prefListR.size();
		int i = 0;
		for(; i < prefListR.size(); i++) {
			if (i == prefsDM.length) {
				prefListR.hashCode();
			}
			if (i == prefsR.length) {
				prefListR.hashCode();
			}
			prefsDM[i] = new GenericPreference(userID, prefs.getItemID(i), prefs.getValue(i));
			prefsR[i] = new GenericPreference(userID, prefListR.get(i).getItemID(), prefListR.get(i).getValue());
		}
		if (prefsDM.length > 1) {
			Arrays.sort(prefsDM, new Check());
			Arrays.sort(prefsR, new Check());
		}
	}
	
	int hamming(Preference[] prefsDM, Preference[] prefsR) {
		int count = 0;
		for(int i = 0; i < prefsDM.length; i++) {
			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
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

//		return 0;
//		for(int i = 0; i < length - 1; i++) {
//			for(int j = sorted; j < length - 1; j++) {
//				if (prefsR[j].getItemID() <= prefsR[j + 1].getItemID()) {
//					continue;
//				} else {
//					Preference tmp = prefsR[j];
//					prefsR[j] = prefsR[j + 1];
//					prefsR[j + 1] = tmp;
//					swaps++;
//				}
//			}
//		}
		while (sorted < length - 1) {
			if (prefsDM[sorted].getItemID() == prefsR[sorted].getItemID()) {
				sorted++;
				continue;
			} else {
				for(int j = sorted; j < length - 1; j++) {
					// do not swap anything already in place
					int jump = 1;
					if (matches[j])
						while (matches[j + jump] && (j + jump) < length) {
							jump++;
						}
					if (!(matches[j] && matches[j + jump])) {
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
//		for(int i = 0; i < length ; i++) {
//			if (prefsDM[i].getItemID() == prefsR[i].getItemID()) {
//				System.out.println("Same item: " + i);
//			}
//		}
		for(int i = 0; i < length; i++) {
			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
				throw new Error("Sorting error?");		
		}
		
		return swaps;
	}
	
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
			if (delta > 0)
				sum += delta; // Math.sqrt(1/delta);
			prefsDM.hashCode();
		}
		
		return sum;
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
	 */
	public static void main(String[] args) throws TasteException, IOException {
		GroupLensDataModel glModel = new GroupLensDataModel(new File(args[0]));
		Recommender recco;
//		recco = doReccoSlope1(glModel);
		recco = doGenericUser(glModel);
		BubbleSortRecommenderEvaulator bsrv = new BubbleSortRecommenderEvaulator();
		double score = bsrv.evaluate(recco, glModel);
		System.out.println("Total score: " + score);
		DataModel model = new PointTextDataModel(args[1]);
		Recommender prec = new PointTextRecommender(model);
		score = bsrv.evaluate(prec, glModel);
		System.out.println("Total score: " + score);

	}
	
	private static Recommender doGenericUser(DataModel bcModel) throws TasteException {
		   UserSimilarity similarity = new CachingUserSimilarity(new EuclideanDistanceSimilarity(bcModel), bcModel);
		    UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, 0.2, similarity, bcModel, 0.2);
		    return new EstimatingUserRecommender(bcModel, neighborhood, similarity);

	}

	private static Recommender doReccoSlope1(DataModel glModel)
	throws TasteException {
		return new SlopeOneRecommender(glModel);
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

