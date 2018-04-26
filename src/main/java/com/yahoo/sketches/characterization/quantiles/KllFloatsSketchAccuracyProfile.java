package com.yahoo.sketches.characterization.quantiles;

import java.util.Arrays;
import java.util.Random;

import com.yahoo.sketches.characterization.Properties;
import com.yahoo.sketches.characterization.quantiles.DataGenerator.Mode;
import com.yahoo.sketches.kll.KllFloatsSketch;

public class KllFloatsSketchAccuracyProfile extends QuantilesAccuracyProfile {

  private int k;
  private float[] inputValues;
  private DataGenerator gen;

  @Override
  void configure(final Properties props) {
    k = Integer.parseInt(props.mustGet("K"));
    gen = new DataGenerator(Mode.valueOf(props.mustGet("data")));
  }

  @Override
  void prepareTrial(final int streamLength) {
    inputValues = new float[streamLength];
  }

  @Override
  double doTrial() {
    gen.fillArray(inputValues);

    // build sketch
    final KllFloatsSketch sketch = new KllFloatsSketch(k);
    for (int i = 0; i < inputValues.length; i++) {
      sketch.update(inputValues[i]);
    }

    Arrays.sort(inputValues);

    // query sketch and gather results
    double maxRankError = 0;
    final FloatRankCalculator rank = new FloatRankCalculator(inputValues);
    for (int i = 0; i < inputValues.length; i++) {
      final double trueRank = rank.getRank(inputValues[i]);
      final double estRank = sketch.getRank(inputValues[i]);
      maxRankError = Math.max(maxRankError, Math.abs(trueRank - estRank));
    }
    return maxRankError;
  }

  static final Random rnd = new Random();

  static void shuffle(final float[] array) {
    for (int i = 0; i < array.length; i++) {
      final int r = rnd.nextInt(i + 1);
      swap(array, i, r);
    }
  }

  private static void swap(final float[] array, final int i1, final int i2) {
    final float value = array[i1];
    array[i1] = array[i2];
    array[i2] = value;
  }

}
