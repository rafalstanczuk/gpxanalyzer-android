package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.data.raw.DataEntity;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;

import java.io.File;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public final class DataEntityCachedProvider {

    @Inject
    GPXDataEntityProvider dataProvider;

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    @Inject
    DataEntityCache dataEntityCache;

    @Inject
    public DataEntityCachedProvider() {
    }

    public Single<Vector<DataEntity>> provide() {
        return provideDataEntityVector()
                .map(this::updateDataCache);
    }

    private Vector<DataEntity> updateDataCache(Vector<DataEntity> dataEntityVector) {

        /**
         * Use selected file once - next time use cached from memory(dataEntityVector or default from rawResId) - don't load twice!
         */
        selectGpxFileUseCase.setSelectedFile(null);


        return dataEntityVector;
    }

    private Single<Vector<DataEntity>> provideDataEntityVector() {
        File selectedFile = selectGpxFileUseCase.getSelectedFile();
        return (selectedFile != null)
                ? dataProvider.provide(selectedFile)
                : ( !dataEntityCache.getDataEntitityVector().isEmpty() ) ?
                Single.just(dataEntityCache.getDataEntitityVector())
                :
                dataProvider.provideDefault();
    }

    public Observable<Integer> getPercentageProgress() {
        return dataProvider.getPercentageProgress();
    }
}
