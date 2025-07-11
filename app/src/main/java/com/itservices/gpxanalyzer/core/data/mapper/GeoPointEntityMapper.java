package com.itservices.gpxanalyzer.core.data.mapper;

import android.location.Location;
import com.itservices.gpxanalyzer.core.data.model.entity.DataEntity;
import com.itservices.gpxanalyzer.core.data.model.entity.GeoPointEntity;

public class GeoPointEntityMapper {

    public static GeoPointEntity mapFrom(DataEntity dataEntity) {
        Location location = (Location) dataEntity.getExtraData();

        return new GeoPointEntity(location, dataEntity);
    }
}
