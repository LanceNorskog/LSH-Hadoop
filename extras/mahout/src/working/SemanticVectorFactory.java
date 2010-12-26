package working;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

/*
 * Given a DataModel, create a semantic vector for a User or Item.
 * Vectors are always 0.0 -> 1.0, linear distribution
 * 
 * Semantic Vector formula: ((sum(U)+ sum(pref(u,i)/#U))/2)/#U
 */

public class SemanticVectorFactory {

  private DataModel model;
  private boolean doUsers;
  private Random rnd = new Random(0);

  public SemanticVectorFactory(DataModel model) {
    this.model = model;
    this.doUsers = doUsers;
  }

  /*
   * Create a Semantic Vector for this user with Item as independent variable
   */
  public Vector getUserVector(long userID, int dimensions) throws TasteException {
    FastIDSet prefs = model.getItemIDsFromUser(userID);
    int nItems = prefs.size();
    LongPrimitiveIterator itemList = prefs.iterator();
    double[] values = new double[dimensions];
    Vector v = new DenseVector(values);
    float minPreference = model.getMinPreference();
    float maxPreference = model.getMaxPreference();
    float prefSum = 0f;
    while(itemList.hasNext()) {
      long itemID = itemList.next();
      float pref = model.getPreferenceValue(userID, itemID);
      pref = ((pref - minPreference) /(maxPreference - minPreference));
      prefSum += pref;
    }
    for(int i = 0; i < dimensions; i++) {
      float rndSum = 0f;
      for(int j = 0; j < nItems; j++) {
        rndSum += rnd.nextDouble();
      }
      float position = ((rndSum + prefSum)/2)/nItems;
      values[i] = position;
      System.out.println(i + ": " + position);
    }
    return v;
  }

  /*
   * Create a Semantic Vector for this item with User as independent variable
   */
  public Vector getItemVector(long itemID) {

    return null;
  }

  /*
   * add 
   */
  private void addPreference(Float preferenceValue, Random rnd, CompactRunningAverage avg) {
    // TODO Auto-generated method stub

  }









  /**
   * @param args
   * @throws IOException 
   * @throws TasteException 
   */
  public static void main(String[] args) throws IOException, TasteException {
    DataModel model = new GroupLensDataModel(new File("/tmp/lsh_hadoop/GL_10k/ratings.dat"));

    SemanticVectorFactory svf = new SemanticVectorFactory(model);
    Vector v = svf.getUserVector(100, 100);
    
  }

}
