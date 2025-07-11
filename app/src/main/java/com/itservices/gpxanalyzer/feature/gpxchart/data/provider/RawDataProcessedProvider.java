package com.itservices.gpxanalyzer.feature.gpxchart.data.provider;


import static com.itservices.gpxanalyzer.core.data.cache.processed.rawdata.RawDataProcessedCachedProvider.EMPTY_RAW_DATA_PROCESSED_DATA;

import android.util.Log;

import com.itservices.gpxanalyzer.core.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.core.data.cache.processed.rawdata.RawDataProcessedCachedProvider;
import com.itservices.gpxanalyzer.domain.cumulative.TrendBoundaryCumulativeMapper;
import com.itservices.gpxanalyzer.domain.cumulative.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.domain.extrema.ExtremaSegmentListMapper;
import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewMode;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RawDataProcessedProvider {
    private static final String TAG = RawDataProcessedProvider.class.getSimpleName();

    /** Atomic reference to the current processed chart data */
    private final AtomicReference<RawDataProcessed> rawDataProcessedAtomicReference = new AtomicReference<>(EMPTY_RAW_DATA_PROCESSED_DATA);

    @Inject
    RawDataProcessedCachedProvider rawDataProcessedCachedProvider;

    @Inject
    public RawDataProcessedProvider() {
    }

    public Single<RawDataProcessed> provide(DataEntityWrapper dataEntityWrapper) {
        if (dataEntityWrapper == null)
            return Single.just(rawDataProcessedAtomicReference.get())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());

        RawDataProcessed rawDataProcessed = rawDataProcessedCachedProvider.provide(dataEntityWrapper);
        if (rawDataProcessed != null) {
            rawDataProcessedAtomicReference.set(rawDataProcessed);
            return Single.just( rawDataProcessed )
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }

        return provideInternal(dataEntityWrapper);
    }

    private Single<RawDataProcessed> provideInternal(DataEntityWrapper dataEntityWrapper) {
        return ExtremaSegmentListMapper.mapFrom(dataEntityWrapper)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(segmentList -> TrendBoundaryCumulativeMapper.mapFrom(dataEntityWrapper, segmentList))
                .map(trendBoundaryDataEntityList -> mapIntoRawDataProcessed(dataEntityWrapper, trendBoundaryDataEntityList))
                .map(rawDataProcessed -> {

                    GpxViewMode gpxViewMode = GpxViewMode.from(rawDataProcessed.dataEntityWrapperAtomic().get().getPrimaryDataIndex());
                    Log.i(TAG, "provideInternal: PROCESSED RawDataProcessed for " + gpxViewMode);

                    rawDataProcessedCachedProvider.add(dataEntityWrapper, rawDataProcessed);

                    return rawDataProcessed;
                });
    }

    private RawDataProcessed mapIntoRawDataProcessed(DataEntityWrapper dataEntityWrapper, List<TrendBoundaryDataEntity> trendBoundaryDataEntityList) {
        return new RawDataProcessed(
                new AtomicReference<>(dataEntityWrapper.getDataHash()),
                new AtomicReference<>(dataEntityWrapper),
                new AtomicReference<>(trendBoundaryDataEntityList)
        );
    }
}