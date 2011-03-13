/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.impl.model;

import java.util.Random;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Distribution;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.junit.Test;

/**
 * Tests {@link GenericDataModel}.
 */
public final class SamplingDataModelTest extends TasteTestCase {
  
  static DataModel smallFull;
  static DataModel largeSparse;
  
  @Override
  public void setUp() throws Exception {
    smallFull = getDataModel();
    largeSparse = getDataModelLarge();
    System.err.println("Finished");
  };
  
  protected static DataModel getDataModelLarge() {
    int USERS = 100;
    int ITEMS = 1000;
    double maxSample = 0.9;
    long FACTOR = 5;
    
    long[] userIDs = new long[USERS];
    Double[][] itemValues = new Double[USERS][];
    for(int i = 0; i < USERS; i++) {
      userIDs[i] = i;
    }
    for(int i = 0; i < USERS; i++) {
      itemValues[i] = new Double[ITEMS];
    }
    Random rnd = new Random(0);
    for(int userID = 0; userID < USERS; userID++) {
      for(int itemID = 0; itemID < ITEMS; itemID++) {
        double sample = rnd.nextDouble();
        if (sample < maxSample) {
          double value = rnd.nextDouble();
          itemValues[userID][itemID] = value * FACTOR;
        } else {
          itemValues[userID][itemID] = 0.0;
        }
      }
    }
    return getDataModel(userIDs, itemValues);
    }
    
    
    @Test
    public void testHolographicSampling() throws Exception {
      DataModel baseModel = smallFull;
      DataModel sampledModel = new SamplingDataModel(smallFull, 0.0, 0.8, Distribution.HOLOGRAPHIC);
      
      assertEquals(baseModel.getNumUsers(), sampledModel.getNumUsers());
      assertEquals(baseModel.getNumItems(), sampledModel.getNumItems());
      
      comparePrefs(baseModel, sampledModel);
    }
    
    @Test
    public void testUserSampling() throws Exception {
      DataModel baseModel = largeSparse;
      DataModel sampledModel = new SamplingDataModel(largeSparse, 0.0, 0.8, Distribution.USER);
      
      // number of users is the same, but nuked ones have 0 prefs
      LongPrimitiveIterator users = baseModel.getUserIDs();
      int nuked = 0;
      while(users.hasNext()) {
        Long userID = users.nextLong();
        PreferenceArray prefs = sampledModel.getPreferencesFromUser(userID);
        int l = prefs.length();
        if (prefs.length() == 0) {
          nuked++;
        } else {
          assertTrue(prefs.length() > 650 && prefs.length() < 850);
        }
        int count = 0;
        for(int item = 0; item < prefs.length(); item++) {
          if (prefs.getValue(item) != 0) {
            count++;
          }   
        }
      }
      assertTrue(nuked > 0);
      
      assertTrue(baseModel.getNumUsers() == sampledModel.getNumUsers());
      assertTrue(baseModel.getNumItems() == sampledModel.getNumItems());
      comparePrefs(baseModel, sampledModel);
    }
    
    private void comparePrefs(DataModel baseModel, DataModel sampledModel) throws TasteException {
      LongPrimitiveIterator itemIter = baseModel.getItemIDs();
      int counter = 0;
      while(itemIter.hasNext()) 
      {
        Long itemID = itemIter.nextLong();
        itemID.hashCode();
        PreferenceArray prefs = baseModel.getPreferencesForItem(itemID);
        for(int pref = 0; pref < prefs.length(); pref++) {
          Preference basePref = prefs.get(pref);
          long userID = basePref.getUserID();
          Float prefValue = prefs.getValue(pref);
          Float sampledValue = sampledModel.getPreferenceValue(userID, itemID);
          assertTrue(null != prefValue);
          assertTrue(null == sampledValue || prefValue.floatValue() == sampledValue.floatValue());
          if (null == sampledValue)
            counter++;
        }
      }
      assertTrue(counter > 0);
    }
    
  }
