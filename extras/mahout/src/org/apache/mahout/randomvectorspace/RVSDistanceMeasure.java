package org.apache.mahout.randomvectorspace;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.parameters.Parameter;
import org.apache.mahout.math.Vector;

public class RVSDistanceMeasure implements DistanceMeasure {
  
  @Override
  public double distance(Vector v1, Vector v2) {
    RVS mask1 = ((RVSVector) v1).mask;
    RVS mask2 = ((RVSVector) v2).mask;
    return (double) mask1.distance(mask2);
  }
  
  @Override
  public double distance(double centroidLengthSquare, Vector centroid, Vector v) {
    return distance(centroid, v);
  }
  
  @Override
  public void configure(Configuration config) {
  }
  
  @Override
  public void createParameters(String prefix, Configuration jobConf) {
  }
  
  @Override
  public Collection<Parameter<?>> getParameters() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
