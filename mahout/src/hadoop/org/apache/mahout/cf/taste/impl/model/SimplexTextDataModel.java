package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.Lookup;
import lsh.core.Point;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
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
 * Load LSH corners-based text format using LSH bag-of-objects.
 * Very inefficient!
 * Load full database of points.
 * Can't load very many!
 */

public class SimplexTextDataModel extends AbstractDataModel {
	// raw text corner-first LSH of users
	final Lookup userDB;
	// raw text corner-first LSH of items
	final Lookup itemDB;
	// LSH projector
	final Hasher hasher;
	// corner-generator - does LSH projection
	final CornerGen cg;
	// real 1.0 = hash(1.0) * variance
	final double varianceEuclid;
	final double varianceManhattan = Double.NaN;
	// real 1.0 dist 
	final double diagonal;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;
	
	final boolean doPoints = true;

	public SimplexTextDataModel(String cornersFile, Hasher hasher, CornerGen cg) throws IOException {
		this.hasher = hasher;
		this.cg = cg;
		userDB = new Lookup(null, true, !doPoints, true, true, false, false);
		itemDB = new Lookup(null, true, !doPoints, true, true, false, false);
		Reader f;
		f = new FileReader(new File(cornersFile));
		itemDB.loadCP(f, "I");
		f.close();
		f = new FileReader(new File(cornersFile));
		userDB.loadCP(f, "U");
		f.close();
		int dimension = cg.stretch.length;
		if (null != hasher) {
			double[] unit = new double[dimension];
			for(int i = 0; i < unit.length; i++)
				unit[i] = 1.0;
			double[] projected = new double[dimension];
			hasher.project(unit, projected);
			double dist = manhattanD(unit, projected);
			varianceEuclid = 1.0 / dist;
			System.out.println("Variance: " + varianceEuclid);
		} else
			varianceEuclid = 1.0;
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
	
	private Float getPreferenceValueCorner(long userID, long itemID)
	throws TasteException {
		Point userP = userDB.id2point.get((userID) + "");
		int[] hashUser = hasher.hash(userP.values);
		Point itemP = itemDB.id2point.get((itemID) + "");
		int[] hashItem = hasher.hash(itemP.values);
		double distance = euclid(hashUser, hashItem);
		return (float) distance2rating(distance);
	}

	private Float getPreferenceValuePoint(long userID, long itemID) {
		Point userP = userDB.id2point.get((userID) + "");
		Point itemP = itemDB.id2point.get((itemID) + "");
		double distance = euclidD(itemP.values, userP.values);
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

	private PreferenceArray getPreferencesFromUserCorner(long userID)
	throws NoSuchUserException {
		int prefIndex = 0;
		Point p = userDB.id2point.get((userID) + "");
		if (null == p)
			throw new NoSuchUserException();
		int[] hashes = hasher.hash(p.values);
		Corner main = new Corner(hashes.clone());
		Set<Corner> all = cg.getHashSet(p);
		int count = 0;
		for(Corner c: all) {
			Set<String> items = itemDB.corner2ids.get(c);
			if (null != items) {
				count += items.size();
			}
		}
		PreferenceArray prefs = new GenericUserPreferenceArray(count);
		for(Corner c: all) {
			Set<String> items = itemDB.corner2ids.get(c);
			if (null != items) {
				float dist = (float) distance2rating(euclid(main.hashes, c.hashes));
				for(String itemID: items) {
					prefs.setUserID(prefIndex, userID);
					prefs.setItemID(prefIndex, Long.parseLong(itemID));
					prefs.setValue(prefIndex, dist);
				}
				prefIndex++;
			}
		}
		prefs.sortByItem();
		return prefs;
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
			float dist = (float) distance2rating(euclidD(up.values, ip.values));
			prefs.setUserID(prefIndex, userID);
			prefs.setItemID(prefIndex, Long.parseLong(item));
			prefs.setValue(prefIndex, dist);
			prefIndex++;
		}
		prefs.sortByItem();
		return prefs;
	}
	
//	??? hash distance. really f'ud
	double manhattan(int[] a, int[] b) {
		float sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return (sum / a.length) * varianceManhattan;
	}

	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum / a.length;
	}

	// hash distance - not correct!
	double euclid(int[] a, int[] b) {
		double sum = 0.0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		return Math.sqrt(sum) * varianceEuclid;
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
		double e = (1 - d) * scale + offset;
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
		SimplexTextDataModel model = new SimplexTextDataModel("/tmp/lsh_hadoop/GL_corners_100k/part-r-00000", null, null);
		model.hashCode();
	}

}
