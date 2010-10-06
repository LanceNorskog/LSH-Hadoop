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
	final int dimensions;
	// scaling for Manhattan and Euclidean distances
	final double varianceManhattan;
	final double varianceEuclid;
	// project ratings from 0->1 into 1...5
	final double scale = 4;
	final double offset = 1;
	
	double total = 0;
	int count = 0;

	public PointTextDataModel(String pointsFile) throws IOException {
		userDB = new Lookup(null, true, false, true, true, false, false);
		itemDB = new Lookup(null, true, false, true, true, false, false);
		Reader f;
		f = new FileReader(new File(pointsFile));
		itemDB.loadPoints(f, "I");
		f.close();
		f = new FileReader(new File(pointsFile));
		userDB.loadPoints(f, "U");
		f.close();
		// yow!
		Iterator<Point> it = userDB.points.iterator();
		dimensions = it.next().values.length;
		double[] zero = new double[dimensions];
		for(int i = 0; i < zero.length; i++)
			zero[i] = 0.0;
		double[] unit = new double[dimensions];
		for(int i = 0; i < unit.length; i++)
			unit[i] = 1.0;
		varianceManhattan = Math.sqrt(2) * 1.0/dimensions;
		varianceEuclid = 1.0/Math.sqrt(dimensions);
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
		total += distance;
		count++;
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

	// rectangular distance
	double manhattanD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum * varianceManhattan;
	}

	// euclidean distance
	double euclidD(double[] a, double[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += (a[i] - b[i]) * (a[i] - b[i]);
		}			
		double dist = Math.sqrt(sum);
		return dist * varianceEuclid;
	}

	double distance2rating(double d) {
		if (d < 0.1d || d > 0.9d) {
			this.hashCode();
		}
		d = invert(d, 2*dimensions);
		double e;
		e = (1-d);
		e = e * scale;
//		e = e * 6;
//		e = e - 12.4;
//		e = Math.max(0, e);
//		e = Math.min(4, e);
		e = e + offset;
		return e;
	}
	
	
	
//	// invert pyramid effect of additive random values
//	// point distances are compressed because the distances are N random numbers added
//	void invertPyramid(double[] in, double[] out) {
//		double sumIn = 0, sumOut = 0;
//		for (int i = 0; i < dimensions; i++) {
//			double r = in[i];
//			sumIn += r;
//			double inverted = invert(r);
//			out[i] = inverted;
//			total += inverted;
//			sumOut += inverted;
//			count++;
//		}
//		double avg = total / count;
//		double avgIn = sumIn / dimensions;
//		double avgOut = sumOut / dimensions;
//		this.hashCode();
//	}

	/*
	 * Correct the effects of adding N samples
	 * Adding N samples makes the random distribution pyramidal - this flattens it.
	 */
	private double invert(double r, double n) {
		double canon = Math.abs(r - 0.5d);
		double compressed = (0.5 - canon)/n;
//		compressed = (0.5 - compressed);
		double inverted;
		if (r > 0.5)
			inverted = r + compressed;
		else 
			inverted = r - compressed;
		return inverted;
//		double canon = r - 0.5d;
//		double height = (0.5 - Math.abs(canon));
//		double compressed = height/n;
//		compressed = (0.5 - compressed);
//		if (canon < 0)
//			compressed = -compressed;
//		double inverted = compressed + 0.5;
//		return inverted;
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
	 */
	public static void main(String[] args) throws IOException {
		PointTextDataModel model = new PointTextDataModel("/tmp/lsh_hadoop/GL_points_10k/part-r-00000");
		model.hashCode();
		System.out.println("Items");
		doscan(model.itemDB);
		System.out.println("Users");
		doscan(model.userDB);
	}

	static void doscan(Lookup points) {
		double min = 100000;
		double max = -100000;
		double sum = 0;
		int dimension = 0;
		for(Point p: points.points) {
			double[] values = p.values;
			dimension = values.length;
			for(int i = 0; i < values.length;i++) {
				min = Math.min(values[i], min);
				max = Math.max(values[i], max);
				sum += values[i];
			}
		}
		System.out.println("min: " + min + ", max: " + max + ", mean: " + sum / (points.points.size() * dimension));
		
	}
	
}
