package lsh.mahout.clustering;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.mahout.clustering.canopy.Canopy;
import org.apache.mahout.clustering.canopy.CanopyClusterer;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
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
		DataInput din = new DataInputStream(new FileInputStream(path));
		int nPoints = din.readInt();
		for(int i = 0; i < nPoints; i++) {
			VectorWritable v = new VectorWritable();
			v.setWritesLaxPrecision(true);
			v.readFields(din);
			vectors.add(v.get());
		}
		return vectors;
	}

	public static void canopyExample(List<Vector> vectors) {
		List<Canopy> canopies = makeCanopies(vectors, new TanimotoDistanceMeasure(), 0.1, 0.03);
		for(Canopy canopy : canopies) {
			Vector radius = canopy.getRadius();
			System.out.print("Canopy id: " + canopy.getId() + ", radius ");
//			radius = normalizeRadius(radius);
			summarizeVector(radius);
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
		StandardDeviation stddev = new StandardDeviation();
		double[] values = new double[v.size()];
		for(int i = 0; i < v.size(); i++) {
			values[i] = v.getQuick(i);
		}
		System.out.println( "min: " + v.minValue() + ", max: " + trim(v.maxValue()) + ", norm2: " + trim(v.norm(2)) + ", stddev: " + trim(stddev.evaluate(values)));
	}

	private static String trim(double maxValue) {
		String string = Double.toString(maxValue);
		int l = string.length();
		return l < 5 ? string : string.substring(0, 6);
		
	}

	public static List<Canopy> makeCanopies(List<Vector> vectors, DistanceMeasure measure, double t1, double t2) {
		System.out.println("make Canopies: tanimoto " + t1 + "," + t2);
		List<Canopy> canopies = CanopyClusterer.createCanopies(
				vectors, measure, t1, t2);
		return canopies;
	}

}
