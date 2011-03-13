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

package org.apache.mahout.cf.taste.impl.eval;

import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.eval.PreferenceBasedRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.ItemAverageRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.junit.Test;
import static org.apache.mahout.cf.taste.eval.RecommenderEvaluator.Formula;

public final class PreferenceBasedRecommenderEvaluatorTest extends TasteTestCase {
  
  @Test
  public void testEvaluate() throws Exception {
    DataModel model = getDataModel();
    Recommender recommender1 = new SlopeOneRecommender(model);
    Recommender recommender2 = new ItemAverageRecommender(model);
    RecommenderEvaluator evaluator = new PreferenceBasedRecommenderEvaluator();
    
    RunningAverage tracker = new CompactRunningAverage();
    evaluator.evaluate(recommender1, recommender2, 100, tracker, Formula.MEANRANK);
    double eval = tracker.getAverage();
    assertEquals(0.185294508934021, eval, EPSILON);
  }
  
}
