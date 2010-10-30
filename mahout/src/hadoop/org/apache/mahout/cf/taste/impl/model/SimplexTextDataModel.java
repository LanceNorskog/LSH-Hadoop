package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.Lookup;
import lsh.core.Point;
import lsh.core.Utils;
import lsh.core.VertexTransitiveHasher;

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
//	final Hasher hasher;
	// corner-generator - does LSH projection
	final CornerGen cg;
	// real 1.0 = hash(1.0) * variance
	double varianceEuclid;
	double varianceManhattan;
	// real 1.0 dist 
	final double diagonal;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;
	
	boolean doPoints = false;

	public SimplexTextDataModel(String cornersFile, CornerGen cg) throws IOException {
//		this.hasher = hasher;
		this.cg = cg;
		userDB = new Lookup(null, false, false, false, false, true, false, false, false);
		itemDB = new Lookup(null, false, false, false, false, false, true, false, false);
		Reader f;
		f = new FileReader(new File(cornersFile));
//		itemDB.loadCP(f, "I");
//		f.close();
//		f = new FileReader(new File(cornersFile));
//		userDB.loadCP(f, "U");
		Utils.load_corner_points_format(f, "I", itemDB, "U", userDB);
		f.close();
		int dimension = cg.stretch.length;
//		if (null != cg.hasher) {
//			double[] unit = new double[dimension];
//			for(int i = 0; i < unit.length; i++)
//				unit[i] = 1.0;
//			double[] projected = new double[dimension];
//			cg.hasher.project(unit, projected);
//			varianceEuclid = 1.0;
//			double dist = euclidD(unit, projected);
//			varianceEuclid = 1.0 / dist;
//		} else
//			varianceEuclid = Double.NaN;

		double[] zero = new double[dimension];
		for(int i = 0; i < zero.length; i++)
			zero[i] = 0.0;
		double[] unit = new double[dimension];
		for(int i = 0; i < unit.length; i++)
			unit[i] = 1.0;
//		varianceEuclid = 1.0;
		int[] zeroHash = cg.hasher.hash(zero);
		int[] unitHash = cg.hasher.hash(unit);
//		varianceEuclid = 1/(Math.sqrt(manhattan(zeroHash, unitHash))*1.5);
		varianceEuclid = manhattan(zeroHash,unitHash);
		varianceEuclid = Math.sqrt(2)/varianceEuclid;
		double dist = manhattanD(zero, unit);
		varianceManhattan = 1/dist;
		varianceManhattan = 1.0/dimension;


		diagonal = 1/Math.sqrt(dimension);
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return new LPI(itemDB.id2corner.keySet().iterator());
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumItems() throws TasteException {
		return itemDB.id2corner.keySet().size();
	}

	@Override
	public int getNumUsers() throws TasteException {
		return userDB.id2corner.keySet().size();
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
		return doPoints ? getPreferenceValuePoint(userID, itemID) : getPreferenceValueCorner(userID, itemID);
	}

	private Float getPreferenceValueCorner(long userID, long itemID)
	throws TasteException {
//		Point userP = userDB.id2point.get((userID) + "");
//		int[] hashUser = hasher.hash(userP.values);
//		Point itemP = itemDB.id2point.get((itemID) + "");
//		int[] hashItem = hasher.hash(itemP.values);
//		double man = manhattanD(userP.values, itemP.values);
//		double distance = manhattan(hashUser, hashItem);
//		return (float) distance2rating(man);
		Corner user = userDB.id2corner.get(userID + "");
		Corner item = itemDB.id2corner.get(itemID + "");
		double distance = manhattan(user.hashes, item.hashes);
		return (float) distance2rating(distance);
	}

	private Float getPreferenceValuePoint(long userID, long itemID) {
//		Point userP = userDB.id2point.get((userID) + "");
//		Point itemP = itemDB.id2point.get((itemID) + "");
//		double distance = manhattanD(itemP.values, userP.values);
//		return (float) distance2rating(distance);
		return 0.0f;
	}

	@Override
	public PreferenceArray getPreferencesForItem(long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PreferenceArray getPreferencesFromUser(long userID)
	throws TasteException {
		return doPoints ? getPreferencesFromUserPoint(userID) : getPreferencesFromUserCorner(userID);
	}

	private PreferenceArray getPreferencesFromUserCorner(long userID)
	throws NoSuchUserException {
		// XX use id2corner
//		Point p = userDB.id2point.get((userID) + "");
//		if (null == p)
//			throw new NoSuchUserException();
//		int[] hashes = cg.hasher.hash(p.values);
		Corner main = userDB.id2corner.get((userID) + "");
		Set<Corner> all = cg.getHashSet(main.hashes.clone());
		int count = 0;
		for(Corner c: all) {
			Set<String> items = itemDB.corner2ids.get(c);
			if (null != items) {
				count += items.size();
			}
		}
		int prefIndex = 0;
		PreferenceArray prefs = new GenericUserPreferenceArray(count);
		for(Corner c: all) {
			Set<String> items = itemDB.corner2ids.get(c);
			if (null != items) {
				float dist = (float) distance2rating(manhattan(main.hashes, c.hashes));
				for(String itemID: items) {
					prefs.setUserID(prefIndex, userID);
					prefs.setItemID(prefIndex, Long.parseLong(itemID));
					prefs.setValue(prefIndex, dist);
				}
				prefIndex++;
			}
		}
		prefs.sortByValueReversed();
		return prefs;
	}

	private PreferenceArray getPreferencesFromUserPoint(long userID)
	throws NoSuchUserException {
//		int prefIndex = 0;
//		Point up = userDB.id2point.get((userID) + "");
//		if (null == up)
//			throw new NoSuchUserException();
//		PreferenceArray prefs = new GenericUserPreferenceArray(itemDB.ids.size());
//		for(String item: itemDB.ids) {
//			Point ip = itemDB.id2point.get(item);
//			float dist = (float) distance2rating(manhattanD(up.values, ip.values));
//			prefs.setUserID(prefIndex, userID);
//			prefs.setItemID(prefIndex, Long.parseLong(item));
//			prefs.setValue(prefIndex, dist);
//			prefIndex++;
//		}
//		prefs.sortByItem();
//		return prefs;
		return null;
	}

	//	??? hash distance. really f'ud
	double manhattan(int[] a, int[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		// 0 times varianceEuclid is NaN !
		return (sum < 0.0000001) ? 0 : sum * varianceEuclid;
	}

	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum * Math.sqrt(2) * varianceManhattan;
	}

	// hash distance - not correct!
//	double euclid(int[] a, int[] b) {
//		double sum = 0.0;
//		for(int i = 0; i < a.length; i++) {
//			sum += (a[i] - b[i]) * (a[i] - b[i]);
//		}			
//		return Math.sqrt(sum) * varianceEuclid;
//	}

	// rectangular distance
	double euclidD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		return Math.sqrt(sum) * diagonal;
	}

	double distance2rating(double d) {
		if (d < 0)
			d = 0.0;
		if (d > 1) 
			d = 1.0;
		double e = (1-d) * scale + offset;
		return e;
	}
	
	/*
	double distance2rating(double d) {
		double spread = 2.5;
		if (d < 0.1d || d > 0.9d) {
			this.hashCode();
		}
//		d = invert(d, 2*dimensions);
		double e;
		double expand = scale * spread;
		e = (1-d);
		e = e * expand;
		e = e - Math.sqrt(expand / spread);
		e = Math.max(0, e);
		e = Math.min(scale, e);
		e = e + offset;
		return e;
	}
	*/


	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return new LPI(userDB.id2corner.keySet().iterator());
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
		VertexTransitiveHasher hasher = new VertexTransitiveHasher(50, 0.70);
		CornerGen cg = new CornerGen(hasher, hasher.stretch);
		SimplexTextDataModel model = new SimplexTextDataModel("/tmp/lsh_hadoop/shortU.txt", cg);
		model.hashCode();
	}

}
