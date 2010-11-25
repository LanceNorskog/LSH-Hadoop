package lsh.mahout.clustering;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
import org.apache.mahout.clustering.canopy.Canopy;
import org.apache.mahout.clustering.canopy.CanopyClusterer;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.TanimotoDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

public class CanopyTest {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    List<Vector> vectors = loadVectors(args[0]);
    canopyExample(vectors);
  }

  public static List<Vector> loadVectors(String path) throws IOException {
    List<Vector> vectors = new ArrayList<Vector>();
    SequenceFile.Reader reader = getSequenceFileReader(path);
    LongWritable index = new LongWritable();
    VectorWritable v = new VectorWritable();
    v.setWritesLaxPrecision(true);
    try {
      while(reader.next(index, v)) {
        vectors.add(v.get().clone());
      }
    } catch (EOFException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return vectors;
  }

  public static void canopyExample(List<Vector> vectors) {
    List<Canopy> canopies = makeCanopies(vectors, new TanimotoDistanceMeasure(), 0.1, 0.000075);
    for(Canopy canopy : canopies) {
      Vector radius = canopy.getRadius();
      System.out.print("Canopy id: " + canopy.getId() + ", center ");
      //			radius = normalizeRadius(radius);
      summarizeVector(canopy.getCenter());
    }
  }

  // Dirichlet gives radius vectors with negative values
  public static Vector normalizeRadius(Vector radius) {
    double min = radius.minValue();
    double max = radius.maxValue();
    return radius.plus(-min);
  }

  public static void summarizeVector(Vector v) {
    int dimensions = v.size();
    RunningAverageAndStdDev stdev = new CompactRunningAverageAndStdDev();
    double[] values = new double[v.size()];
    for(int i = 0; i < v.size(); i++) {
      values[i] = v.getQuick(i);
      stdev.addDatum(values[i]);
    }
    System.out.println( "min: " + v.minValue() + ", max: " + trim(v.maxValue()) + ", norm2: " + trim(v.norm(2)) + ", stddev: " + trim(stdev.getStandardDeviation()));
  }

  private static String trim(double maxValue) {
    String string = Double.toString(maxValue);
    int l = string.length();
    return l < 7 ? string : string.substring(0, 6);

  }

  public static List<Canopy> makeCanopies(List<Vector> vectors, DistanceMeasure measure, double t1, double t2) {
    //		System.out.println("make Canopies: tanimoto " + t1 + "," + t2);
    List<Canopy> canopies = CanopyClusterer.createCanopies(
        vectors, measure, t1, t2);
    return canopies;
  }

  public static SequenceFile.Reader getSequenceFileReader(String path) throws IOException {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(path).makeQualified(fs), conf);
    return reader;
  }
}
