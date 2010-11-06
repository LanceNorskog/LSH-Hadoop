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
//		doKmeans(vectors);
		doDirichlet(vectors, dimensions);
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
			result.hashCode();
			System.out.println("N of cluster[]: " + result.size());
//			int i = 0;
			org.apache.mahout.clustering.Cluster[] c = result.get(result.size() - 1);
//			for(org.apache.mahout.clustering.Cluster[] c: result) {
				System.out.println("Length of cluster: " + c.length);
//				for(int j = 0; j < c.length; j++) {
//					System.out.print(" " + c[j].getNumPoints());
//				}
//				for(int j = 0; j < c.length; j++) {
//					System.out.println(" " + Math.sqrt(c[j].getCenter().getLengthSquared()));
//				}
//				System.out.println();
//				i++;
//			}
	}

	private static void doKmeans(List<Vector> vectors) {
		List<Canopy> canopies = CanopyTest.makeCanopies(vectors, new TanimotoDistanceMeasure(), 0.1, 0.05);
		System.out.println("N of canopies: " + canopies.size());

		DistanceMeasure measure = new TanimotoDistanceMeasure();

		List<SoftCluster> clusters = new ArrayList<SoftCluster>();
		for(Canopy canopy: canopies) {
			Vector centroid = canopy.computeCentroid();
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
