package org.apache.mahout.math.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deltas {
  List<Double> full = new ArrayList<Double>();
  
  public void addDatum(double datum) {
    full.add(datum);
  }
  
  public double getDeltas() {
    Collections.sort(full);
    double deltas = 0;
    for(int i = 0; i < full.size() - 1; i++) {
      deltas += full.get(i + 1) - full.get(i);
    }
    
    return deltas;
  }

}
