package com.itservices.gpxanalyzer.data.provider;

import static com.itservices.gpxanalyzer.events.RequestStatus.NEW_DATA_LOADING;

import com.itservices.gpxanalyzer.data.provider.file.GpxFileDataEntityProvider;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.data.raw.DataEntity;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;

import java.io.File;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Single;

public final class GpxDataEntityCachedProvider {

    @Inject
    GpxFileDataEntityProvider dataProvider;

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    @Inject
    DataEntityCache dataEntityCache;

    @Inject
    GlobalEventWrapper eventWrapper;

    @Inject
    public GpxDataEntityCachedProvider() {
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
                ? provideFromSelected(selectedFile)
                : ( !dataEntityCache.getDataEntitityVector().isEmpty() ) ?
                Single.just(dataEntityCache.getDataEntitityVector())
                :
                getProvideDefault();
    }

    private Single<Vector<DataEntity>> getProvideDefault() {
        eventWrapper.onNext(NEW_DATA_LOADING);
        return dataProvider.provideDefault();
    }

    private Single<Vector<DataEntity>> provideFromSelected(File selectedFile) {
        eventWrapper.onNext(NEW_DATA_LOADING);
        return dataProvider.provide(selectedFile);
    }
}
