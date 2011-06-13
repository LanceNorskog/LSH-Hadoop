/**
 * 
 */
package lsh.mahout.recommnder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.math.simplex.SimplexFactory;

/**
 * @author lance
 * 
 * Redone for new Simplex Vector stuff: no knowledge of internals
 *
 */
public class SimplexRecommender implements Recommender {
  List<RecommendedItem> NORECS = Collections.emptyList();
  final SimplexTextDataModel model;

  public SimplexRecommender(Properties props, String dataFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    model = createDataModel(props, dataFile);
  }

  public static SimplexTextDataModel createDataModel(Properties props, String dataFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    SimplexFactory sf;
    String hasherClass = props.getProperty(LSHDriver.HASHER);
    double gridsize = Double.parseDouble(props.getProperty(LSHDriver.GRIDSIZE));
    int dimensions = Integer.parseInt(props.getProperty(LSHDriver.DIMENSION));

    sf = (SimplexFactory) Class.forName(hasherClass).newInstance();
    double[] stretch;
    stretch = new double[dimensions];
    for(int i = 0; i < stretch.length; i++) {
      stretch[i] = gridsize;
    }
    sf.setStretch(stretch);
    CornerGen cg = new CornerGen(sf, stretch);
    return new SimplexTextDataModel(dataFile, cg);
  }


  /* (non-Javadoc)
   * @see org.apache.mahout.cf.taste.recommender.Recommender#estimatePreference(long, long)
   */
  @Override
  public float estimatePreference(long userID, long itemID)
  throws TasteException {
    return model.getPreferenceValue(userID, itemID);
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
   * 
   * TODO: push this down into datamodel as much as possible
   */
  //	@Override
  //	public List<RecommendedItem> recommend(long userID, int howMany)
  //	throws TasteException {
  //		List<RecommendedItem> recs = new ArrayList<RecommendedItem>(howMany);
  //		Point p = model.userDB.id2point.get(((Long) userID).toString());
  //		if (null == p) 
  //			return NORECS;
  //		int[] hashes = model.cg.hasher.hash(p.values);
  //		Corner main = new Corner(hashes);
  //		getRecommendations(howMany, recs, main, main);
  //		Corner main = model.userDB.id2corner.get(((Long) userID).toString());
  //		Set<Corner> all = model.cg.getHashSet(main.hashes.clone());
  //		for(Corner c: all) {
  //			getRecommendationsHash(howMany, recs, main, c);
  //		}
  //		getRecommendationsRecurse(howMany, recs, main, new Corner(main.hashes.clone()));
  //		return recs;
  //	}

  // This collects recommendations by stepping away from the userID's corner in the item space.
  // Only check the given cell and surrounding cells
  @Override
  public List<RecommendedItem> recommend(long userID, int howMany)
  throws TasteException {

    Corner main = model.userDB.id2corner.get(((Long) userID).toString());

    List<RecommendedItem> recs = new ArrayList<RecommendedItem>();
    getRecommendationsAll(howMany, recs, main);
    return recs;
  }

  private void getRecommendationsAll(int howMany,
      List<RecommendedItem> recs, Corner main) {
    for(String item: model.itemDB.id2corner.keySet()) {
      Corner c = model.itemDB.id2corner.get(item);
      getRecommendations(howMany, recs, main, c);
    }
  }

  // Items in this corner and neighboring corners- limits returns to gridsize
  private void getRecommendationsCluster(int howMany,
      List<RecommendedItem> recs, Corner main) {
    Set<Corner> all = model.cg.getHashSet(main.hashes.clone());
    for(Corner c: all) {
      getRecommendations(howMany, recs, main, c);
    }
  }

  // add all items in this corner, use insertion sort
  private void getRecommendations(int howMany, List<RecommendedItem> recs,
      Corner main, Corner c) {
    Set<String> ids = model.itemDB.corner2ids.get(c);
    if (null != ids) {
      float rating = (float) (model.distance2rating(model.manhattan(main.hashes, c.hashes)));
      for(String id: ids) {
        if (recs.size() == howMany)
          break;
        RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), rating);
        if (recs.size() == 0) {
          recs.add(recco);
          continue;
        }
        if (recs.contains(recco)) {
          continue;
        }
        int j = recs.size() - 1;
        if (rating <= recs.get(j).getValue()) {
          recs.add(recco);
          continue;
        }
        for(; j >= 0; j--) {
          if (rating <= recs.get(j).getValue()) {
            recs.add(j, recco);
            break;
          }
        }
      }
    }
  }

  //	private void getRecommendationsBreadthFirst(int howMany, List<RecommendedItem> recs, Corner main) {
  //		int count = 0;
  //		Set<Corner> found = new HashSet<Corner>();
  //		Queue<Corner> q = new LinkedList<Corner>();
  //		q.offer(main);
  //		while (!q.isEmpty()) {
  //			Corner center = q.remove();
  //			count++;
  //			found.add(center);
  //			if (addCorners(main, center, howMany, recs))
  //				return;
  //			try {
  //			Set<Corner> all = model.cg.getHashSet(center.hashes.clone());
  //			for(Corner c: all) {
  //				// add next-most outer step
  //				if (! found.contains(c)) {
  //					q.offer(c);
  //				}
  //			}
  //			} catch (OutOfMemoryError e) {
  //				e.hashCode();
  //			}
  //		}
  //
  //	}
  //	
  //	private boolean addCorners(Corner main, Corner center, int howMany, List<RecommendedItem> recs) {
  //		Set<String> ids = model.itemDB.corner2ids.get(center);
  //		if (null != ids) {
  //			float rating = (float) (model.distance2rating(model.manhattan(main.hashes, center.hashes)));
  //			for(String id: ids) {
  //				RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), rating);
  //				recs.add(recco);
  //				if (recs.size() == howMany)
  //					return true;
  //			}
  //		}
  //		return false;
  //	}

  // walk permuted neighbor lists outwards until get requested # of recommendations
  //	private void getRecommendationsRecurse(int howMany, List<RecommendedItem> recs, Set<Corner> found, Corner main, Corner center) {
  //		if (recs.size() == howMany)
  //			return;
  //		if (found.contains(center))
  //			return;
  //		Set<String> ids = model.itemDB.corner2ids.get(center);
  //		if (null != ids) {
  //			float rating = (float) (model.distance2rating(model.manhattan(main.hashes, center.hashes)));
  //			for(String id: ids) {
  //				RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), rating);
  //				recs.add(recco);
  //				if (recs.size() == howMany)
  //					return;
  //			}
  //		}
  //		found.add(center);
  //		Set<Corner> all = model.cg.getHashSet(center.hashes.clone());
  //		Queue q = new LinkedList<Corner>();
  //		for(Corner c: all) {
  //			if (! found.contains(c)) {
  //				getRecommendationsRecurse(howMany, recs, found, main, c);
  //			}
  //		}
  //
  //	}

  //	private void getRecommendationsHash(int howMany, List<RecommendedItem> recs,
  //			Corner main, Corner c) {
  //		Set<String> ids = model.itemDB.corner2ids.get(c);
  //		if (null != ids) {
  //			for(String id: ids) {
  //				float rating = (float) (model.distance2rating(model.manhattan(main.hashes, c.hashes)));
  //				if (rating == Float.NaN)
  //					rating = 1.0f;
  //				RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), rating);
  //				int j = 0;
  //				for(; j < recs.size(); j++) {
  //					if (rating < recs.get(j).getValue()) {
  //						if (!recs.contains(recco)) {
  //							recs.add(j, recco);
  //						}
  //						break;
  //					}
  //				}
  //				if (j == recs.size() && !recs.contains(recco))
  //					recs.add(recco);
  //				if (recs.size() == howMany)
  //					break;
  //			}
  //		}
  //	}
  //
  //	private void getRecommendations(int howMany, List<RecommendedItem> recs,
  //			Corner main, Corner c) {
  //		Set<String> ids = model.itemDB.corner2ids.get(c);
  //		if (null != ids) {
  //			for(String id: ids) {
  //				float rating = (float) (model.distance2rating(model.manhattan(main.hashes, c.hashes)));
  //				RecommendedItem recco = new GenericRecommendedItem(Long.parseLong(id), rating);
  //				int j = 0;
  //				for(; j < recs.size(); j++) {
  //					if (rating < recs.get(j).getValue()) {
  //						if (!recs.contains(recco)) {
  //							recs.add(j, recco);
  //						}
  //						break;
  //					}
  //				}
  //				if (j == recs.size() && !recs.contains(recco))
  //					recs.add(recco);
  //				if (recs.size() == howMany)
  //					break;
  //			}
  //		}
  //	}

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
    props.setProperty(LSHDriver.DIMENSION, "150");
    props.setProperty(LSHDriver.GRIDSIZE, "1.0");
    SimplexRecommender rec = new SimplexRecommender(props, args[0]);
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
      List<RecommendedItem> recs = rec.recommend(user, 30);
      if (null != recs) {
        for(RecommendedItem recco: recs) {
          recco.hashCode();
          System.out.println("\t" + recco.getItemID() + "\t" + recco.getValue());
        }
      }
    }
  }

}
