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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.SemanticVectorFactory;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link VectorDataModel}.
 */
public final class VectorDataModelTest extends TasteTestCase {
  
  private static final double[][] DATA = {
    {123,456,0.1},
    {123,789,0.6},
    {123,654,0.7},
    {234,123,0.5},
    {234,234,1.0},
    {234,999,0.9},
    {345,789,0.6},
    {345,654,0.7},
    {345,123,1.0},
    {345,234,0.5},
    {345,999,0.5},
    {456,456,0.1},
    {456,789,0.5},
    {456,654,0.0},
    {456,999,0.2}
  };
  
  private DataModel model;
  private DistanceMeasure measure;
  
  DataModel getBigDataModel() {
    FastByIDMap<PreferenceArray> result = new FastByIDMap<PreferenceArray>();
    for (int i = 0; i < DATA.length; i++) {
      double[] pref = DATA[i];
      List<Preference> prefsList = new ArrayList<Preference>();
      prefsList.add(new GenericPreference((long) pref[0], (long) (pref[1]), (float) pref[2]));
      if (!prefsList.isEmpty()) {
        result.put((long) pref[0], new GenericUserPreferenceArray(prefsList));
      }
    }
    return new GenericDataModel(result);    
  }
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    GenericDataModel baseModel = (GenericDataModel) getBigDataModel();
    
    SemanticVectorFactory svf = new SemanticVectorFactory(baseModel, 2);
    this.measure = new EuclideanDistanceMeasure();
    VectorDataModel vm = new VectorDataModel(2, measure, 2 * 2);
    LongPrimitiveIterator users = baseModel.getUserIDs();
    while (users.hasNext()) {
      long userID = users.nextLong();
      Vector userV = svf.getRandomUserVector(userID);
      vm.addUser(userID, userV);
    }
    LongPrimitiveIterator items = baseModel.getItemIDs();
    while (items.hasNext()) {
      long itemID = items.nextLong();
      Vector itemV = svf.projectItemDense(itemID);
      vm.addItem(itemID, itemV);
    }
    this.model = vm;
  }
  
  @Test
  public void testSizes() throws TasteException {
    assertEquals("Expected 4 users in big model", 4, model.getNumUsers());
    assertEquals("Expected 2 items in big model", 2, model.getNumItems());
  }
  
  //  @Test  
  //  public void testSerialization() throws Exception {
  //    GenericDataModel model = (GenericDataModel) getDataModel();
  //    ByteArrayOutputStream baos = new ByteArrayOutputStream();
  //    ObjectOutputStream out = new ObjectOutputStream(baos);
  //    out.writeObject(model);
  //    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
  //    ObjectInputStream in = new ObjectInputStream(bais);
  //    GenericDataModel newModel = (GenericDataModel) in.readObject();
  //    assertEquals(model.getNumItems(), newModel.getNumItems());
  //    assertEquals(model.getNumUsers(), newModel.getNumUsers());
  //    assertEquals(model.getPreferencesFromUser(1L), newModel.getPreferencesFromUser(1L));    
  //    assertEquals(model.getPreferencesForItem(1L), newModel.getPreferencesForItem(1L));
  //    assertEquals(model.getRawUserData(), newModel.getRawUserData());
  //  }
  //  
//  @Test
//  public void testRecommend() throws Exception {
//    UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(model);
//    UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, userSimilarity, model);
//    Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, userSimilarity);
//    int recSize = recommender.recommend(123, 2).size();
//    assertEquals(1, recSize);
//    recSize = recommender.recommend(234, 3).size();
//    assertEquals(0, recSize);
//    recSize = recommender.recommend(345, 2).size();
//    assertEquals(1, recSize);
//    
//    // Make sure this doesn't throw an exception
//    model.refresh(null);
//  }
  
    @Test
    public void testTranspose() throws Exception {
      PreferenceArray userPrefs = model.getPreferencesFromUser(456);
      assertNotNull("user prefs are null and it shouldn't be", userPrefs);
      PreferenceArray pref = model.getPreferencesForItem(654);
      assertNotNull("pref is null and it shouldn't be", pref);
      assertEquals("pref Size: " + pref.length() + " is not: " + 4, 4, pref.length());
    }
    
    @Test  (expected = NoSuchElementException.class)
    public void testGetItems() throws Exception {
      LongPrimitiveIterator it = model.getItemIDs();
      assertNotNull(it);
      assertTrue(it.hasNext());
      assertEquals(654, it.nextLong());
      assertTrue(it.hasNext());
      assertEquals(999, it.nextLong());
      assertFalse(it.hasNext());
      it.next();
    }
  //  
  //  @Test
  //  public void testPreferencesForItem() throws Exception {
  //    PreferenceArray prefs = model.getPreferencesForItem(456);
  //    assertNotNull(prefs);
  //    Preference pref1 = prefs.get(0);
  //    assertEquals(123, pref1.getUserID());
  //    assertEquals(456, pref1.getItemID());
  //    Preference pref2 = prefs.get(1);
  //    assertEquals(456, pref2.getUserID());
  //    assertEquals(456, pref2.getItemID());
  //    assertEquals(2, prefs.length());
  //  }
  //  
  //  @Test
  //  public void testGetNumUsers() throws Exception {
  //    assertEquals(4, model.getNumUsers());
  //  }
  //  
  //  
  //  @Test
  //  public void testNumUsersPreferring() throws Exception {
  //    assertEquals(2, model.getNumUsersWithPreferenceFor(456));
  //    assertEquals(0, model.getNumUsersWithPreferenceFor(111));
  //    assertEquals(0, model.getNumUsersWithPreferenceFor(111, 456));
  //    assertEquals(2, model.getNumUsersWithPreferenceFor(123, 234));
  //  }
}