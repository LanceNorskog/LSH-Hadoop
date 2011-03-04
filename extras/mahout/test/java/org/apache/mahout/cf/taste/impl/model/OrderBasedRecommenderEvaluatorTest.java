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

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.OrderBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.ItemAverageRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.ConjugateGradientOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.junit.Test;
import static org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula;

public final class OrderBasedRecommenderEvaluatorTest extends TasteTestCase {

  @Test
  public void testEvaluate() throws Exception {
    DataModel model = getDataModelLarge();
    Recommender recommender1 = buildKNNRecommender(model);
    Recommender recommender2 = new SlopeOneRecommender(getDataModelLarge());
    RecommenderEvaluator evaluator =
        new OrderBasedRecommenderEvaluator();
    
    checkFormula(recommender1, recommender2, evaluator, 0.20000000298023224, Formula.MEANRANK);
    checkFormula(recommender1, recommender2, evaluator, -1.0, Formula.NONE);
    checkFormula(recommender1, recommender2, evaluator, 0.17320507764816284, Formula.WILCOXON);
    checkFormula(recommender1, recommender2, evaluator, 2.0, Formula.HAMMING);
    checkFormula(recommender1, recommender2, evaluator, Double.NEGATIVE_INFINITY, Formula.BUBBLE);
    checkFormula(recommender1, recommender2, evaluator, 0.13862943649291992, Formula.COMMON);
  }

  private void checkFormula(Recommender recommender1,
                            Recommender recommender2,
                            RecommenderEvaluator evaluator,
                            double expected,
                            Formula formula) throws TasteException {
    RunningAverage tracker = new CompactRunningAverage();
    evaluator.evaluate(recommender1, recommender2, 100, tracker, formula);
    double eval = tracker.getAverage();
    assertEquals(expected, eval, EPSILON);
  }
  
  private static Recommender buildKNNRecommender(DataModel dataModel) throws TasteException {
    ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
    Optimizer optimizer = new ConjugateGradientOptimizer();
    return new KnnItemBasedRecommender(dataModel, similarity, optimizer, 5);
  }

  protected static DataModel getDataModelLarge() {
    return getDataModel(
      new long[] {1, 2, 3, 4, 5, 6, 7, 8},
      new Double[][] {
              {0.1, 0.2},
              {0.2, 0.3, 0.3, 0.6},
              {0.4, 0.4, 0.5, 0.9},
              {0.1, 0.4, 0.5, 0.8},
              {0.2, 0.3, 0.7},
              {0.2, 0.3, 0.4,},
              {0.1, 0.2, 1.0},
              {0.9, 0.3, 1.0},
              {0.1, 0.1, 1.1},
              {0.2, 0.2, 0.6,},
      });
  }

}
