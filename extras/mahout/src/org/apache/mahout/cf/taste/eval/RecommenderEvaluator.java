package org.apache.mahout.cf.taste.eval;

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

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * <p>
 * Implementations of this interface evaluate the quality of a
 * {@link org.apache.mahout.cf.taste.recommender.Recommender}'s recommendations.
 * It can also compare a {@link org.apache.mahout.cf.taste.recommender.Recommender}
 * v.s. a {@link org.apache.mahout.cf.taste.model.DataModel}, which is useful
 * for very dense DataModels.
 * </p>
 */


public interface RecommenderEvaluator {

  /*
   * Different measurement formulae are available for order based evaluation:
   * COMMON     Number of common results in a subsample
   * HAMMING    Sliding Window Hamming (accept values within X nearby slots)
   * BUBBLE     Number of swaps required for a bubble sort
   *                (comparing one order to the other order)
   * 
   * The following two use Statistical Rank, a common algorithm for
   * calculating the similarity of two ordered lists. It correlates 
   * very strongly with the bubble sort measurement.
   * WILCOXON   Wilcoxon/foo/bar
   *            Normal-distribution probability value for matched sets of values.
   *            Based upon:
   *            http://comp9.psych.cornell.edu/Darlington/normscor.htm
   *            Real Wilcoxon rank test requires a lookup table
   * MEANRANK   Mean value of calculated ranks
   * 
   * WILCOXON and MEANRANK return the squart root of the actual value.
   * BUBBLE uses the log, and maybe should use square root instead.
   * 
   * No formula options yet for preference-based evaluation.
   * NONE       Dummy for PreferenceBased.
   */
  enum Formula {NONE, COMMON, HAMMING, BUBBLE, WILCOXON, MEANRANK};

  void evaluate(Recommender recommender1,
      Recommender recommender2, int samples,
      RunningAverage tracker, Formula formula) throws TasteException;

  void evaluate(Recommender recommender, DataModel model,
      int samples, RunningAverage tracker, Formula formula)
      throws TasteException;

  abstract void evaluate(DataModel model1, DataModel model2,
      int samples, RunningAverage tracker, Formula formula)
      throws TasteException;

  /**
   * <p>
   * Evaluates the quality of a {@link org.apache.mahout.cf.taste.recommender.Recommender}'s recommendations.
   * The range of values that may be returned depends on the implementation, but <em>lower</em> values must
   * mean better recommendations, with 0 being the lowest / best possible evaluation, meaning a perfect match.
   * This method does not accept a {@link org.apache.mahout.cf.taste.recommender.Recommender} directly, but
   * rather a {@link RecommenderBuilder} which can build the
   * {@link org.apache.mahout.cf.taste.recommender.Recommender} to test on top of a given {@link DataModel}.
   * </p>
   * 
   * <p>
   * Implementations will take a certain percentage of the preferences supplied by the given {@link DataModel}
   * as "training data". This is typically most of the data, like 90%. This data is used to produce
   * recommendations, and the rest of the data is compared against estimated preference values to see how much
   * the {@link org.apache.mahout.cf.taste.recommender.Recommender}'s predicted preferences match the user's
   * real preferences. Specifically, for each user, this percentage of the user's ratings are used to produce
   * recommendatinos, and for each user, the remaining preferences are compared against the user's real
   * preferences.
   * </p>
   * 
   * <p>
   * For large datasets, it may be desirable to only evaluate based on a small percentage of the data.
   * <code>evaluationPercentage</code> controls how many of the {@link DataModel}'s users are used in
   * evaluation.
   * </p>
   * 
   * <p>
   * To be clear, <code>trainingPercentage</code> and <code>evaluationPercentage</code> are not related. They
   * do not need to add up to 1.0, for example.
   * </p>
   * 
   * @param recommenderBuilder
   *          object that can build a {@link org.apache.mahout.cf.taste.recommender.Recommender} to test
   * @param dataModelBuilder
   *          {@link DataModelBuilder} to use, or if null, a default {@link DataModel}
   *          implementation will be used
   * @param dataModel
   *          dataset to test on
   * @param trainingPercentage
   *          percentage of each user's preferences to use to produce recommendations; the rest are compared
   *          to estimated preference values to evaluate
   *          {@link org.apache.mahout.cf.taste.recommender.Recommender} performance
   * @param evaluationPercentage
   *          percentage of users to use in evaluation
   * @return a "score" representing how well the {@link org.apache.mahout.cf.taste.recommender.Recommender}'s
   *         estimated preferences match real values; <em>lower</em> scores mean a better match and 0 is a
   *         perfect match
   * @throws TasteException
   *           if an error occurs while accessing the {@link DataModel}
   */
  @Deprecated
  double evaluate(RecommenderBuilder recommenderBuilder,
                  DataModelBuilder dataModelBuilder,
                  DataModel dataModel,
                  double trainingPercentage,
                  double evaluationPercentage) throws TasteException;

  @Deprecated
  double evaluate(RecommenderBuilder recommenderBuilder,
                  DataModel trainingModel,
                  DataModel testModel) throws TasteException;

  /**
   * @deprecated see {@link DataModel#getMaxPreference()}
   */
  @Deprecated
  float getMaxPreference();

  @Deprecated
  void setMaxPreference(float maxPreference);

  /**
   * @deprecated see {@link DataModel#getMinPreference()}
   */
  @Deprecated
  float getMinPreference();

  @Deprecated
  void setMinPreference(float minPreference);
  

}

