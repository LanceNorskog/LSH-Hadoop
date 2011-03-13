package org.apache.mahout.cf.taste.impl.model;

import java.io.IOException;
import java.util.Properties;

import lsh.hadoop.LSHDriver;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

public class GLSimplexRecommenderBuilder implements RecommenderBuilder {
  public static SimplexRecommender recommender = null;
  public static String HASHER = "lsh.core.VertexTransitiveHasher";
  public static String DIMENSION = "100";
  public static String GRIDSIZE = "0.55";

  public static void init(String fileName) {
    recommender = createSingleton(fileName);
  }

  @Override
  public Recommender buildRecommender(DataModel dataModel)
  throws TasteException {
    if (null == recommender)
      throw new Error("Call builder.init first");
    return recommender;
  }


  private static SimplexRecommender createSingleton(String fileName) {
    Properties props = new Properties();
    props.setProperty(LSHDriver.HASHER, "lsh.core.VertexTransitiveHasher");
    props.setProperty(LSHDriver.DIMENSION, "150");
    props.setProperty(LSHDriver.GRIDSIZE, "0.70");
    try {
      return new SimplexRecommender(props, fileName);
    } catch (Exception e) {
      return null;
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
