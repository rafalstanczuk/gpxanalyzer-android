package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;

import java.io.File;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;

public final class DataEntityCachedProvider {

    @Inject
    GPXDataEntityProvider dataProvider;

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    private Vector<DataEntity> dataEntities;

    @Inject
    public DataEntityCachedProvider() {
    }

    public Observable<Vector<DataEntity>> provide() {
        return provideDataEntityVector()
                .map(this::updateDataCache);
    }

    private Vector<DataEntity> updateDataCache(Vector<DataEntity> dataEntityVector) {

        /**
         * Use selected file once - next time use cached from memory(dataEntityVector or default from rawResId) - don't load twice!
         */
        selectGpxFileUseCase.setSelectedFile(null);
        setDataEntities(dataEntityVector);

        return dataEntityVector;
    }

    private Observable<Vector<DataEntity>> provideDataEntityVector() {

        File selectedFile = selectGpxFileUseCase.getSelectedFile();
        /**
         * Use selected file once - next time use cached from memory(dataEntityVector or default from rawResId) - don't load twice!
         */
        return (selectedFile != null)
                ? dataProvider.provide(selectedFile)
                : (dataEntities != null) ?
                Observable.just(dataEntities)
                :
                dataProvider.provideDefault();
    }

    private void setDataEntities(Vector<DataEntity> dataEntities) {
        this.dataEntities = dataEntities;
    }

    public Observable<Integer> getPercentageProgress() {
        return dataProvider.getPercentageProgress();
    }
}
