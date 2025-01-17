/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.min;

import java.util.Random;

public class TestingUtil2 {
  public static Random rand = new Random();

  /**
   *
   * @param x1 given
   * @param x2 given
   * @param points given
   * @param log given
   * @return double array
   */
  public static double[] evenlySpaced(final double x1, final double x2, final int points,
      final boolean log) {
    if (points <= 0) {
      throw new IllegalArgumentException("points must be > 0");
    }
    final double[] out = new double[points];
    out[0] = x1;
    if (points == 1) {
      return out;
    }
    if (points == 2) {
      out[1] = x2;
      return out;
    }
    // 3 or more
    if (log) {
      if ((x2 <= 0) || (x1 <= 0)) {
        throw new IllegalArgumentException("x1 and x2 must be > 0.");
      }
      final double logMin = log(x1);
      final double delta = log(x2 / x1) / (points - 1);
      for (int i = 1; i < points; i++) {
        out[i] = exp(mXplusY(delta, i, logMin));
      }
    } else { // linear
      final double delta = (x2 - x1) / (points - 1);
      for (int i = 1; i < points; i++) {
        out[i] = mXplusY(delta, i, x1);
      }
    }
    return out;
  }

  /**
   *
   * @param x1 given
   * @param x2 given
   * @param points given
   * @param log given
   * @return double array
   */
  public static double[] uniformRandom(final double x1, final double x2, final int points,
      final boolean log) {
    if (points <= 0) {
      throw new IllegalArgumentException("points must be > 0");
    }
    final double[] out = new double[points];

    if (log) {
      if ((x2 <= 0) || (x1 <= 0)) {
        throw new IllegalArgumentException("x1 and x2 must be > 0.");
      }
      final double logX1 = log(x1);
      final double delta = log(x2) - logX1;
      for (int i = 0; i < points; i++) {
        out[i] = exp(mXplusY(delta, rand.nextDouble(), logX1));
      }
    } else {
      final double delta = x2 - x1;
      for (int i = 0; i < points; i++) {
        out[i] = mXplusY(delta, rand.nextDouble(), x1);
      }
    }
    return out;
  }

  /**
   * Scales a value v in the range 0 to 1 into the range min to max.
   *
   * @param v
   *          the given value of x in the range 0 to 1.
   * @param min
   *          the minimum endpoint of the resulting range
   * @param max
   *          the maximum endpoint of the resulting range
   * @return scaled x
   */
  public static double scale(final double v, final double min, final double max) {
    final double delta = abs(min - max);
    final double actMin = min(min, max);
    return mXplusY(delta, v, actMin);
  }

  public static final double linXlinYline(final double slope, final double x, final double x0,
      final double y0) {
    return mXplusY(slope, (x - x0), y0);
  }

  public static final double logXlogYline(final double slope, final double x, final double x0,
      final double y0) {
    return exp(mXplusY(slope, log(x / x0), log(y0)));
  }

  public static final double mXplusY(final double m, final double x, final double y) {
    return (m * x) + y;
  }

}
