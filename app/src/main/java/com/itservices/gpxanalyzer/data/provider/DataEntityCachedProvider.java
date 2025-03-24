package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public final class DataEntityCachedProvider {

    @Inject
    GPXDataEntityProvider dataProvider;

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    private final AtomicReference<Vector<DataEntity>> dataEntitiesAtomic = new AtomicReference<>();

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
        setDataEntities(dataEntityVector);

        return dataEntityVector;
    }

    private Single<Vector<DataEntity>> provideDataEntityVector() {
        File selectedFile = selectGpxFileUseCase.getSelectedFile();
        return (selectedFile != null)
                ? dataProvider.provide(selectedFile)
                : (dataEntitiesAtomic.get() != null) ?
                Single.just(dataEntitiesAtomic.get())
                :
                dataProvider.provideDefault();
    }

    private void setDataEntities(Vector<DataEntity> dataEntities) {
        dataEntitiesAtomic.set(dataEntities);
    }

    public Observable<Integer> getPercentageProgress() {
        return dataProvider.getPercentageProgress();
    }
}
