package lsh.mahout.core2;

import java.util.ArrayList;
import java.util.List;

/*
 * Contain a set of simplexes in space.
 */

public class SimplexSpace<T> {
  final List<Simplex<T>> simplexes = new ArrayList<Simplex<T>>();
  
  public SimplexSpace() {
    
  }
  
  public void addSimplex(Simplex x) {
    simplexes.add(x);
  }
  
  
  
}
