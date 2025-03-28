package com.itservices.gpxanalyzer.data.mapper;

import android.location.Location;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.GeoPointEntity;

public class GeoPointEntityMapper {

    public static GeoPointEntity mapFrom(DataEntity dataEntity) {
        Location location = (Location) dataEntity.getExtraData();

        return new GeoPointEntity(location, dataEntity);
    }
}
