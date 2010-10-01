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
 */

public class SimplexSVTextDataModel extends AbstractDataModel {
	PreferenceArray NO_PREFS = new GenericUserPreferenceArray(0);
	// raw text corner-first LSH of users
	final Lookup userDB;
	// raw text corner-first LSH of items
	final Lookup itemDB;
	// LSH projector
	final Hasher hasher;
	// corner-generator - does LSH projection
	final CornerGen cg;
	// real 1.0 = hash(1.0) * variance
	final double variance;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;

	public SimplexSVTextDataModel(Hasher hasher) throws IOException {
		this("/tmp/lsh_hadoop/short.csv",  hasher, null);
	}

	public SimplexSVTextDataModel(String cornersFile, Hasher hasher, CornerGen cg) throws IOException {
		this.hasher = hasher;
		this.cg = cg;
		userDB = new Lookup(null, true, true, true, true, false, false);
		itemDB = new Lookup(null, true, true, true, true, false, false);
		Reader f;
		f = new FileReader(new File(cornersFile));
		itemDB.loadCP(f, "I");
		f.close();
		f = new FileReader(new File(cornersFile));
		userDB.loadCP(f, "U");
		f.close();
		if (null != hasher) {
			int dimension = cg.stretch.length;
			double[] raw = new double[dimension];
			for(int i = 0; i < raw.length; i++)
				raw[i] = 1.0;
			double[] projected = new double[dimension];
			hasher.project(raw, projected);
			double dist = manhattanD(raw, projected);
			variance = 1.0 / dist;
			System.out.println("Variance: " + variance);
		} else
			variance = 1.0;
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
		Point userP = userDB.id2point.get((userID) + "");
		int[] hashUser = hasher.hash(userP.values);
		Point itemP = itemDB.id2point.get((itemID) + "");
		int[] hashItem = hasher.hash(itemP.values);
		double distance = euclid(hashUser, hashItem);
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
		return prefs;
	}

	float manhattan(int[] a, int[] b) {
		float sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum / a.length;
	}

	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum / a.length;
	}

	double euclid(int[] a, int[] b) {
		//		double[] aP = new double[a.length];
		//		double[] bP = new double[a.length];
		//		hasher.unhash(a, aP);
		//		hasher.unhash(b, bP);
		double sum = 0.0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		return Math.sqrt(sum);
	}

	double euclidD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		return Math.sqrt(sum);
	}

	double distance2rating(double d) {
		double e = (1 - (d * variance)) * scale + offset;
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
		SimplexSVTextDataModel model = new SimplexSVTextDataModel("/tmp/lsh_hadoop/GL_corners_100k/part-r-00000", null, null);
		model.hashCode();
	}

}
