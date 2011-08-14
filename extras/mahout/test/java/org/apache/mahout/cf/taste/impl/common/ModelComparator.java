/**
 * 
 */
package org.apache.mahout.cf.taste.impl.common;

import java.util.Comparator;
import java.util.Map;

/**
 * @author lance
 *
 * Compare two ids in a data model
 */
public class ModelComparator<Long> implements Comparator<Long> {
  
  final private Map<Long,Integer> counts;

  public ModelComparator(Map<Long,Integer> counts) {
    this.counts = counts;
  }

   @Override
  public int compare(Long arg0, Long arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

   /**
    * @param args
    */
   public static void main(String[] args) {
     // TODO Auto-generated method stub
     
   }


}
