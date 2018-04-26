package com.yahoo.sketches.characterization.quantiles;

public class FloatRankCalculator {

  private final float[] values;
  private int nLess;

  // assumes that values are sorted
  FloatRankCalculator(final float[] values) {
    this.values = values;
  }

  double getRank(final float value) {
    while (nLess < values.length && values[nLess] < value) {
      nLess++;
    }
    return (double) nLess / values.length;
  }

}
