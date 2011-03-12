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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.SamplingDataModel.Distribution;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.junit.Test;

/**
 * Tests {@link GenericDataModel}.
 */
public final class SamplingDataModelTest extends TasteTestCase {
  
  protected static DataModel getDataModelOriginal() {
    return getDataModel(
            new long[] {1, 2, 3, 4},
            new Double[][] {
                    {0.1, 0.3},
                    {0.2, 0.3, 0.3},
                    {0.4, 0.3, 0.5},
                    {0.7, 0.3, 0.8},
            });
  }

  protected static DataModel getDataModelSampled() {
    return getDataModel(
            new long[] {1, 2, 3, 4},
            new Double[][] {
                    {0.1, 0.3},
                    {0.2, 0.3, 0.3},
                    {0.4, 0.3, 0.5},
                    {0.7, 0.3, 0.8},
            });
  }
  
  @Test
  public void testHolographicSampling() throws Exception {
    DataModel baseModel = (DataModel) getDataModel();
    DataModel sampledModel = new SamplingDataModel(getDataModel(), 0.0, 0.1, Distribution.HOLOGRAPHIC);
    
    assertEquals(baseModel.getNumUsers(), sampledModel.getNumUsers());
    assertEquals(baseModel.getNumItems(), sampledModel.getNumItems());
    
    comparePrefs(baseModel, sampledModel);
  }

  @Test
  public void testUserSampling() throws Exception {
    DataModel baseModel = (DataModel) getDataModel();
    DataModel sampledModel = new SamplingDataModel(getDataModel(), 0.0, 0.1, Distribution.USER);
    
    // number of users is the same, but nuked ones have 0 prefs
    LongPrimitiveIterator users = baseModel.getUserIDs();
    int nuked = 0;
    while(users.hasNext()) {
      Long userID = users.nextLong();
      PreferenceArray prefs = sampledModel.getPreferencesFromUser(userID);
      if (prefs.length() == 0)
        nuked++;
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
