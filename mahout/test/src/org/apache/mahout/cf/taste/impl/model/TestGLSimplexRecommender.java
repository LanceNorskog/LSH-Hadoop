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
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommender;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensRecommenderBuilder;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * @author lance
 * 
 */
public class TestGLSimplexRecommender {

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
    GLSimplexRecommenderBuilder.init(args[1]);
    GLSimplexRecommenderBuilder recb = new GLSimplexRecommenderBuilder();
    Recommender rec = recb.buildRecommender(null);
    //		LongPrimitiveIterator lpi = printUsers(rec);
    //		lpi = printItems(rec);
    //		printAllRecommendations(rec);
    runEvaluator(rec, args);
  }

  private static void runEvaluator(Recommender rec, String[] args) throws TasteException, IOException {
    //		AverageAbsoluteDifferenceRecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
    //		AverageAbsoluteDifferenceRecommenderEvaluatorDual evaluatorDual = new AverageAbsoluteDifferenceRecommenderEvaluatorDual();
    //	    DataModel model = GLSimplexRecommenderBuilder.recommender.model;
    GLSimplexRecommenderBuilder builder = new GLSimplexRecommenderBuilder();
    LSHTextDataModel model = GLSimplexRecommenderBuilder.recommender.model;
    DataModel realModel = new GroupLensDataModel(new File(args[0]));
    GroupLensRecommender realRecco = new GroupLensRecommender(realModel);
    //	    System.out.println("GroupLensRecommender v.s. GroupLens data");
    //	    double evaluation;
    //	    evaluation = evaluator.evaluate(new GroupLensRecommenderBuilder(),
    //	    		null,
    //	    		realModel,
    //	    		0.9,
    //	    		0.3);
    //	    System.out.println("GroupLensRecommender v.s. Simplex data");
    //	    evaluation = evaluator.evaluate(new GroupLensRecommenderBuilder(),
    //	    		null,
    //	    		model,
    //	    		0.9,
    //	    		0.3);
    //	    System.out.println("SimplexRecommender v.s. GroupLens data");
    //	    evaluation = evaluator.evaluate(new GLSimplexRecommenderBuilder(),
    //	    		null,
    //	    		realModel,
    //	    		0.9,
    //	    		0.3);
    //	    System.out.println("SimplexRecommender v.s. Simplex data");
    //	    evaluation = evaluatorDual.evaluateDual(new GLSimplexRecommenderBuilder(),
    //	      model,
    //	      realModel);
    System.out.println("SimplexRecommder v.s. GroupLensRecommender (SlopeOne)");
    evaluateN2(builder.buildRecommender(null), model, realRecco, realModel);
  }

  private static void evaluateN2(Recommender recommender, LSHTextDataModel model, 
      GroupLensRecommender realRecco,
      DataModel realModel) throws TasteException {
    double sum = 0.0;
    double sumRMS = 0.0;
    int count = 0;
    for(String userIDstr: model.userDB.ids) {
      Long userID = Long.parseLong(userIDstr);
      PreferenceArray prefs = model.getPreferencesFromUser(userID);
      Iterator<Preference> it = prefs.iterator();
      while(it.hasNext()) {
        Preference pref = it.next();
        Long itemID = pref.getItemID();
        float value = pref.getValue();
        if (value != Float.NaN) {
          try {
            Float realPref = realRecco.estimatePreference(userID, itemID);
            Float delta = Math.abs(realPref - value);
            if (delta < Float.MAX_VALUE && delta > Float.MIN_VALUE) {
              sum += delta;
              sumRMS += delta * delta;
              count++;
            }

          } catch (TasteException e) {
            ;
          }
        }
      }

    }
    System.out.println("Total prefs: " + count + " (possible: " + (model.userDB.ids.size() * model.itemDB.ids.size()) + ")");
    System.out.println("Average delta: " + (sum / count));
    System.out.println("RMS     delta: " + Math.sqrt((sum / count)));
  }



  //	private static void evaluateN2(Recommender recommender, SimplexSVTextDataModel model, 
  //			GroupLensRecommender realRecco,
  //			DataModel realModel) throws TasteException {
  //		double sum = 0.0;
  //		int count = 0;
  //		for(String userIDstr: model.userDB.ids) {
  //			Long userID = Long.parseLong(userIDstr);
  //			for(String itemIDstr: model.itemDB.ids) {
  //				Long itemID = Long.parseLong(itemIDstr);
  //				try {
  //				float pref = recommender.estimatePreference(userID, itemID);
  //				if (pref != Float.NaN) {
  //					Float realPref = realRecco.estimatePreference(userID, itemID);
  //					if (realPref != Float.NaN) {
  //						count++;
  //						sum += Math.abs(realPref - pref);
  //					}
  //				}
  //				} catch (TasteException e) {
  //					;
  //				}
  //			}
  //		}
  //		System.out.println("Total prefs: " + count + " (possible: " + (model.userDB.ids.size() * model.itemDB.ids.size()) + ")");
  //		System.out.println("Average delta: " + (sum / count));
  //	}

  private LSHRecommender buildRecommender(String[] args)
  throws InstantiationException, IllegalAccessException,
  ClassNotFoundException, IOException {
    Properties props = new Properties();
    props.setProperty(LSHDriver.HASHER, "lsh.core.VertexTransitiveHasher");
    props.setProperty(LSHDriver.DIMENSION, "100");
    props.setProperty(LSHDriver.GRIDSIZE, "0.6");
    LSHRecommender rec = new LSHRecommender(props, args[0]);
    return rec;
  }

  private static void printAllRecommendations(LSHRecommender rec)
  throws TasteException {
    LongPrimitiveIterator it = rec.model.getUserIDs();
    while (it.hasNext()) {
      long user = it.nextLong();

      System.out.println("Items recco'd for user: " + user);
      List<RecommendedItem> recs = rec.recommend(user, 3);
      if (null != recs) {
        for (RecommendedItem recco : recs) {
          recco.hashCode();
          System.out.println("\t" + recco.getItemID() + "\t"
              + recco.getValue());
        }
      }
    }
  }

  private static LongPrimitiveIterator printItems(LSHRecommender rec)
  throws TasteException {
    LongPrimitiveIterator lpi;
    System.out.println("Item IDs:");
    lpi = rec.model.getItemIDs();
    while (lpi.hasNext()) {
      System.out.println("\t" + lpi.next());
    }
    return lpi;
  }

  private static LongPrimitiveIterator printUsers(LSHRecommender rec)
  throws TasteException {
    LongPrimitiveIterator lpi = rec.model.getUserIDs();
    System.out.println("User IDs:");
    while (lpi.hasNext()) {
      System.out.println("\t" + lpi.next());
    }
    return lpi;
  }

}


