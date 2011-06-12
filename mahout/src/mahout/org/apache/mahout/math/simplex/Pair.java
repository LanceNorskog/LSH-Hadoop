package org.apache.mahout.math.simplex;

import java.util.Comparator;


class Pair {
  final int index;
  final double value;
  
  public Pair(int index, double value) {
    this.index = index;
    this.value = value;
  }
}

class PairComparator implements Comparator<Pair> {
  
  // sorts from highest to lowest
  
  @Override
  public int compare(Pair a, Pair b) {
    if (a.value < b.value)
      return 1;
    else if (a.value > b.value)
      return -1;
    else
      return 0;
  }
}

