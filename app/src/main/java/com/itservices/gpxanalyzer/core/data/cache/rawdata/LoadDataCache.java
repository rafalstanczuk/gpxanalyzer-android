package com.itservices.gpxanalyzer.core.data.cache.rawdata;

import android.location.Location;

import com.itservices.gpxanalyzer.core.data.mapper.GeoPointEntityMapper;
import com.itservices.gpxanalyzer.core.data.model.entity.DataEntity;
import com.itservices.gpxanalyzer.core.data.model.entity.GeoPointEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoadDataCache {

    @Inject
    DataEntityCache dataEntityCache;

    @Inject
    GeoPointCache geoPointCache;


    @Inject
    public LoadDataCache() {
    }

    public void init(int nPrimaryIndexes) {
        dataEntityCache.init(nPrimaryIndexes);
        geoPointCache.init();
    }

    public void accept(DataEntity dataEntity) {
        dataEntityCache.accept(dataEntity);
        if (dataEntity.getExtraData() instanceof Location) {

            GeoPointEntity geoPointEntity = GeoPointEntityMapper.mapFrom(dataEntity);

            dataEntity.setExtraData(geoPointEntity);

            geoPointCache.accept(
                    geoPointEntity
            );
        } else if(dataEntity.getExtraData() instanceof GeoPointEntity) {
            geoPointCache.accept(
                    (GeoPointEntity)dataEntity.getExtraData()
            );
        }
    }
}
