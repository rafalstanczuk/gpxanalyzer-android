package com.itservices.gpxanalyzer.data.entity;

import android.location.Location;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GeoPointEntity extends GeoPoint {
    private final DataEntity dataEntity;

    public GeoPointEntity(double aLatitude, double aLongitude, DataEntity dataEntity) {
        super(aLatitude, aLongitude);
        this.dataEntity = dataEntity;
    }

    public GeoPointEntity(double aLatitude, double aLongitude, double aAltitude, DataEntity dataEntity) {
        super(aLatitude, aLongitude, aAltitude);
        this.dataEntity = dataEntity;
    }

    public GeoPointEntity(Location aLocation, DataEntity dataEntity) {
        super(aLocation);
        this.dataEntity = dataEntity;
    }

    public GeoPointEntity(GeoPoint aGeopoint, DataEntity dataEntity) {
        super(aGeopoint);
        this.dataEntity = dataEntity;
    }

    public GeoPointEntity(IGeoPoint pGeopoint, DataEntity dataEntity) {
        super(pGeopoint);
        this.dataEntity = dataEntity;
    }

    public DataEntity getDataEntity() {
        return dataEntity;
    }
}
