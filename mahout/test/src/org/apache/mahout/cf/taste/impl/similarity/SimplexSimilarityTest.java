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

package org.apache.mahout.cf.taste.impl.similarity;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.SemanticVectorFactory;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.simplex.OrthonormalHasher;
import org.apache.mahout.math.simplex.Simplex;
import org.apache.mahout.math.simplex.SimplexSimilarity;
import org.apache.mahout.math.simplex.SimplexSpace;
import org.junit.Test;

/** <p>Tests {@link SimplexSimilarity}.</p> */
public final class SimplexSimilarityTest extends SimilarityTestCase {
  
  public static final int DIMENSIONS = 20;

  @Test
  public void testFullCorrelation1() throws Exception {
    DataModel dataModel = getDataModel(
            new long[] {1, 2},
            new Double[][] {
                    {1.0, 2.0, 3.0},
                    {1.0, 2.0, 3.0},
            });
    SimplexSpace<Long> itemSpace = getItemSpace(dataModel);
    double correlation = new SimplexSimilarity(null, itemSpace, new EuclideanDistanceMeasure()).itemSimilarity(1, 2);
    // No reason for this number; SVF vectors are deterministic from the itemID. Yes, this is bad.
    assertCorrelationEquals(Math.sqrt(2)/10, correlation);
  }

  private SimplexSpace<Long> getUserSpace(DataModel model) throws TasteException {
    SimplexSpace<Long> userSpace = new SimplexSpace<Long>(new OrthonormalHasher(DIMENSIONS, 0.1), DIMENSIONS);
    LongPrimitiveIterator userIter = model.getUserIDs();
    SemanticVectorFactory svf = new SemanticVectorFactory(model, DIMENSIONS);
    while (userIter.hasNext()) {
      long userID = userIter.nextLong();
      Vector userV = svf.projectUserDense(userID);
      Simplex<Long> userS = userSpace.newSimplex(userV, userID);
      userSpace.addSimplex(userS, userID);
    }
    return userSpace;
  }

  private SimplexSpace<Long> getItemSpace(DataModel model) throws TasteException {
    SimplexSpace<Long> itemSpace = new SimplexSpace<Long>(new OrthonormalHasher(DIMENSIONS, 0.1), DIMENSIONS);
    LongPrimitiveIterator itemIter = model.getItemIDs();
    SemanticVectorFactory svf = new SemanticVectorFactory(model, DIMENSIONS);
    while (itemIter.hasNext()) {
      long itemID = itemIter.nextLong();
      Vector itemV = svf.projectItemDense(itemID, null);
      Simplex<Long> itemS = itemSpace.newSimplex(itemV, itemID);
      itemSpace.addSimplex(itemS, itemID);
    }
    return itemSpace;
  }

  @Test
  public void testFullCorrelation2() throws Exception {
    DataModel dataModel = getDataModel(
            new long[] {1, 2},
            new Double[][] {
                    {1.0, 2.0, 3.0},
                    {4.0, 5.0, 6.0},
            });
    SimplexSpace<Long> userSpace = getUserSpace(dataModel);
    double correlation = new SimplexSimilarity(userSpace, null, 
        new EuclideanDistanceMeasure()).userSimilarity(1, 2);
    assertCorrelationEquals(1.0, correlation);
  }


  @Test
  public void testAnticorrelation() throws Exception {
    DataModel dataModel = getDataModel(
            new long[] {1, 2},
            new Double[][] {
                    {1.0, 2.0, 3.0},
                    {3.0, 2.0, 1.0},
            });
    SimplexSpace<Long> userSpace = getUserSpace(dataModel);
    double correlation = new SimplexSimilarity(userSpace, null, 
        new EuclideanDistanceMeasure()).userSimilarity(1, 2);
    assertCorrelationEquals(-1.0, correlation);
  }


  @Test
  public void testSimple() throws Exception {
    DataModel dataModel = getDataModel(
            new long[] {1, 2},
            new Double[][] {
                    {1.0, 2.0, 3.0},
                    {2.0, 3.0, 1.0},
            });
    SimplexSpace<Long> userSpace = getUserSpace(dataModel);
    double correlation = new SimplexSimilarity(userSpace, null, new EuclideanDistanceMeasure()).userSimilarity(1, 2);
    assertCorrelationEquals(-0.5, correlation);
  }

  private  SimplexSpace<Long> getSpace(int DIMS) {
    //    DistanceMeasure measure = new ChebyshevDistanceMeasure(); 
    //    DistanceMeasure measure = new ManhattanDistanceMeasure(); 
    //    DistanceMeasure measure = new MinkowskiDistanceMeasure(2.5); 
    DistanceMeasure measure = new EuclideanDistanceMeasure();
    return new SimplexSpace<Long>(new OrthonormalHasher(DIMS, 0.05), DIMS);
  }

 /* private  void addUserSimplices(SimplexSpace space, DataModel bcModel) throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions());
    LongPrimitiveIterator lpi = bcModel.getUserIDs();
    while (lpi.hasNext()) {
      Long userID = lpi.nextLong();
      Vector sv = svf.projectUserDense(userID, 3);
      if (null != sv) {
        space.addVector(sv, userID);
      }
    }
  }

  private  void addItemSimplices(SimplexSpace<Long> space, DataModel bcModel) throws TasteException {
    SemanticVectorFactory svf = new SemanticVectorFactory(bcModel, space.getDimensions());
    LongPrimitiveIterator lpi = bcModel.getItemIDs();
    while (lpi.hasNext()) {
      Long itemID = lpi.nextLong();
      Vector sv = svf.getItemVector(itemID, 3, 50);
      if (null != sv)
        space.addVector(sv, itemID);
    }
  }
*/

}
