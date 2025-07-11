package com.itservices.gpxanalyzer.core.ui.components.mapview;

import android.content.Context;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * A custom {@link Marker} subclass intended for representing a specifically selected location on the map.
 * Currently, this class does not add specific functionality beyond the base {@link Marker},
 * but serves as a distinct type for selected markers, potentially for custom styling or event handling elsewhere.
 */
public final class CustomSelectedMarkerMap extends Marker {
    /**
     * Constructor that initializes the marker for a specific {@link MapView}.
     *
     * @param mapView The MapView this marker belongs to.
     */
    public CustomSelectedMarkerMap(MapView mapView) {
        super(mapView);
    }

    /**
     * Constructor that initializes the marker for a specific {@link MapView}
     * and uses a provided Context for resource access (deprecated in newer osmdroid versions,
     * prefer the constructor without Context if possible).
     *
     * @param mapView       The MapView this marker belongs to.
     * @param resourceProxy The context used for resource lookups (optional/deprecated).
     */
    public CustomSelectedMarkerMap(MapView mapView, Context resourceProxy) {
        super(mapView, resourceProxy);
    }
}
