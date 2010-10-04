package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Lookup;
import lsh.core.Point;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/*
 * Load semantic vectors tables.
 */

public class PointTextDataModel extends AbstractDataModel {
	// raw text corner-first LSH of users
	final Lookup userDB;
	// raw text corner-first LSH of items
	final Lookup itemDB;
	// real 1.0 = hash(1.0) * variance
	double varianceManhattan;
	// real 1.0 dist 
	final double diagonal;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;

	final boolean doPoints = true;

	public PointTextDataModel(String pointsFile) throws IOException {
		userDB = new Lookup(null, true, !doPoints, true, true, false, false);
		itemDB = new Lookup(null, true, !doPoints, true, true, false, false);
		Reader f;
		f = new FileReader(new File(pointsFile));
		itemDB.loadPoints(f, "I");
		f.close();
		f = new FileReader(new File(pointsFile));
		userDB.loadPoints(f, "U");
		f.close();

		Iterator<Point> points = itemDB.points.iterator();
		int dimension = points.next().values.length;
		double[] zero = new double[dimension];
		for(int i = 0; i < zero.length; i++)
			zero[i] = 0.0;
		double[] unit = new double[dimension];
		for(int i = 0; i < unit.length; i++)
			unit[i] = 1.0;
		varianceManhattan = 1.0;
		double dist = manhattanD(zero, unit);
		varianceManhattan = 1/dist;
//		System.err.println("Variance: " + varianceManhattan);

		diagonal = 1/Math.sqrt(dimension);
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return new LPI(itemDB.ids.iterator());
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumItems() throws TasteException {
		return itemDB.ids.size();
	}

	@Override
	public int getNumUsers() throws TasteException {
		return userDB.ids.size();
	}

	@Override
	public int getNumUsersWithPreferenceFor(long... itemIDs)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getPreferenceTime(long userID, long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float getPreferenceValue(long userID, long itemID)
	throws TasteException {
		return getPreferenceValuePoint(userID, itemID);
	}

	private Float getPreferenceValuePoint(long userID, long itemID) {
		Point userP = userDB.id2point.get((userID) + "");
		Point itemP = itemDB.id2point.get((itemID) + "");
		double distance = manhattanD(itemP.values, userP.values);
		return (float) distance2rating(distance);
	}

	@Override
	public PreferenceArray getPreferencesForItem(long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PreferenceArray getPreferencesFromUser(long userID)
	throws TasteException {
		return getPreferencesFromUserPoint(userID);
	}

	private PreferenceArray getPreferencesFromUserPoint(long userID)
	throws NoSuchUserException {
		int prefIndex = 0;
		Point up = userDB.id2point.get((userID) + "");
		if (null == up)
			throw new NoSuchUserException();
		PreferenceArray prefs = new GenericUserPreferenceArray(itemDB.ids.size());
		for(String item: itemDB.ids) {
			Point ip = itemDB.id2point.get(item);
			float dist = (float) distance2rating(manhattanD(up.values, ip.values));
			prefs.setUserID(prefIndex, userID);
			prefs.setItemID(prefIndex, Long.parseLong(item));
			prefs.setValue(prefIndex, dist);
			prefIndex++;
		}
		prefs.sortByItem();
		return prefs;
	}

	//	??? hash distance. really f'ud
	//	double manhattan(int[] a, int[] b) {
	//		float sum = 0;
	//		for(int i = 0; i < a.length; i++) {
	//			sum += Math.abs(a[i] - b[i]);
	//		}
	//		return (sum / a.length) * varianceManhattan;
	//	}

	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum * varianceManhattan;
	}

	// rectangular distance
	double euclidD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		return Math.sqrt(sum) * diagonal;
	}

	double distance2rating(double d) {
		double e = (1-d) * scale + offset;
		return e;
	}

	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return new LPI(userDB.ids.iterator());
	}

	@Override
	public boolean hasPreferenceValues() {
		return false;
	}

	@Override
	public void removePreference(long userID, long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setPreference(long userID, long itemID, float value)
	throws TasteException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		throw new UnsupportedOperationException();

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		PointTextDataModel model = new PointTextDataModel("/tmp/lsh_hadoop/GL_points_10k/part-r-00000");
		model.hashCode();
	}

}
