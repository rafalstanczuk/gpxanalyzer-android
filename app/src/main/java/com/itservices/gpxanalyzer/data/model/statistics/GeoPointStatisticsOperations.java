package com.itservices.gpxanalyzer.data.model.statistics;

import org.osmdroid.util.GeoPoint;
import java.util.List;

/**
 * Interface defining operations for collecting and analyzing geographical point statistics.
 * This interface provides methods for tracking minimum, maximum, and average coordinates,
 * as well as calculating center points and bounding box corners for a set of geographical points.
 *
 * The interface is designed to be used in conjunction with the GPX analyzer application
 * for processing and analyzing geographical track data.
 */
public interface GeoPointStatisticsOperations {
    /**
     * Accepts a single geographical point and updates the statistics accordingly.
     *
     * @param point The GeoPoint to process, or null to skip
     */
    void accept(GeoPoint point);

    /**
     * Accepts a list of geographical points and updates the statistics for all valid points.
     *
     * @param points List of GeoPoints to process, or null to skip
     */
    void acceptAll(List<GeoPoint> points);

    /**
     * Resets all statistics to their initial state.
     */
    void reset();

    /**
     * Checks if any points have been processed.
     *
     * @return true if no points have been processed, false otherwise
     */
    boolean isEmpty();

    /**
     * Returns the total number of points that have been processed.
     *
     * @return The count of processed points
     */
    long getCount();

    /**
     * Returns the minimum latitude among all processed points.
     *
     * @return The minimum latitude value
     */
    double getMinLatitude();

    /**
     * Returns the maximum latitude among all processed points.
     *
     * @return The maximum latitude value
     */
    double getMaxLatitude();

    /**
     * Returns the minimum longitude among all processed points.
     *
     * @return The minimum longitude value
     */
    double getMinLongitude();

    /**
     * Returns the maximum longitude among all processed points.
     *
     * @return The maximum longitude value
     */
    double getMaxLongitude();

    /**
     * Returns the average latitude of all processed points.
     *
     * @return The average latitude value, or 0.0 if no points have been processed
     */
    double getAverageLatitude();

    /**
     * Returns the average longitude of all processed points.
     *
     * @return The average longitude value, or 0.0 if no points have been processed
     */
    double getAverageLongitude();

    /**
     * Returns the center point of all processed points.
     *
     * @return The center GeoPoint, or null if no points have been processed
     */
    GeoPoint getCenter();

    /**
     * Returns the southwest corner of the bounding box containing all processed points.
     *
     * @return The southwest corner GeoPoint, or null if no points have been processed
     */
    GeoPoint getSouthwestCorner();

    /**
     * Returns the northeast corner of the bounding box containing all processed points.
     *
     * @return The northeast corner GeoPoint, or null if no points have been processed
     */
    GeoPoint getNortheastCorner();
} 