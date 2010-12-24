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

import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
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
import org.junit.Before;
import org.junit.Test;

import working.MinkowskiDistanceMeasure;

import java.io.File;
import java.util.NoSuchElementException;

/** <p>Tests {@link LSHDataModel}.</p> */
public final class LSHDataModelTest extends TasteTestCase {

  /*
   * Generated semantic vectors based on GroupLens originals
   * 
   * Users: 1,10,100,101
   * Items: 1,10,100,1005,1007,1009
   */
  private static final String[] DATA = {
      "1,0.30932754278182983,0.40351617336273193*U\n" + 
      "1,0.4302093982696533,0.5015546679496765*I\n" + 
      "10,0.5572678446769714,0.5245930552482605*U\n" + 
      "10,0.4975421726703644,0.6101256012916565*I\n" + 
      "100,0.45778024196624756,0.4548991024494171*U\n" + 
      "100,0.46164557337760925,0.631072461605072*I\n" + 
      "1005,0.4474824368953705,0.603765606880188*I\n" + 
      "1007,0.4313960373401642,0.5904638767242432*I\n" + 
      "1009,0.5165315270423889,0.48830467462539673*I\n" + 
      "101,0.40751633048057556,0.5549958348274231*U",
      };

  private DataModel model;
  private File testFile;
  private DistanceMeasure measure = new EuclideanDistanceMeasure();

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    testFile = getTestTempFile("test.txt");
    writeLines(testFile, DATA);
    model = new LSHDataModel(testFile, measure);
  }

  @Test
  public void testFile() throws Exception {
    UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(model);
    UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, userSimilarity, model);
    Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, userSimilarity);
    assertEquals(1, recommender.recommend(1, 3).size());
    assertEquals(0, recommender.recommend(10, 3).size());
    assertEquals(1, recommender.recommend(100, 3).size());

    // Make sure this doesn't throw an exception
    model.refresh(null);
  }

//  @Test
//  public void testTranspose() throws Exception {
//    FileDataModel tModel = new FileDataModel(testFile, true, FileDataModel.DEFAULT_MIN_RELOAD_INTERVAL_MS);
//    PreferenceArray userPrefs = tModel.getPreferencesFromUser(456);
//    assertNotNull("user prefs are null and it shouldn't be", userPrefs);
//    PreferenceArray pref = tModel.getPreferencesForItem(123);
//    assertNotNull("pref is null and it shouldn't be", pref);
//    assertEquals("pref Size: " + pref.length() + " is not: " + 3, 3, pref.length());
//  }
//
//  @Test  
//  public void testGetItems() throws Exception {
//    LongPrimitiveIterator it = model.getItemIDs();
//    assertNotNull(it);
//    assertTrue(it.hasNext());
//    assertEquals(123, it.nextLong());
//    assertTrue(it.hasNext());
//    assertEquals(234, it.nextLong());
//    assertTrue(it.hasNext());
//    assertEquals(456, it.nextLong());
//    assertTrue(it.hasNext());
//    assertEquals(654, it.nextLong());
//    assertTrue(it.hasNext());
//    assertEquals(789, it.nextLong());
//    assertTrue(it.hasNext());
//    assertEquals(999, it.nextLong());
//    assertFalse(it.hasNext());
//    try {
//      it.next();
//      fail("Should throw NoSuchElementException");
//    } catch (NoSuchElementException nsee) {
//      // good
//    }
//  }
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
//  @Test
//  public void testNumUsersPreferring() throws Exception {
//    assertEquals(2, model.getNumUsersWithPreferenceFor(456));
//    assertEquals(0, model.getNumUsersWithPreferenceFor(111));
//    assertEquals(0, model.getNumUsersWithPreferenceFor(111, 456));
//    assertEquals(2, model.getNumUsersWithPreferenceFor(123, 234));
//  }
//
//  @Test
//  public void testRefresh() throws Exception {
//    final MutableBoolean initialized = new MutableBoolean(false);
//    Runnable initializer = new Runnable() {
//      @Override
//      public void run() {
//        try {
//          model.getNumUsers();
//          initialized.setValue(true);
//        } catch (TasteException te) {
//          // oops
//        }
//      }
//    };
//    new Thread(initializer).start();
//    Thread.sleep(1000L); // wait a second for thread to start and call getNumUsers()
//    model.getNumUsers(); // should block
//    assertTrue(initialized.booleanValue());
//    assertEquals(4, model.getNumUsers());
//  }
//
//  @Test
//  public void testExplicitRefreshAfterCompleteFileUpdate() throws Exception {
//    File file = getTestTempFile("refresh");
//    writeLines(file, "123,456,3.0");
//
//    /* create a FileDataModel that always reloads when the underlying file has changed */
//    FileDataModel dataModel = new FileDataModel(file, false, 0L);
//    assertEquals(3.0f, dataModel.getPreferenceValue(123L, 456L), EPSILON);
//
//    /* change the underlying file,
//     * we have to wait at least a second to see the change in the file's lastModified timestamp */
//    Thread.sleep(2000L);
//    writeLines(file, "123,456,5.0");
//    dataModel.refresh(null);
//
//    assertEquals(5.0f, dataModel.getPreferenceValue(123L, 456L), EPSILON);
//  }
//
//  @Test
//  public void testToString() {
//    assertTrue(model.toString().length() > 0);
//  }

}
