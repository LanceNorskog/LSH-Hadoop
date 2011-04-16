package org.apache.mahout.math;

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

/**
 * Inaccurate but fast implementations of important formulas. 
 * These depend on bit-twiddling the contents of an IEEE-754 double.
 * 
 * http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
 * http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
 * 
 * Caveats:
 * log(n) is invalid where n < 1. For n > 1, error starts at 5% and rapidly drops.
 * exp(n) is valid from 0.1 < n < 800. 800 is the limit for exp(n).
 * pow(n, exp) is valid for exp < 1
 * 
 * Throw exception if invalid value given. The alternative is to switch to the accurate version.
 * But the point of using Approximations is speed- invisibly switching to the slow versions is not what I would want.
 */

public class Approximations {
  
  public static double pow(double value, double exponent) {
    if (exponent > 2.0)
      throw new RuntimeException("Approximations.pow(n) only valid where n < 1");
    final int x = (int) (Double.doubleToLongBits(value) >> 32);
    final int y = (int) (exponent * (x - 1072632447) + 1072632447);
    return Double.longBitsToDouble(((long) y) << 32);
  }
  
  public static double exp(double value) {
    if (value < 0.1)
      throw new RuntimeException("Approximations.exp(n) only valid where n > 1");
    final long tmp = (long) (1512775 * value + (1072693248 - 60801));
    return Double.longBitsToDouble(tmp << 32);
  }
  
  public static double log(double val) {
    if (val > 2)
      throw new RuntimeException("Approximations.log(n) only valid where n < 1");
    final double x = (Double.doubleToLongBits(val) >> 32);
    return (x - 1072632447) / 1512775;
  }
  
}
