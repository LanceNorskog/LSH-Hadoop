/**
 * 
 */
package org.apache.mahout.cf.taste.impl.common;

import java.util.Comparator;
import java.util.Map;

/**
 * @author lance
 *
 * Compare two ids in a data model by the number of ratings
 * Want reverse order
 */
public class ModelComparator implements Comparator<Long> {
  
  final private Map<Long,Integer> counts;
  // You'd think there would be one of these publically available...
  final Comparator<Integer> intComparator = new Comparator<Integer> () {
    @Override
    public int compare(Integer o1, Integer o2) {
      if (o1 < o2)
        return -1;
      else if (o1 > o2)
        return 1;
      else
        return 0;
    };
  };

  public ModelComparator(Map<Long,Integer> counts) {
    this.counts = counts;
  }

   @Override
  public int compare(Long arg0, Long arg1) {
   return -1 * intComparator.compare(counts.get(arg0), counts.get(arg1));
//   return intComparator.compare((int) arg0.intValue(), (Integer) arg1.intValue());
  }

}
