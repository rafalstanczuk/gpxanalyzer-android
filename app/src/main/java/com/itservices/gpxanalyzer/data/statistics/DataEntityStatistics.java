package com.itservices.gpxanalyzer.data.statistics;

import com.itservices.gpxanalyzer.data.entity.DataEntity;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link DataEntityStatisticsOperations} that provides statistical analysis
 * for a collection of DataEntity objects. This class maintains running statistics including
 * minimum, maximum, and average values for multiple measures within each DataEntity.
 *
 * The class is designed to be used with the GPX analyzer application for processing track data
 * and providing statistical analysis for various measurements such as speed, elevation, etc.
 *
 * Thread safety: This class is not thread-safe. If concurrent access is required, external
 * synchronization should be provided by the caller.
 */
public class DataEntityStatistics implements DataEntityStatisticsOperations {

    private double[] min;
    private double[] max;

    private double[] sum;
    private long count;

    /**
     * Private constructor to prevent instantiation without specifying the number of measures.
     */
    private DataEntityStatistics(){}

    /**
     * Creates a new instance of DataEntityStatistics with the specified number of measures.
     *
     * @param nPrimaryIndexes The number of different measures to track
     */
    public DataEntityStatistics(int nPrimaryIndexes) {
        min = new double[nPrimaryIndexes];
        max = new double[nPrimaryIndexes];
        sum = new double[nPrimaryIndexes];

        resetValues();
    }

    /**
     * Calculates the standard deviation for a specific measure across a list of DataEntity objects.
     *
     * @param primaryDataIndex The index of the measure to calculate standard deviation for
     * @param dataEntityList The list of DataEntity objects to analyze
     * @return The standard deviation of the specified measure
     */
    public static double getStdDev(int primaryDataIndex, List<DataEntity> dataEntityList) {
        double[] values = new double[dataEntityList.size()];

        // Extract values into an array
        Arrays.setAll(values, i -> dataEntityList.get(i).getMeasures().get(primaryDataIndex).value());

        // Use Apache Commons Math StandardDeviation class
        StandardDeviation stdDev = new StandardDeviation(false); // 'false' means population std dev
        return stdDev.evaluate(values);
    }

    /**
     * Resets all statistics to their initial state.
     */
    @Override
    public void reset() {
        min = new double[min.length];
        max = new double[min.length];
        sum = new double[min.length];

        resetValues();
    }

    /**
     * Resets all statistical values to their initial state.
     */
    private void resetValues() {
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, Double.MIN_VALUE);
        Arrays.fill(sum, 0.0);
        count = 0;
    }

    /**
     * Accepts a single DataEntity and updates the statistics accordingly.
     * If the entity is null, it will be ignored.
     *
     * @param dataEntity The DataEntity to process, or null to skip
     */
    @Override
    public void accept(DataEntity dataEntity) {
        if (dataEntity == null) {
            return;
        }

        for (int i = 0; i < dataEntity.getMeasures().size(); i++) {
            double value = dataEntity.getMeasures().get(i).value();
            min[i] = Math.min(min[i], value);
            max[i] = Math.max(max[i], value);
            sum[i] += value;
        }

        count++;
    }

    public void acceptAll(List<DataEntity> points) {
        if (points == null) {
            return;
        }
        points.forEach(this::accept);
    }

    /**
     * Returns the minimum value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The minimum value for the specified measure
     */
    @Override
    public double getMin(int index) {
        return min[index];
    }

    /**
     * Returns the maximum value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The maximum value for the specified measure
     */
    @Override
    public double getMax(int index) {
        return max[index];
    }

    /**
     * Returns the average value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The average value for the specified measure, or 0.0 if no data has been processed
     */
    @Override
    public double getAverage(int index) {
        return count > 0 ? sum[index] / count : 0.0;
    }

    /**
     * Returns the total number of data entities that have been processed.
     *
     * @return The count of processed data entities
     */
    @Override
    public long getCount() {
        return count;
    }

    /**
     * Checks if any data entities have been processed.
     *
     * @return true if no data entities have been processed, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return count == 0;
    }
}