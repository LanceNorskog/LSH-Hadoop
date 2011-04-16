package lsh.mahout.core2;

import java.util.ArrayList;
import java.util.List;

/*
 * Contain a set of simplexes in space.
 */

public class SimplexSpace<T> {
  final List<Simplex> simplexes = new ArrayList<Simplex>();
  
  public SimplexSpace() {
    
  }
  
  public void addSimplex(Simplex x) {
    simplexes.add(x);
  }
  
  
  
}
