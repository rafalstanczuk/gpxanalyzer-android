package com.itservices.gpxanalyzer.ui.components.mapview;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

/**
 * Interface defining operations for managing map overlays such as markers and polylines.
 * This interface provides methods for adding, removing, and clearing various types of
 * map overlays, which are used to display geographical data on the map.
 *
 * The interface is designed to be implemented by map view wrapper classes that provide
 * a simplified interface for working with map overlays in the GPX analyzer application.
 */
public interface MapOverlayOperations {
    /**
     * Adds a marker to the map at the specified position with the given title.
     *
     * @param position The geographical position where the marker should be placed
     * @param title The text to display when the marker is tapped
     */
    Marker addMarker(GeoPoint position, String title);

    /**
     * Adds a polyline to the map connecting the specified points.
     *
     * @param points List of geographical points that define the polyline
     * @param color The color of the polyline in ARGB format
     * @param width The width of the polyline in pixels
     */
    void addPolyline(List<GeoPoint> points, int color, float width);

    /**
     * Removes all markers from the map.
     */
    void clearMarkers();

    /**
     * Removes all polylines from the map.
     */
    void clearPolylines();

    /**
     * Removes all overlays (both markers and polylines) from the map.
     */
    void clearAll();
} 