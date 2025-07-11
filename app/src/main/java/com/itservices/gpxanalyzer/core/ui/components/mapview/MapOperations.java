package com.itservices.gpxanalyzer.core.ui.components.mapview;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * Defines a contract for common map control operations.
 * Implementations of this interface provide ways to manipulate the map's view,
 * such as setting the center, zoom level, or fitting the view to a specific bounding box.
 */
public interface MapOperations {
    /**
     * Sets the center of the map view to the specified geographical point.
     *
     * @param center The {@link GeoPoint} to center the map on.
     */
    void setCenter(GeoPoint center);

    /**
     * Sets the zoom level of the map view.
     *
     * @param zoomLevel The desired zoom level (specific values depend on the map provider).
     */
    void setZoom(double zoomLevel);

    /**
     * Sets both the center and zoom level of the map view in a single operation.
     *
     * @param center    The {@link GeoPoint} to center the map on.
     * @param zoomLevel The desired zoom level.
     */
    void setCenterAndZoom(GeoPoint center, double zoomLevel);

    /**
     * Adjusts the map view to display the specified bounding box.
     * The exact zoom level and padding may vary depending on the implementation.
     *
     * @param boundingBox The {@link BoundingBox} to display.
     */
    void setBoundingBox(BoundingBox boundingBox);

    /**
     * Adjusts the map view to display the specified bounding box with defined padding and animation.
     *
     * @param boundingBox         The {@link BoundingBox} to display.
     * @param paddingPx           Padding around the bounding box in pixels.
     * @param animationDurationMs Duration of the zoom/pan animation in milliseconds.
     */
    void setBoundingBox(BoundingBox boundingBox, int paddingPx, long animationDurationMs);

    /**
     * Adjusts the map view to display the specified bounding box with padding defined as a percentage of the view dimensions.
     *
     * @param boundingBox    The {@link BoundingBox} to display.
     * @param paddingPercent Padding around the bounding box as a percentage (e.g., 0.1 for 10%).
     */
    void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent);

    /**
     * Adjusts the map view to display the specified bounding box with both percentage and pixel padding, and optional animation.
     *
     * @param boundingBox         The {@link BoundingBox} to display.
     * @param paddingPercent      Padding as a percentage (applied first).
     * @param paddingPx           Additional padding in pixels (applied after percentage padding).
     * @param animated            Whether to animate the transition.
     * @param animationDurationMs Duration of the animation if animated is true.
     */
    void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent,
                                 int paddingPx, boolean animated, long animationDurationMs);
} 