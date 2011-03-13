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
  final boolean useItem;
  double epsilon = 0.0001;

  PrefComparator() {
    sign = 1;
    useItem = false;
  }

  PrefComparator(int sign, boolean useItem) {
    // need 1/-1
    this.sign = sign / Math.abs(sign);
    this.useItem = useItem;
  }

  @Override
  public int compare(Preference p1, Preference p2) {
    if (p1.getValue()*sign > ((p2.getValue() - epsilon)*sign))
      return 1;
    else if (p1.getValue()*sign < (p2.getValue() + epsilon)*sign)
      return -1;
    else if (!useItem)
      return 0;
    else {
      // break ties by item id
      if (p1.getItemID() > p2.getItemID())
        return 1;
      else if (p1.getItemID() < p2.getItemID())
        return -1;
      else
        return 0;
    }
  }

}
