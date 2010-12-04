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
import org.apache.mahout.math.RandomVector;
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
 */


public class GLReducer extends
Reducer<LongWritable, MyTupleWritable, Text, Text> {

  private int dimension = -1;
  private int randomSeed = 0;

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
  }

  protected void reduce(
      LongWritable key,
      Iterable<MyTupleWritable> values,
      Reducer<LongWritable, MyTupleWritable, Text, Text>.Context context)
  throws java.io.IOException, InterruptedException {
    
    Vector column = new RandomAccessSparseVector(Integer.MAX_VALUE/2);
    long itemID = -1;
    for (MyTupleWritable data : values) {
      long userID = ((LongWritable) data.get(0)).get();
      itemID = ((LongWritable) data.get(1)).get();
      Float prefValue = ((FloatWritable) data.get(2)).get();
      column.set((int) userID, prefValue);
    }
    int users = column.size();
    Vector item = new DenseVector(dimension);
    Vector random = new RandomVector(randomSeed + (int) itemID);
    for(int dim = 0; dim < dimension; dim++) {
      Iterator<Element> sparse = column.iterateNonZero();
      double userSum = 0;
      double prefSum = 0;
      while(sparse.hasNext()) {
        Element e = sparse.next();
        userSum += random.getQuick(e.index());
        prefSum += e.get();
      }
      double value = (userSum + prefSum / users)/2;
      value /= users;
      item.setQuick(dim, value);
    }
    NamedVector namedItem = new NamedVector(item, Long.toString(itemID));
   // write namedItem
    System.err.println(namedItem.getName() + item.toString());
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