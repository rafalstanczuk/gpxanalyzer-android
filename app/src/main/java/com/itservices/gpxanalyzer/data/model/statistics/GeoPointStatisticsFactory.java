package com.itservices.gpxanalyzer.data.model.statistics;

import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * Factory class for creating instances of {@link GeoPointStatistics}.
 * This class provides static methods to create GeoPointStatistics instances
 * from various input sources, such as single points, lists of points, or
 * bounding box corners.
 *
 * The factory is designed to be used with the GPX analyzer application for
 * creating statistics objects from geographical track data.
 *
 * This class cannot be instantiated as it only contains static factory methods.
 */
public class GeoPointStatisticsFactory {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GeoPointStatisticsFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates an empty GeoPointStatistics instance with initial values.
     * The statistics will be in a reset state until points are accepted.
     *
     * @return A new empty GeoPointStatistics instance
     */
    public static GeoPointStatistics createEmpty() {
        return new GeoPointStatistics();
    }

    /**
     * Creates a GeoPointStatistics instance initialized with a single point.
     * If the point is null, an empty statistics instance will be returned.
     *
     * @param point The GeoPoint to initialize the statistics with
     * @return A new GeoPointStatistics instance containing the point's statistics
     */
    public static GeoPointStatistics createFromPoint(GeoPoint point) {
        GeoPointStatistics statistics = new GeoPointStatistics();
        if (point != null) {
            statistics.accept(point);
        }
        return statistics;
    }

    /**
     * Creates a GeoPointStatistics instance initialized with a list of points.
     * If the list is null, an empty statistics instance will be returned.
     *
     * @param points List of GeoPoints to initialize the statistics with
     * @return A new GeoPointStatistics instance containing the points' statistics
     */
    public static GeoPointStatistics createFromPoints(List<GeoPoint> points) {
        GeoPointStatistics statistics = new GeoPointStatistics();
        if (points != null) {
            statistics.acceptAll(points);
        }
        return statistics;
    }

    /**
     * Creates a GeoPointStatistics instance initialized with the corners of a bounding box.
     * If either corner is null, an empty statistics instance will be returned.
     *
     * @param southwest The southwest corner of the bounding box
     * @param northeast The northeast corner of the bounding box
     * @return A new GeoPointStatistics instance containing the bounding box statistics
     */
    public static GeoPointStatistics createFromBoundingBox(GeoPoint southwest, GeoPoint northeast) {
        GeoPointStatistics statistics = new GeoPointStatistics();
        if (southwest != null && northeast != null) {
            statistics.accept(southwest);
            statistics.accept(northeast);
        }
        return statistics;
    }
} 