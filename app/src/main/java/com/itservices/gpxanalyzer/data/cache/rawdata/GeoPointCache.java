package com.itservices.gpxanalyzer.data.cache.rawdata;


import com.itservices.gpxanalyzer.data.model.entity.GeoPointEntity;
import com.itservices.gpxanalyzer.data.model.statistics.GeoPointStatistics;

import org.osmdroid.util.GeoPoint;

import java.util.Comparator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GeoPointCache {

    private final ConcurrentMap<Long, GeoPointEntity> geoPointMap = new ConcurrentHashMap<>();

    private final Vector<GeoPoint> geoPointVector = new Vector<>();

    GeoPointStatistics geoPointStatistics = new GeoPointStatistics();

    @Inject
    public GeoPointCache() {
    }

    public void init() {
        geoPointStatistics = new GeoPointStatistics();
        geoPointVector.clear();
        geoPointMap.clear();
    }

    public void accept(GeoPointEntity geoPointEntity) {
        geoPointMap.put(geoPointEntity.getDataEntity().timestampMillis(), geoPointEntity);
        geoPointVector.add(geoPointEntity);
        geoPointStatistics.accept(geoPointEntity);
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

    public Vector<GeoPoint> getGeoPointVector() {
        return geoPointVector;
    }

    public GeoPointEntity getGeoPointForTime(long timestampMillis) {
        return geoPointMap.get(timestampMillis);
    }

    public Vector<GeoPoint> get(long timestampMillisStart, long timestampMillisEnd) {
        if (timestampMillisEnd < timestampMillisStart) {
            return new Vector<>();
        }

        Vector<GeoPoint> allGeoPoint = getGeoPointVector();
            GeoPointEntity first = (GeoPointEntity)(allGeoPoint.firstElement());
            GeoPointEntity last = (GeoPointEntity)(allGeoPoint.lastElement());

        if ( first.getDataEntity().timestampMillis() == timestampMillisStart
                &&
             last.getDataEntity().timestampMillis() == timestampMillisEnd
        ) {
            return allGeoPoint;
        }

        return geoPointMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= timestampMillisStart && entry.getKey() <= timestampMillisEnd)
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparingLong( geopoint -> geopoint.getDataEntity().timestampMillis()))
                .collect(Collectors.toCollection(Vector::new));
    }
}
