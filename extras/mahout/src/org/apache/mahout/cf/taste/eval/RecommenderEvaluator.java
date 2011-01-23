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

}

