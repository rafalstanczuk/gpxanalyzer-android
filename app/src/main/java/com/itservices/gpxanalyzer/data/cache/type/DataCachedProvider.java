package com.itservices.gpxanalyzer.data.cache.type;

import android.location.Location;

import com.itservices.gpxanalyzer.data.cache.GeoPointCachedProvider;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.GeoPointEntity;
import com.itservices.gpxanalyzer.data.mapper.GeoPointEntityMapper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataCachedProvider {

    @Inject
    DataEntityCachedProvider dataEntityCachedProvider;

    @Inject
    GeoPointCachedProvider geoPointCachedProvider;


    @Inject
    public DataCachedProvider() {
    }

    public void init(int nPrimaryIndexes) {
        dataEntityCachedProvider.init(nPrimaryIndexes);
        geoPointCachedProvider.init();
    }

    public void accept(DataEntity dataEntity) {
        dataEntityCachedProvider.accept(dataEntity);
        if (dataEntity.getExtraData() instanceof Location) {

            GeoPointEntity geoPointEntity = GeoPointEntityMapper.mapFrom(dataEntity);

            dataEntity.setExtraData(geoPointEntity);

            geoPointCachedProvider.accept(
                    geoPointEntity
            );
        } else if(dataEntity.getExtraData() instanceof GeoPointEntity) {
            geoPointCachedProvider.accept(
                    (GeoPointEntity)dataEntity.getExtraData()
            );
        }
    }
}
