package org.apache.mahout.math.stats.correlation;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

public class HoeffdingCorrelation {
  
  public double correlation(final double[] xArray, final double[] yArray)
  throws IllegalArgumentException {
    double xMean = mean(xArray);
    double yMean = mean(yArray);
    if (xArray.length == yArray.length && xArray.length > 1) {
      double top = 0;
      double bottomX = 0;
      double bottomY = 0;
      for(int i = 0; i < xArray.length; i++) {
        double xDelta = xArray[i] - xMean;
        double yDelta = yArray[i] - yMean;
        double num = xDelta*yDelta;
        top += num;
        bottomX += xDelta * xDelta;
        bottomY += yDelta * yDelta;
      }
      double measure = top / Math.sqrt(Math.abs(bottomX) * Math.abs(bottomY));
      return measure;
    }
    else {
      throw MathRuntimeException.createIllegalArgumentException(
          "invalid array dimensions. xArray has size {0}; yArray has {1} elements",
          xArray.length, yArray.length);
    }
  }
  
  private double mean(double[] xArray) {
    double mean = 0;
    for(int i = 0; i < xArray.length; i++) {
      mean += xArray[i];
    }
    return mean/xArray.length;
  }
  
}
