package org.apache.mahout.cf.taste.impl.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.PointTextDataModel;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

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
		int nitems = dataModel.getNumItems();
		LongPrimitiveIterator users = dataModel.getUserIDs();

		while (users.hasNext()) {
			long userID = users.nextLong();
			PreferenceArray prefs = dataModel.getPreferencesFromUser(userID);
			List<RecommendedItem> prefListR = recco.recommend(userID, prefs.length());
			Preference[] prefsDM = new GenericPreference[prefListR.size()];
			Preference[] prefsR = new GenericPreference[prefListR.size()];
			initArray(prefs, prefListR, prefsDM, prefsR, dataModel, recco, userID);
			int dist = hamming(prefsDM, prefsR);
			System.out.print("Total: " + prefs.length() + ", match: " + (prefs.length() - dist));
			int swaps = sort(prefsDM, prefsR);
			System.out.println(", swapped: " + swaps);

		}
		return 0;
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
		for(int i = 0; i < length ; i++) {
			if (prefsDM[i].getItemID() == prefsR[i].getItemID()) {
//				System.out.println("Same item: " + i);
			}
		}
		for(int i = 0; i < length - 1; i++) {
			for(int j = sorted; j < length - 1; j++) {
				if (prefsR[j].getItemID() <= prefsR[j + 1].getItemID()) {
					continue;
				} else {
					Preference tmp = prefsR[j];
					prefsR[j] = prefsR[j + 1];
					prefsR[j + 1] = tmp;
					swaps++;
				}
			}
		}
		for(int i = 0; i < length; i++) {
			if (prefsDM[i].getItemID() != prefsR[i].getItemID())
				throw new Error("Sorting error?");		
		}
		
		return swaps;
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
		recco = doReccoSlope1(glModel);
		BubbleSortRecommenderEvaulator bsrv = new BubbleSortRecommenderEvaulator();
		double d = bsrv.evaluate(recco, glModel);
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

