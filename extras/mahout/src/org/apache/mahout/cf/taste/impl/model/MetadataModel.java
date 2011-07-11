package org.apache.mahout.cf.taste.impl.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;

/*
 * Store metadata for users or items. Allows richer exploration of recommendations.
 */

public class MetadataModel<T> implements Serializable {
  
  private final Map<Long,T> data;
  private final String label;
  
  public MetadataModel(Map<Long,T> data, String label) {
    this.data = data;
    this.label = label;
  }
  
  public boolean containsKey(long key) {
    return data.containsKey(key);
  }
  
  public boolean containsValue(T value) {
    return data.containsValue(value);
  }
  
  /*
   * Search for subvalue in array of values 
   */
  public boolean containsSubValue(long key, T sub) {
    T array = data.get(key);
    Object[] subs = (Object[]) array;
    for(Object o: subs) {
      if (o.equals(sub))
        return true;
    }
    return false;
  }
  
  public T getData(long key) throws TasteException {
    if (! data.containsKey(key))
      throw new TasteException("No data for key: " + key);
    return data.get(key);
  }
  
  public Iterator<Long> getKeys() {
    return data.keySet().iterator();
  }
  
  public String getLabel() {
    return label;
  }
  
  public void put(Long key, T value) {
    data.put(key, value);
  }
  
}
