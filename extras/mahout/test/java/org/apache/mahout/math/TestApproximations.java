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

package org.apache.mahout.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verify that approximations are within 5% of exact versions.
 */

public class TestApproximations extends Assert {
  static double EPSILON = 0.05;
  
  @Test
  public void testPow() {
    double pow = Approximations.pow(14, 2);
    approx(pow, Math.pow(14, 2), EPSILON);
    pow = Approximations.pow(300, 20);
    approx(pow, Math.pow(300, 20), EPSILON);
    pow = Approximations.pow(30000, 0.2);
    approx(pow, Math.pow(30000, 0.2), EPSILON);
    pow = Approximations.pow(200, 0.00001);
    approx(pow, Math.pow(200, 0.00001), EPSILON);
  }
  
  @Test
  public void testExp() {
    double exp = Approximations.exp(14);
    approx(exp, Math.exp(14), EPSILON);
    exp = Approximations.exp(300);
    approx(exp, Math.exp(300), EPSILON);
    exp = Approximations.exp(30000);
    approx(exp, Math.exp(30000), EPSILON);
    exp = Approximations.exp(0.2);
    approx(exp, Math.exp(0.2), EPSILON);
  }
  
  @Test
  public void testLog() {
    double log = Approximations.log(14);
    approx(log, Math.log(14), EPSILON);
    log = Approximations.log(300);
    approx(log, Math.log(300), EPSILON);
    log = Approximations.log(30000);
    approx(log, Math.log(30000), EPSILON);
    log = Approximations.log(0.2);
    approx(log, Math.log(0.2), EPSILON);
  }
  
  private void approx(double approx, double exact, double eps) {
    double greater = Math.max(approx, exact);
    double lesser = Math.min(approx, exact);
    assertTrue(greater / lesser < 1.0 + eps); 
  }

}
