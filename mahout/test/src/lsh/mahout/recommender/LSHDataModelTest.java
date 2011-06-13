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

package lsh.mahout.recommender;

import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.model.VectorDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/** <p>Tests {@link VectorDataModel}.</p> */
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
    model = new VectorDataModel(testFile, measure);
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

}
