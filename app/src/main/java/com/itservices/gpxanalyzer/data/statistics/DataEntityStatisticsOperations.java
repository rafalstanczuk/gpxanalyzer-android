package com.itservices.gpxanalyzer.data.statistics;

import com.itservices.gpxanalyzer.data.raw.DataEntity;

/**
 * Interface defining operations for collecting and analyzing statistics from DataEntity objects.
 * This interface provides methods for tracking minimum, maximum, and average values
 * for different data measures within DataEntity objects.
 *
 * The interface is designed to be used in conjunction with the GPX analyzer application
 * for processing and analyzing data from GPX tracks and other geographical data sources.
 */
public interface DataEntityStatisticsOperations {
    /**
     * Accepts a single DataEntity and updates the statistics accordingly.
     *
     * @param point The DataEntity to process, or null to skip
     */
    void accept(DataEntity point);

    /**
     * Resets all statistics to their initial state.
     */
    void reset();

    /**
     * Checks if any data entities have been processed.
     *
     * @return true if no data entities have been processed, false otherwise
     */
    boolean isEmpty();

    /**
     * Returns the total number of data entities that have been processed.
     *
     * @return The count of processed data entities
     */
    long getCount();

    /**
     * Returns the minimum value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The minimum value for the specified measure
     */
    double getMin(int index);

    /**
     * Returns the maximum value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The maximum value for the specified measure
     */
    double getMax(int index);

    /**
     * Returns the average value for the specified measure index.
     *
     * @param index The index of the measure to get statistics for
     * @return The average value for the specified measure, or 0.0 if no data has been processed
     */
    double getAverage(int index);
}