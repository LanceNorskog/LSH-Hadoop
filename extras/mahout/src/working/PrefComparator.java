package working;

import java.util.Comparator;

import org.apache.mahout.cf.taste.model.Preference;

/*
 * For sorting by preference.
 * Can swap signs.
 * Can sub-sort with Item id.
 */

public class PrefComparator implements Comparator<Preference> {
  final int sign;
  final boolean useIDs;
  float epsilon = 0.0001f;
  
  PrefComparator() {
    sign = 1;
    useIDs = false;
  }
  
  PrefComparator(int sign, boolean useIDs) {
    // need 1/-1
    this.sign = sign / Math.abs(sign);
    this.useIDs = useIDs;
  }
  
  @Override
  public int compare(Preference p1, Preference p2) {
    float left = (p1.getValue() - epsilon) * sign;
    float right = (p2.getValue() - epsilon) * sign;
    if (left > right)
      return 1;
    else if (left < right)
      return -1;
    else if (!useIDs)
      return 0;
    else {
      // break ties by user id
      long leftU = p1.getUserID();
      long rightU = p2.getUserID();
      if (leftU > rightU)
        return 1;
      else if (leftU < rightU)
        return -1;
      else {
        // break ties by item id
        long leftI = p1.getItemID();
        long rightI = p2.getItemID();
        if (leftI > rightI)
          return 1;
        else if (leftI < rightI)
          return -1;
        else
          return 0;
      }
    }
  }
  
}
