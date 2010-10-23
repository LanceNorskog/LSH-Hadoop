package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Lookup;
import lsh.core.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
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
	final int dimensions;
	// scaling for Manhattan and Euclidean distances
	final double varianceManhattan;
	final double varianceEuclid;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;
	// mean of point distances
	final double manhattanMean = 0.33;
	final double euclidMean = 0.4;
	
	public double total = 0;
	public int count = 0;
	public int clamped = 0;
	
	public long[] buckets = new long[10];

	public PointTextDataModel(String pointsPath) throws IOException {
		this(new File(pointsPath));
	}
	
	public PointTextDataModel(File pointsFile) {
		userDB = new Lookup(null, true, false, true, true, false, false);
		itemDB = new Lookup(null, true, false, true, true, false, false);
		try {
			FileReader itemReader = new FileReader(pointsFile);
			FileReader userReader = new FileReader(pointsFile);
			itemDB.loadPoints(itemReader, "I");
			itemReader.close();
			userDB.loadPoints(userReader, "U");
			userReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// yow!
		Iterator<Point> it = userDB.points.iterator();
//		dimensions = it.next().values.length / 2;
		dimensions = 1;
//		double[] zero = new double[dimensions];
//		for(int i = 0; i < zero.length; i++)
//			zero[i] = 0.0;
//		double[] unit = new double[dimensions];
//		for(int i = 0; i < unit.length; i++)
//			unit[i] = 1.0;
		varianceManhattan = (1.0/(dimensions));
		varianceEuclid = 1.0/Math.sqrt(dimensions);
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return new LPI(itemDB.ids.iterator());
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		int nitems = getNumItems();
		FastIDSet fids = new FastIDSet(nitems);
		for(int i = 0; i < nitems; i++) {
			fids.add(i);
		}
		return fids;
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
		total += distance;
		count++;
		double rating = distance2rating(distance);
		return (float) rating;
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
		prefs.sortByValueReversed();
		return prefs;
	}

	// rectangular distance
	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < dimensions; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		double r = sum * varianceManhattan;
//		r = invert(r, dimensions*2);
		return r;
	}

	// euclidean distance
	double euclidD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < dimensions; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		double dist = Math.sqrt(sum);
		return dist * varianceEuclid;
	}

	double distance2rating(double d) {
		double spread = 2.5;
		
		if (d < 0.0 || d > 1.0d) {
			d = Math.max(0.01, d);
			d = Math.min(0.99, d);
			clamped ++;
			this.hashCode();
		}
//		d = invert(d, dimensions);
		buckets[(int) (d * buckets.length)]++;
		double e;
		e = (1-d);
		e = e * scale;
		e = Math.max(0, e);
		e = Math.min(scale, e);
		e = e + offset;
		return e;
	}
	
	/*
	 * Correct the effects of adding N samples
	 * Adding N samples makes the random distribution pyramidal - this flattens it.
	 */
	private double invert(double r, double n) {
//		double canon = Math.abs(r - 0.5d);
//		double compressed = (0.5 - canon)/n;
////		compressed = (0.5 - compressed);
//		double inverted;
//		if (r > 0.5)
//			inverted = r + compressed;
//		else 
//			inverted = r - compressed;
//		return inverted;
//		double canon = r - 0.5d;
//		double height = (0.5 - Math.abs(canon));
//		double compressed = height/n;
//		compressed = (0.5 - compressed);
//		if (canon < 0)
//			compressed = -compressed;
//		double inverted = (r + compressed);
//		return inverted;
//		double canon = r - 0.5d;
//		double height = canon;
//		double compressed = height/n;
//		double decompressed = (canon - compressed);
//		double inverted = 0.5 - (decompressed);
//		return inverted;
//		double e = r * 2;
//		return Math.min(r, 0.99999);
//		n = n /2;
		double canon;
		double basis = ( manhattanMean);
		double midpointLeft = basis/2.0;
		double midpointRight = basis + (1 - basis)/2.0;
		double inverted;
		double compress = n/(n+1); 
		if (r > basis) {
			canon = (r - basis);
//			if (canon < midpointRight)
//				inverted = midpointLeft + canon/2;
//			else
//				inverted = midpointLeft + canon/2;
			inverted = basis + canon + (canon * compress);
		}
		else { 
			canon = (basis - r);
//			if (canon < midpointLeft)
//				inverted = midpointLeft + canon/2;
//			else
//				inverted = midpointRight/2 + canon;
			inverted = (basis - canon)*compress;
		}
		if (inverted < 0) 
			this.hashCode();
		return inverted;
}

	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return new LPI(userDB.ids.iterator());
	}

	@Override
	public boolean hasPreferenceValues() {
		return true;
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
	 * @throws TasteException 
	 */
	public static void main(String[] args) throws IOException, TasteException {
		PointTextDataModel model = new PointTextDataModel(args[0]);
		model.hashCode();
		System.out.println("pow: " + Math.pow(1.01, 1/100.0));
		System.out.println("Items");
		doscan(model.itemDB, model.dimensions);
		System.out.println("Users");
		doscan(model.userDB, model.dimensions);
		dodistances(model);
	}

 static void dodistances(PointTextDataModel model) throws TasteException {
		for(String userID: model.userDB.ids) {
			for(String itemID: model.itemDB.ids) {
				long u = Long.parseLong(userID);
				long id = Long.parseLong(itemID);
				model.getPreferenceValue(u, id);
			}
		}
		System.out.println("clamped: " + model.clamped);
		System.out.println("total:   " + model.total);
		System.out.println("count:   " + model.count);
		System.out.println("mean:    " + (model.total / model.count));
		System.out.println("buckets: " + (Arrays.toString(model.buckets)));
	}

	static void doscan(Lookup points, int dimensions) {
		double min = 100000;
		double max = -100000;
		double sum = 0;
		for(Point p: points.points) {
			double[] values = p.values;
			for(int i = 0; i < dimensions;i++) {
				min = Math.min(values[i], min);
				max = Math.max(values[i], max);
				sum += values[i];
			}
		}
		System.out.println("min: " + min + ", max: " + max + ", mean: " + sum / (points.points.size() * dimensions));
		
	}
	
}
