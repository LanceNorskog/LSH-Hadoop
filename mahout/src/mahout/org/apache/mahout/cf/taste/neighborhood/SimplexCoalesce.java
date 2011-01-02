/**
 * 
 */
package org.apache.mahout.cf.taste.neighborhood;

import java.util.HashMap;
import java.util.Map;

import lsh.core.Hasher;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.math.Vector;

/**
 * @author lance
 *
 * Coalesce neighboring simplices to satisfy a clustering rule.
 * 
 */
public class SimplexCoalesce {
  public double distance = 0.0001;
  public int nUsers = 1;
  
//  public void coalesce
  
  
  

  /*
   * same as iterative map/reduce algorithm

coalescing version

while corners db > 0
for each corner in db
    lod, next lod

    if corner satisfies rule
        send to finished stream
    done

    Find corner for next lod  
    if there  
       coalesce to that corner
    else
       emit self to finished stream
repeat
done

to avoid changing the list during walk:
    mark LOD as -1 for finished 
    remove and save as separate pass after coalesce pass

   */
  
  
}


