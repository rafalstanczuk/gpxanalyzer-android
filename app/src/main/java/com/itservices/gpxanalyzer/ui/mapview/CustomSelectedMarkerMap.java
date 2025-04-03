package com.itservices.gpxanalyzer.ui.mapview;

import android.content.Context;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public final class CustomSelectedMarkerMap extends Marker {
    public CustomSelectedMarkerMap(MapView mapView) {
        super(mapView);
    }

    public CustomSelectedMarkerMap(MapView mapView, Context resourceProxy) {
        super(mapView, resourceProxy);
    }
}
