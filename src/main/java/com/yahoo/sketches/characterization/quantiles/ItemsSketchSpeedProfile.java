package com.yahoo.sketches.characterization.quantiles;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import com.yahoo.memory.WritableMemory;
import com.yahoo.sketches.ArrayOfDoublesSerDe;
import com.yahoo.sketches.ArrayOfItemsSerDe;
import com.yahoo.sketches.characterization.Properties;
import com.yahoo.sketches.quantiles.ItemsSketch;

public class ItemsSketchSpeedProfile extends QuantilesSpeedProfile {

  private static final Comparator<Double> COMPARATOR = Comparator.naturalOrder();
  private static final ArrayOfItemsSerDe<Double> SERDE = new ArrayOfDoublesSerDe();
  private static final Random rnd = new Random();
  private int k;
  private double[] inputValues;
  private int numQueryValues;
  private Double[] queryValues;

  long buildTimeNs;
  long updateTimeNs;
  long getQuantilesTimeNs;
  long getCdfTimeNs;
  long getRankTimeNs;
  long serializeTimeNs;
  long deserializeTimeNs;
  long numRetainedItems;
  long serializedSizeBytes;

  @Override
  void configure(final int k, final int numQueryValues, final Properties properties) {
    this.k = k;
    this.numQueryValues = numQueryValues;
  }

  @Override
  void prepareTrial(final int streamLength) {
    // prepare input data
    inputValues = new double[streamLength];
    for (int i = 0; i < streamLength; i++) {
      inputValues[i] = rnd.nextDouble();
    }
    // prepare query data that must be ordered
    queryValues = new Double[numQueryValues];
    for (int i = 0; i < numQueryValues; i++) {
      queryValues[i] = rnd.nextDouble();
    }
    Arrays.sort(queryValues);
    resetStats();
  }

  @SuppressWarnings("unused")
  @Override
  void doTrial() {
    DoublesSketchAccuracyProfile.shuffle(inputValues);

    final long startBuild = System.nanoTime();
    final ItemsSketch<Double> sketch = ItemsSketch.getInstance(k, COMPARATOR);
    final long stopBuild = System.nanoTime();
    buildTimeNs += stopBuild - startBuild;

    final long startUpdate = System.nanoTime();
    for (int i = 0; i < inputValues.length; i++) {
      sketch.update(inputValues[i]);
    }
    final long stopUpdate = System.nanoTime();
    updateTimeNs += stopUpdate - startUpdate;

    final long startGetQuantiles = System.nanoTime();
    sketch.getQuantiles(numQueryValues);
    final long stopGetQuantiles = System.nanoTime();
    getQuantilesTimeNs += stopGetQuantiles - startGetQuantiles;

    final long startGetCdf = System.nanoTime();
    sketch.getCDF(queryValues);
    final long stopGetCdf = System.nanoTime();
    getCdfTimeNs += stopGetCdf - startGetCdf;

    final long startGetRank = System.nanoTime();
    for (final double value: queryValues) {
      //sketch.getRank(value); //TODO this was not released yet
      final double estRank = sketch.getCDF(new Double[] {value})[0];
    }
    final long stopGetRank = System.nanoTime();
    getRankTimeNs += stopGetRank - startGetRank;

    final long startSerialize = System.nanoTime();
    final byte[] bytes = sketch.toByteArray(SERDE);
    final long stopSerialize = System.nanoTime();
    serializeTimeNs += stopSerialize - startSerialize;

    final WritableMemory mem = WritableMemory.wrap(bytes);
    final long startDeserialize = System.nanoTime();
    ItemsSketch.getInstance(mem, COMPARATOR, SERDE);
    final long stopDeserialize = System.nanoTime();
    deserializeTimeNs += stopDeserialize - startDeserialize;

    // could record the last one since they must be the same
    // but let's average across all trials to see if there is an anomaly
    numRetainedItems += sketch.getRetainedItems();
    serializedSizeBytes += bytes.length;
  }

  @Override
  String getHeader() {
    return "Stream\tTrials\tBuild\tUpdate\tQuant\tCDF\tRank\tSer\tDeser\tItems\tSize";
  }

  @Override
  String getStats(final int streamLength, final int numTrials, final int numQueryValues) {
    return String.format("%d\t%d\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%d\t%d",
      streamLength,
      numTrials,
      (double) buildTimeNs / numTrials,
      (double) updateTimeNs / numTrials / streamLength,
      (double) getQuantilesTimeNs / numTrials / numQueryValues,
      (double) getCdfTimeNs / numTrials / numQueryValues,
      (double) getRankTimeNs / numTrials / numQueryValues,
      (double) serializeTimeNs / numTrials,
      (double) deserializeTimeNs / numTrials,
      numRetainedItems / numTrials,
      serializedSizeBytes / numTrials
    );
  }

  private void resetStats() {
    buildTimeNs = 0;
    updateTimeNs = 0;
    getQuantilesTimeNs = 0;
    getCdfTimeNs = 0;
    getRankTimeNs = 0;
    serializeTimeNs = 0;
    deserializeTimeNs = 0;
    numRetainedItems = 0;
    serializedSizeBytes = 0;
  }

}
