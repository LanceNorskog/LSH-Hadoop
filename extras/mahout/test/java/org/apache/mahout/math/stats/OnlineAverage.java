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

package org.apache.mahout.math.stats;

/**
 * <p>
 * Extends {@link FullRunningAverage} to add a running standard deviation computation.
 * Uses Welford's method, as described at http://www.johndcook.com/standard_deviation.html
 * </p>
 */
public final class OnlineAverage {
  
  private double stdDev = Double.NaN;
  private double mk;
  private double sk;
  private int count;
  private double average;
  
  public OnlineAverage() {
    count = 0;
    average = Double.NaN;
  }  
  
  /**
   * @param datum
   *          new item to add to the running average
   */
  public void addDatum(double datum) {
    if (++count == 1) {
      average = datum;
    } else {
      average = average * (count - 1) / count + datum / count;
    }
    int count = getCount();
    if (count == 1) {
      mk = datum;
      sk = 0.0;
    } else {
      double oldmk = mk;
      double diff = datum - oldmk;
      mk += diff / count;
      sk += diff * (datum - mk);
    }
    recomputeStdDev();
  }
  
  /**
   * @param datum
   *          item to remove to the running average
   * @throws IllegalStateException
   *           if count is 0
   */
  public void removeDatum(double datum) {
    int oldCount = getCount();
    if (count == 0) {
      throw new IllegalStateException();
    }
    if (--count == 0) {
      average = Double.NaN;
    } else {
      average = average * (count + 1) / count - datum / count;
    }    double oldmk = mk;
    mk = (oldCount * oldmk - datum) / (oldCount - 1);
    sk -= (datum - mk) * (datum - oldmk);
    recomputeStdDev();
  }
  
  private void recomputeStdDev() {
    int count = getCount();
    if (count > 1) {
      stdDev = Math.sqrt(sk / (count - 1));
    } else {
      stdDev = Double.NaN;
    }
  }
  
  public int getCount() {
    return count;
  }
  
  public double getAverage() {
    return average;
  }
  
  public double getStandardDeviation() {
    return stdDev;
  }
  
  @Override
  public String toString() {
    return String.valueOf(String.valueOf(getAverage()) + ',' + stdDev);
  }
  
}
