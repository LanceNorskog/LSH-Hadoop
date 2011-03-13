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

import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Evaluate recommender by comparing delta of raw and generated prefs.
 * Can also compare data models.
 */
public final class PreferenceBasedRecommenderEvaluator implements RecommenderEvaluator {
  
  public void evaluate(Recommender recco1,
                       Recommender recco2,
                       int samples,
                       RunningAverage tracker,
                       Formula formula) throws TasteException {
    DataModel model1 = recco1.getDataModel();
    
    LongPrimitiveIterator users = model1.getUserIDs();   
    while (users.hasNext()) {
      long userID = users.nextLong();
      List<RecommendedItem> prefs = recco1.recommend(userID, samples);
      for(RecommendedItem pref: prefs) {
        Float value = recco2.estimatePreference(userID, pref.getItemID());
        if (null == value || value.equals(Float.NaN))
          continue;
        float variance = Math.abs(value - pref.getValue());
        //        System.out.println("userID: " + userID + ", itemID: " + pref.getItemID() + ", value: " + value + ", pref Value: " + pref.getValue());
        tracker.addDatum(variance);
      }
    }
  }
  
  
  public void evaluate(Recommender recco,
                       DataModel model,
                       int samples,
                       RunningAverage tracker,
                       Formula formula) throws TasteException {
    
    LongPrimitiveIterator users = model.getUserIDs();   
    while (users.hasNext()) {
      long userID = users.nextLong();
      PreferenceArray prefs = model.getPreferencesFromUser(userID);
      for(Preference pref: prefs) {
        Float value = recco.estimatePreference(userID, pref.getItemID());
        if (null == value || value.equals(Float.NaN))
          continue;
        float variance = Math.abs(value - pref.getValue());
        //        System.out.println("userID: " + userID + ", itemID: " + pref.getItemID() + ", value: " + value + ", pref Value: " + pref.getValue());
        tracker.addDatum(variance);
      }
    }
  }
  
  
  public void evaluate(DataModel model1,
                       DataModel model2,
                       int samples,
                       RunningAverage tracker,
                       Formula formula) throws TasteException {
    LongPrimitiveIterator users = model1.getUserIDs();
    while (users.hasNext()) {
      long userID = users.nextLong();
      PreferenceArray prefs = model2.getPreferencesFromUser(userID);
      for(Preference pref: prefs) {
        Float value = model2.getPreferenceValue(userID, pref.getItemID());
        if (null == value)
          continue;
        float variance = Math.abs(value - pref.getValue());
        tracker.addDatum(variance);
      }
    }
  }
  
  
  @Override
  public double evaluate(RecommenderBuilder recommenderBuilder,
                         DataModelBuilder dataModelBuilder, DataModel dataModel,
                         double trainingPercentage, double evaluationPercentage)
  throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  @Override
  public double evaluate(RecommenderBuilder recommenderBuilder,
                         DataModel trainingModel, DataModel testModel) throws TasteException {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  @Override
  public float getMaxPreference() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  @Override
  public float getMinPreference() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  @Override
  public void setMaxPreference(float maxPreference) {
    // TODO Auto-generated method stub
    
  }
  
  
  @Override
  public void setMinPreference(float minPreference) {
    // TODO Auto-generated method stub
    
  }
  
}

