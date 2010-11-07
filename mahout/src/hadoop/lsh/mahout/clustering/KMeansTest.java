package lsh.mahout.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.clustering.Model;
import org.apache.mahout.clustering.ModelDistribution;
import org.apache.mahout.clustering.canopy.Canopy;
import org.apache.mahout.clustering.dirichlet.DirichletClusterer;
import org.apache.mahout.clustering.dirichlet.models.AsymmetricSampledNormalDistribution;
import org.apache.mahout.clustering.dirichlet.models.NormalModelDistribution;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansClusterer;
import org.apache.mahout.clustering.fuzzykmeans.SoftCluster;
import org.apache.mahout.clustering.kmeans.Cluster;
import org.apache.mahout.clustering.kmeans.KMeansClusterer;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.TanimotoDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

public class KMeansTest {

	public static void main(String args[]) throws Exception {
		int k = 2; 
		List<Vector> vectors = CanopyTest.loadVectors(args[0]);
		int dimensions = vectors.get(0).size();
		List<Vector> save = new ArrayList(vectors.size());
		for(Vector v: vectors) {
			save.add(v.clone());
		}
		doKmeans(vectors, true);
//		doFuzzyKmeans(vectors);
//		CanopyTest.canopyExample(save);
//		doDirichlet(vectors, dimensions);
	}

	private static void doDirichlet(List<Vector> vectors, int dimensions) {
		List<VectorWritable> vwList = new ArrayList<VectorWritable>();
		ModelDistribution<VectorWritable> modelDist = new AsymmetricSampledNormalDistribution(
				new VectorWritable(new DenseVector(dimensions)));
		DirichletClusterer dc =
			new DirichletClusterer(
					vwList,
					modelDist,
					1.0, 10, 2, 2);
		List<org.apache.mahout.clustering.Cluster[]> result = dc.cluster(50);
		System.out.println("N of cluster[]: " + result.size());
		org.apache.mahout.clustering.Cluster[] c = result.get(result.size() - 1);
		System.out.println("Length of cluster: " + c.length);
		//				for(int j = 0; j < c.length; j++) {
		//					System.out.print(" " + c[j].getNumPoints());
		//				}
		double normalize = 1/Math.sqrt(dimensions);
		for(int j = 0; j < c.length; j++) {
			System.out.print("radius: ");
			Vector radius = c[j].getRadius();
			//					radius = CanopyTest.normalizeRadius(radius);
			//					Vector v = radius.times(normalize);
			CanopyTest.summarizeVector(radius);
		}
		for(int j = 0; j < c.length; j++) {
			System.out.print("radius: ");
			Vector radius = c[j].getRadius();
			System.out.println(radius);
		}
	}

	private static String trim(double maxValue) {
		String string = Double.toString(maxValue);
		int l = string.length();
		return l < 5 ? string : string.substring(0, 6);

	}



	private static void doKmeans(List<Vector> vectors, boolean csv) {
		DistanceMeasure measure = new TanimotoDistanceMeasure();
		List<Canopy> canopies = CanopyTest.makeCanopies(vectors, measure, 0.10, 0.03);
//		System.out.println("N of canopies: " + canopies.size());

		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		for(Canopy canopy: canopies) {
			Vector center = canopy.getCenter();
			SoftCluster cluster = new SoftCluster(center, canopy.getId(), measure);
			clusters.add(cluster);
		}
		List<List<Cluster>> kmout = KMeansClusterer.clusterPoints(vectors, clusters, 
				new EuclideanDistanceMeasure(), 10, 0.1 );
		kmout.hashCode();
//		System.out.println("N of clusters: " + kmout.size());
		for(Cluster sc: kmout.get(kmout.size() - 1)) 
		{ 
			Vector center = sc.getCenter();
			if (csv) {
				for(int i = 0; i < center.size(); i++) {
					System.out.print(center.getQuick(i) + ",");
				}
				System.out.println();
			} else {
				System.out.println("Cluster ident: " + sc.getIdentifier() + 
						", vector L2: " + trim(center.norm(2)) +
						", radius L2: " + trim(sc.getRadius().norm(2)));
			}
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			//			for(int i = 0; i < sc.count(); i++) {
			//				for(int j = 0; j < sc.count(); j++) {
			//					double dist = sc.
			//				}
			//			}
		}
	}
	
	private static void doFuzzyKmeans(List<Vector> vectors) {
		DistanceMeasure measure = new TanimotoDistanceMeasure();
		List<Canopy> canopies = CanopyTest.makeCanopies(vectors, measure, 0.1, 0.03);
		System.out.println("N of canopies: " + canopies.size());

		List<SoftCluster> clusters = new ArrayList<SoftCluster>();
		for(Canopy canopy: canopies) {
			Vector centroid = canopy.getCenter();
			SoftCluster cluster = new SoftCluster(centroid, canopy.getId(), measure);
			clusters.add(cluster);
		}
		List<List<SoftCluster>> kmout = FuzzyKMeansClusterer.clusterPoints(vectors, clusters, 
				new EuclideanDistanceMeasure(), 0.1, 2.0, 30 );
		kmout.hashCode();
		System.out.println("N of clusters: " + kmout.size());
		for(SoftCluster sc: kmout.get(kmout.size() - 1)) {
			System.out.println("Cluster ident: " + sc.getIdentifier() + ", N points: " + sc.getNumPoints());
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			//			for(int i = 0; i < sc.count(); i++) {
			//				for(int j = 0; j < sc.count(); j++) {
			//					double dist = sc.
			//				}
			//			}
		}
	}
}
