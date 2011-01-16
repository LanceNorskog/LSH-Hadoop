package working;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/*
 * Compare Recommenders and DataModels. 
 * Allow different formulae for evaluation.
 */

public interface RecommenderEvaluator {

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

