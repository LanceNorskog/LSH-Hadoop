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

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommender;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

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
		DataModel realModel = new GroupLensDataModel(new File(args[0]));
		GroupLensRecommender realRecco = new GroupLensRecommender(realModel);
		printAllRecommendations(realRecco);
	}

	private static void printAllRecommendations(Recommender recco)
	throws TasteException {
		DataModel model = recco.getDataModel();
		LongPrimitiveIterator items = model.getItemIDs();
		while (items.hasNext()) {
			long itemID = items.nextLong();
			double sum = 0;
			int count = 0;
			LongPrimitiveIterator users = model.getUserIDs();
			while (users.hasNext()) {
				long userID = users.nextLong();

				float pref = recco.estimatePreference(userID, itemID);
				if (pref != Float.NaN) {
					sum += pref;
					count++;
				}
			}
			double rating = (sum > 0 && count > 0) ? sum / count : 0;
			System.out.println(itemID + "," + count + "," + rating);
		}
	}

}


