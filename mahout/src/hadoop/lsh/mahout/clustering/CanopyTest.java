package lsh.mahout.clustering;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		CanopyExample(vectors);
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

	public static void CanopyExample(List<Vector> vectors) {
		List<Canopy> canopies = makeCanopies(vectors, new TanimotoDistanceMeasure(), 0.1, 0.05);
		for(Canopy canopy : canopies) {
			System.out.println("Canopy id: " + canopy.getId() + " center: "
					+ canopy.getCenter().asFormatString());
		}
	}

	public static List<Canopy> makeCanopies(List<Vector> vectors, DistanceMeasure measure, double t1, double t2) {
		System.out.println("make Canopies: tanimoto " + t1 + "," + t2);
		List<Canopy> canopies = CanopyClusterer.createCanopies(
				vectors, measure, t1, t2);
		return canopies;
	}

}
