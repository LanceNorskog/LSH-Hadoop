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

package working;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli2.OptionException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.example.TasteOptionParser;
import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
import org.apache.mahout.cf.taste.impl.common.CompactRunningAverageAndStdDev;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import working.RecommenderEvaluator;

/**
 * <p>A simple example "runner" class which will evaluate the performance of the current
 * implementation of {@link GroupLensRecommender}.</p>
 */
public final class GroupLensRecommenderEvaluatorRunner {
  
  private static final Logger log = LoggerFactory.getLogger(GroupLensRecommenderEvaluatorRunner.class);
  
  private GroupLensRecommenderEvaluatorRunner() {
    // do nothing
  }
  
  public static void main(String... args) throws IOException, TasteException, OptionException {
 long start = System.currentTimeMillis();
    RecommenderEvaluator evaluator = new PreferenceBasedRecommenderEvaluator();
    File ratingsFile = TasteOptionParser.getRatings(args);
    DataModel model = ratingsFile == null ? new GroupLensDataModel() : new GroupLensDataModel(ratingsFile);
    GroupLensRecommenderBuilder recommenderBuilder = new GroupLensRecommenderBuilder();
    DataModel trainingModel = new SamplingDataModel(model, 0.0, 0.7);
    DataModel testModel = new SamplingDataModel(model, 0.4, 1.0);
    Recommender trainingRecommender = recommenderBuilder.buildRecommender(trainingModel);
    Recommender testRecommender = recommenderBuilder.buildRecommender(testModel);
    RunningAverage tracker = new CompactRunningAverageAndStdDev();
    evaluator.evaluate(trainingRecommender, testRecommender, 50, tracker, RecommenderEvaluator.Formula.NONE);
    double average = tracker.getAverage();
    log.info(String.valueOf(average));
    System.err.println("ms: " + (System.currentTimeMillis() - start));
  }
  
}
