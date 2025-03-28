package com.itservices.gpxanalyzer.data.cache;


import com.itservices.gpxanalyzer.data.statistics.GeoPointStatistics;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GeoPointCachedProvider {

    private final Vector<GeoPoint> geoPointVector = new Vector<>();

    GeoPointStatistics geoPointStatistics = new GeoPointStatistics();

    @Inject
    public GeoPointCachedProvider() {
    }

    public void init() {
        geoPointStatistics = new GeoPointStatistics();
        geoPointVector.clear();
    }

    public void accept(GeoPoint point) {
        geoPointVector.add(point);
        geoPointStatistics.accept(point);
    }

    public void acceptAll(List<GeoPoint> points) {
        this.geoPointVector.clear();
        this.geoPointVector.addAll(points);
        geoPointStatistics.acceptAll(points);
    }

    public void reset() {
        this.geoPointVector.clear();
        geoPointStatistics.reset();
    }

    public boolean isEmpty() {
        return geoPointStatistics.isEmpty();
    }

    public GeoPointStatistics getGeoPointStatistics() {
        return geoPointStatistics;
    }

    public Vector<GeoPoint> getPointVector() {
        return geoPointVector;
    }
}
