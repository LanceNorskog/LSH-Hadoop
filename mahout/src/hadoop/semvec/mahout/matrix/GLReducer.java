package semvec.mahout.matrix;

import java.io.IOException;
import java.util.Iterator;

import lsh.hadoop.LSHDriver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.RandomFactory;
import org.apache.mahout.math.RandomMatrix;
import org.apache.mahout.math.RandomVectorOld;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

/*
 * Input: Item=key, value = user, item, pref
 *  Values are 0->1
 * 
 * For a dimension, randomly project all users and items onto a line from 0 to 1.
 * For each user+preference, pull the item towards the user.
 * 
 * Where U=random position and U# = the number of user prefs,
 *       Item(i) =  ((sum(U)+ sum(pref(u,i)/#U))/2)/#U
 * 
 * Write projected Item vectors as SequenceFile.
 * 
 * TODO: Use SemanticVectorFactory?
 */


public class GLReducer extends
Reducer<LongWritable, MyTupleWritable, Text, Text> {

  private int dimension = -1;
  private int randomSeed = 0;
  private RandomFactory factory = null;

  @Override
  protected void setup(org.apache.hadoop.mapreduce.Reducer<LongWritable,MyTupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
    Configuration conf = context.getConfiguration();
    String d = conf.get(LSHDriver.DIMENSION);
    String r = conf.get(LSHDriver.RANDOMSEED);
    if (null == d)
      dimension = 2;
    else
      dimension = Integer.parseInt(d);
    if (null != r)
      randomSeed = Integer.parseInt(r);
    factory = new RandomFactory(randomSeed);
  }

  protected void reduce(
      LongWritable key,
      Iterable<MyTupleWritable> values,
      Reducer<LongWritable, MyTupleWritable, Text, Text>.Context context)
  throws java.io.IOException, InterruptedException {
    
    Vector column = new RandomAccessSparseVector(Integer.MAX_VALUE/2);
    long itemID = -1;
    int samples = 0;
    for (MyTupleWritable data : values) {
      long userID = ((LongWritable) data.get(0)).get();
      itemID = ((LongWritable) data.get(1)).get();
      Float prefValue = ((FloatWritable) data.get(2)).get();
      if (prefValue == 0.0)
        prefValue = 0.0000001f;
      column.set((int) userID, prefValue);
      samples++;
    }
    // how we get 0 samples I don't know.
    // one sample is not enough
    if (samples < 2)
      return;
    Vector item = new DenseVector(dimension);
    for(int dim = 0; dim < dimension; dim++) {
      Vector random = factory.getVector(100000, RandomMatrix.GAUSSIAN01);
      Iterator<Element> sparse = column.iterateNonZero();
      double userSum = 0;
      double prefSum = 0;
      while(sparse.hasNext()) {
        Element e = sparse.next();
        int index = e.index();
        userSum += random.getQuick(index);
        prefSum += e.get();
      }
      double value = ((userSum + (prefSum / samples)/2))/samples;
      value = Math.min(0.9999999999, value);
      item.setQuick(dim, value);
    }
    NamedVector namedItem = new NamedVector(item, Long.toString(itemID));
   // write namedItem
//    System.err.println(namedItem.getName() + item.toString());
  }

  @Override
  protected void cleanup(org.apache.hadoop.mapreduce.Reducer<LongWritable,MyTupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
  };

}

/*
 * reduce the object load from incrementing the ratings
 */
class ChemicalDouble {
  public double f;

  ChemicalDouble() {
    f = 0.0f;
  }

  //	@Override
  //	public int hashCode() {
  //		// TODO Auto-generated method stub
  //		return ((Double) f).hashCode();
  //	}
  //
  //	@Override
  //	public boolean equals(Object o) {
  //		return ((Double) f).equals(((ChemicalDouble)o).f);
  //	}
}