/**
 * 
 */
package org.apache.mahout.cf.taste.impl.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.Point;
import lsh.core.VertexTransitiveHasher;
import lsh.hadoop.LSHDriver;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * @author lance
 *
 */
public class GLSimplexRecommender implements Recommender {
	List<RecommendedItem> NORECS = Collections.emptyList();
	final SimplexSVTextDataModel model;
	final Distance distance;

	public GLSimplexRecommender(Properties props, String dataFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		model = createDataModel(props, dataFile);
		distance = new Distance();
	}

	public static SimplexSVTextDataModel createDataModel(Properties props, String dataFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		Hasher hasher;
		String hasherClass = props.getProperty(LSHDriver.HASHER);
		double gridsize = Double.parseDouble(props.getProperty(LSHDriver.GRIDSIZE));
		int dimensions = Integer.parseInt(props.getProperty(LSHDriver.DIMENSION));

		hasher = (Hasher) Class.forName(hasherClass).newInstance();
		double[] stretch;
		stretch = new double[dimensions];
		for(int i = 0; i < stretch.length; i++) {
			stretch[i] = gridsize;
		}
		hasher.setStretch(stretch);
		CornerGen cg = new CornerGen(hasher, stretch);
		return new SimplexSVTextDataModel(dataFile, hasher, cg);
	}


	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#estimatePreference(long, long)
	 */
	@Override
	public float estimatePreference(long userID, long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#getDataModel()
	 */
	@Override
	public DataModel getDataModel() {
		return model;
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#recommend(long, int)
	 * Return all in same and neighboring hashes.
	 */
	@Override
	public List<RecommendedItem> recommend(long userID, int howMany)
	throws TasteException {
		List<RecommendedItem> recs = new ArrayList<RecommendedItem>(howMany);
		Point p = model.userDB.id2point.get(((Long) userID).toString());
		if (null == p) 
			return NORECS;
		int[] hashes = model.hasher.hash(p.values);
		Corner main = new Corner(hashes);
		Set<Corner> all = model.cg.getHashSet(p);
		// usePoints(howMany, recs, p, c);
		for(Corner c: all) {
			Set<String> ids = model.itemDB.corner2ids.get(c);
			if (null != ids) {
				for(String id: ids) {
					float dist = hamming(main, c);
					RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), dist);
					int j = 0;
					for(; j < recs.size(); j++) {
						if (dist < recs.get(j).getValue()) {
							recs.add(j, recco);
							break;
						}
					}
					if (j == recs.size())
						recs.add(recco);
					if (recs.size() == howMany)
						break;
				}
			}
		}
		return recs;
	}

	private float hamming(Corner main, Corner c) {
		float sum = 0;
		for(int i = 0; i < main.hashes.length; i++) {
			sum += Math.abs(main.hashes[i] - c.hashes[i]);
		}
		return sum / main.hashes.length;
	}


	private void usePoints(int howMany, List<RecommendedItem> recs, Point p,
			Corner c) {
		Set<Point> points = model.itemDB.corner2points.get(c);
		if (null != points) {
			for(Point ip: points) {
				double dist = (float) distance.manhattan(p, ip);
				RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(ip.id), (float) dist);
				int j = 0;
				for(; j < recs.size(); j++) {
					if (dist < recs.get(j).getValue()) {
						recs.add(j, recco);
						break;
					}
				}
				if (j == recs.size())
					recs.add(recco);
				if (recs.size() == howMany)
					break;
				recco.hashCode();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#recommend(long, int, org.apache.mahout.cf.taste.recommender.IDRescorer)
	 */
	@Override
	public List<RecommendedItem> recommend(long userID, int howMany,
			IDRescorer rescorer) throws TasteException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#removePreference(long, long)
	 */
	@Override
	public void removePreference(long userID, long itemID)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.recommender.Recommender#setPreference(long, long, float)
	 */
	@Override
	public void setPreference(long userID, long itemID, float value)
	throws TasteException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.mahout.cf.taste.common.Refreshable#refresh(java.util.Collection)
	 */
	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TasteException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, TasteException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Properties props = new Properties();
		props.setProperty(LSHDriver.HASHER, "lsh.core.VertexTransitiveHasher");
		props.setProperty(LSHDriver.DIMENSION, "100");
		props.setProperty(LSHDriver.GRIDSIZE, "0.6");
		String file = args.length > 0 ? args[0] : "/tmp/lsh_hadoop/short.csv";
		GLSimplexRecommender rec = new GLSimplexRecommender(props, args[0]);
		//		LongPrimitiveIterator lpi = model.getUserIDs();
		//		System.out.println("User IDs:");
		//		while (lpi.hasNext()) {
		//			System.out.println("\t" + lpi.next());
		//		}
		//		lpi.hashCode();
		//		System.out.println("Item IDs:");
		//		lpi = model.getItemIDs();
		//		while (lpi.hasNext()) {
		//			System.out.println("\t" + lpi.next());
		//		}
		//		lpi.hashCode();

		LongPrimitiveIterator it = rec.model.getUserIDs();
		while(it.hasNext()) {
			long user = it.nextLong();

			System.out.println("Items recco'd for user: " + user);
			List<RecommendedItem> recs = rec.recommend(user, 3);
			if (null != recs) {
				for(RecommendedItem recco: recs) {
					recco.hashCode();
					System.out.println("\t" + recco.getItemID() + "\t" + recco.getValue());
				}
			}
		}
	}

}

class Distance {

	// manhattan because i'm lazy
	public double manhattan(Point a, Point b) {
		double dist = 0;
		for(int i = 0; i < a.values.length; i++) {
			dist += Math.abs(a.values[i] - b.values[i]);
		}
		return dist;
	}
}

