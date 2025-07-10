package com.itservices.gpxanalyzer.data.model.statistics;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Implementation of {@link GeoPointStatisticsOperations} that provides statistical analysis
 * for a collection of geographical points. This class maintains running statistics including
 * minimum, maximum, and average coordinates, as well as calculates center points and bounding
 * box corners.
 *
 * The class is designed to be used with the GPX analyzer application for processing track data
 * and providing geographical statistics for visualization and analysis purposes.
 *
 * Thread safety: This class is not thread-safe. If concurrent access is required, external
 * synchronization should be provided by the caller.
 */
public class GeoPointStatistics implements GeoPointStatisticsOperations {
    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;
    private double sumLatitude;
    private double sumLongitude;
    private long count;
    private GeoPoint center;
    private GeoPoint southwestCorner;
    private GeoPoint northeastCorner;

    /**
     * Creates a new instance of GeoPointStatistics with initial values.
     * The statistics will be in a reset state until points are accepted.
     */
    @Inject
    public GeoPointStatistics() {
        reset();
    }

    /**
     * Resets all statistics to their initial state. This method should be called
     * when starting to process a new set of points.
     */
    @Override
    public void reset() {
        minLatitude = Double.MAX_VALUE;
        maxLatitude = Double.MIN_VALUE;
        minLongitude = Double.MAX_VALUE;
        maxLongitude = Double.MIN_VALUE;
        sumLatitude = 0.0;
        sumLongitude = 0.0;
        count = 0;
        center = null;
        southwestCorner = null;
        northeastCorner = null;
    }

    /**
     * Accepts a single geographical point and updates the statistics accordingly.
     * If the point is null, it will be ignored.
     *
     * @param point The GeoPoint to process, or null to skip
     */
    @Override
    public void accept(GeoPoint point) {
        if (point == null) {
            return;
        }

        double lat = point.getLatitude();
        double lon = point.getLongitude();

        minLatitude = Math.min(minLatitude, lat);
        maxLatitude = Math.max(maxLatitude, lat);
        minLongitude = Math.min(minLongitude, lon);
        maxLongitude = Math.max(maxLongitude, lon);
        sumLatitude += lat;
        sumLongitude += lon;
        count++;

        updateCorners();
        updateCenter();
    }

    /**
     * Accepts a list of geographical points and updates the statistics for all valid points.
     * If the list is null, no points will be processed.
     *
     * @param points List of GeoPoints to process, or null to skip
     */
    @Override
    public void acceptAll(List<GeoPoint> points) {
        if (points == null) {
            return;
        }
        points.forEach(this::accept);
    }

    /**
     * Updates the corner points of the bounding box based on the current minimum
     * and maximum coordinates.
     */
    private void updateCorners() {
        southwestCorner = new GeoPoint(minLatitude, minLongitude);
        northeastCorner = new GeoPoint(maxLatitude, maxLongitude);
    }

    /**
     * Updates the center point based on the current sum and count of points.
     */
    private void updateCenter() {
        if (count > 0) {
            center = new GeoPoint(sumLatitude / count, sumLongitude / count);
        }
    }

    /**
     * Returns the minimum latitude among all processed points.
     *
     * @return The minimum latitude value
     */
    @Override
    public double getMinLatitude() {
        return minLatitude;
    }

    /**
     * Returns the maximum latitude among all processed points.
     *
     * @return The maximum latitude value
     */
    @Override
    public double getMaxLatitude() {
        return maxLatitude;
    }

    /**
     * Returns the minimum longitude among all processed points.
     *
     * @return The minimum longitude value
     */
    @Override
    public double getMinLongitude() {
        return minLongitude;
    }

    /**
     * Returns the maximum longitude among all processed points.
     *
     * @return The maximum longitude value
     */
    @Override
    public double getMaxLongitude() {
        return maxLongitude;
    }

    /**
     * Returns the average latitude of all processed points.
     *
     * @return The average latitude value, or 0.0 if no points have been processed
     */
    @Override
    public double getAverageLatitude() {
        return count > 0 ? sumLatitude / count : 0.0;
    }

    /**
     * Returns the average longitude of all processed points.
     *
     * @return The average longitude value, or 0.0 if no points have been processed
     */
    @Override
    public double getAverageLongitude() {
        return count > 0 ? sumLongitude / count : 0.0;
    }

    /**
     * Returns the total number of points that have been processed.
     *
     * @return The count of processed points
     */
    @Override
    public long getCount() {
        return count;
    }

    /**
     * Returns the center point of all processed points.
     *
     * @return The center GeoPoint, or null if no points have been processed
     */
    @Override
    public GeoPoint getCenter() {
        return center;
    }

    /**
     * Returns the southwest corner of the bounding box containing all processed points.
     *
     * @return The southwest corner GeoPoint, or null if no points have been processed
     */
    @Override
    public GeoPoint getSouthwestCorner() {
        return southwestCorner;
    }

    /**
     * Returns the northeast corner of the bounding box containing all processed points.
     *
     * @return The northeast corner GeoPoint, or null if no points have been processed
     */
    @Override
    public GeoPoint getNortheastCorner() {
        return northeastCorner;
    }

    /**
     * Checks if any points have been processed.
     *
     * @return true if no points have been processed, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Compares this GeoPointStatistics with another object for equality.
     * Two GeoPointStatistics instances are considered equal if they have the same
     * minimum, maximum, and sum values for both latitude and longitude, as well as
     * the same count and corner points.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPointStatistics that = (GeoPointStatistics) o;
        return Double.compare(that.minLatitude, minLatitude) == 0 &&
               Double.compare(that.maxLatitude, maxLatitude) == 0 &&
               Double.compare(that.minLongitude, minLongitude) == 0 &&
               Double.compare(that.maxLongitude, maxLongitude) == 0 &&
               Double.compare(that.sumLatitude, sumLatitude) == 0 &&
               Double.compare(that.sumLongitude, sumLongitude) == 0 &&
               count == that.count &&
               Objects.equals(center, that.center) &&
               Objects.equals(southwestCorner, that.southwestCorner) &&
               Objects.equals(northeastCorner, that.northeastCorner);
    }

    /**
     * Returns a hash code for this GeoPointStatistics instance.
     * The hash code is based on all the statistical values and corner points.
     *
     * @return A hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(minLatitude, maxLatitude, minLongitude, maxLongitude,
                          sumLatitude, sumLongitude, count, center,
                          southwestCorner, northeastCorner);
    }

    /**
     * Returns a string representation of this GeoPointStatistics instance.
     * The string includes all statistical values and corner points.
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return "GeoPointStatistics{" +
               "minLatitude=" + minLatitude +
               ", maxLatitude=" + maxLatitude +
               ", minLongitude=" + minLongitude +
               ", maxLongitude=" + maxLongitude +
               ", averageLatitude=" + getAverageLatitude() +
               ", averageLongitude=" + getAverageLongitude() +
               ", count=" + count +
               ", center=" + center +
               ", southwestCorner=" + southwestCorner +
               ", northeastCorner=" + northeastCorner +
               '}';
    }
} 