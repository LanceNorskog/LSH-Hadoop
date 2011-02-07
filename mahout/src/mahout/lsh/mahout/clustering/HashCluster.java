/**
 * 
 */
package lsh.mahout.clustering;

import org.apache.mahout.clustering.AbstractCluster;
import org.apache.mahout.clustering.Model;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

/**
 * 
 */
public class HashCluster extends AbstractCluster {

  /**
   * 
   */
  public HashCluster() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param point
   * @param id
   */
  public HashCluster(Vector point, int id) {
    super(point, id);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param center
   * @param radius
   * @param id
   */
  public HashCluster(Vector center, Vector radius, int id) {
    super(center, radius, id);
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.clustering.AbstractCluster#getIdentifier()
   */
  @Override
  public String getIdentifier() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.clustering.Model#pdf(java.lang.Object)
   */
  @Override
  public double pdf(VectorWritable x) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.mahout.clustering.Model#sampleFromPosterior()
   */
  @Override
  public Model<VectorWritable> sampleFromPosterior() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
