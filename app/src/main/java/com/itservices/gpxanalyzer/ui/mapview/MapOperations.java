package com.itservices.gpxanalyzer.ui.mapview;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface MapOperations {
    void setCenter(GeoPoint center);
    void setZoom(double zoomLevel);
    void setCenterAndZoom(GeoPoint center, double zoomLevel);
    void setBoundingBox(BoundingBox boundingBox);
    void setBoundingBox(BoundingBox boundingBox, int paddingPx, long animationDurationMs);
    void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent);
    void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent, 
                                 int paddingPx, long animationDurationMs);
} 