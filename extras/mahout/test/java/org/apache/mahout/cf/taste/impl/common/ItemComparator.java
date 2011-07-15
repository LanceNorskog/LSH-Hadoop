package org.apache.mahout.cf.taste.impl.common;

import java.util.Comparator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;

public class ItemComparator implements Comparator<Long> {
  FastByIDMap<Integer> userCache = new FastByIDMap<Integer>();
  private DataModel model;
  
  public ItemComparator(DataModel model) {
    this.model = model;
  }
  
  @Override
  public int compare(Long user1, Long user2) {
    try {
      int size1;
      if (userCache.containsKey(user1))
        size1 = userCache.get(user1);
      else {
        size1 = model.getItemIDsFromUser(user1).size();
        userCache.put(user1, size1);
      }
      int size2;
      if (userCache.containsKey(user2)) 
        size2 = userCache.get(user2);
      else {
        size2 = model.getItemIDsFromUser(user2).size();
        userCache.put(user2, size2);
      }
      if (size1 < size2)
        return -1;
      else if (size1 > size2)
        return 1;
      else
        return 0;
    } catch (TasteException e) {
      return 0;
    }
  }
  
}
