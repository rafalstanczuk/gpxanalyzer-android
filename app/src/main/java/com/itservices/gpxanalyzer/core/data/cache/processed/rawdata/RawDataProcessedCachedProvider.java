package com.itservices.gpxanalyzer.core.data.cache.processed.rawdata;

import static com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper.isNotEqualByDataHash;

import android.util.Log;

import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewMode;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RawDataProcessedCachedProvider {
    private static final String TAG = RawDataProcessedCachedProvider.class.getSimpleName();


    public static RawDataProcessed EMPTY_RAW_DATA_PROCESSED_DATA =
            new RawDataProcessed(
                    new AtomicReference<>(0L),
                    new AtomicReference<>(new DataEntityWrapper(0, null)),
                    new AtomicReference<>(new ArrayList<>())
            );
    private final ConcurrentMap<GpxViewMode, AtomicReference<RawDataProcessed>> rawDataProcessedDataMap =
            new ConcurrentHashMap<>(GpxViewMode.values().length);

    @Inject
    public RawDataProcessedCachedProvider() {
    }

    public RawDataProcessed provide(DataEntityWrapper currentWrapper) {
        if (currentWrapper == null) {
            return null;
        }

        clearOldCachedData(currentWrapper);

        GpxViewMode gpxViewMode = null;
        AtomicReference<RawDataProcessed> rawDataProcessedAtomic = null;
        try {
            gpxViewMode = GpxViewMode.from(currentWrapper.getPrimaryDataIndex());
            rawDataProcessedAtomic = rawDataProcessedDataMap.get(gpxViewMode);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "provide: ", e);
        }

        if (rawDataProcessedAtomic == null) {
            return null;
        }

        RawDataProcessed rawDataProcessed = rawDataProcessedAtomic.get();
        if (rawDataProcessed == null) {
            return null;
        }

        Log.i(TAG, "provide: FOUND RawDataProcessed for " + gpxViewMode);


        // Return a defensive copy to prevent external modifications
        return new RawDataProcessed(
                new AtomicReference<>(rawDataProcessed.inputDataEntityWrapperHash().get()),
                new AtomicReference<>(rawDataProcessed.dataEntityWrapperAtomic().get()),
                new AtomicReference<>(rawDataProcessed.trendBoundaryDataEntityListAtomic().get())
        );
    }

    private void clearOldCachedData(DataEntityWrapper currentDataEntityWrapper) {
        if (currentDataEntityWrapper == null || currentDataEntityWrapper.getData() == null) {
            return;
        }

                rawDataProcessedDataMap.keySet().forEach(gpxViewMode -> {

                    AtomicReference<RawDataProcessed> rawDataProcessedDataAtomic = rawDataProcessedDataMap.get(gpxViewMode);

                    if (rawDataProcessedDataAtomic != null && rawDataProcessedDataAtomic.get() != null
                            && rawDataProcessedDataAtomic.get().inputDataEntityWrapperHash() != null) {
                        long inputDataWrapperHash = rawDataProcessedDataAtomic.get().inputDataEntityWrapperHash().get();

                        boolean toRemove = isNotEqualByDataHash(inputDataWrapperHash, currentDataEntityWrapper);

                        if (toRemove) {
                            Log.i(TAG, "clearOldCachedData() remove old inputDataWrapperHash: hash1 = ["
                                    + inputDataWrapperHash + "], currentDataEntityWrapper = ["
                                    + currentDataEntityWrapper.getDataHash() + "]");

                            rawDataProcessedDataAtomic.set(null);
                        }
                    }
                });

    }

    public void add(DataEntityWrapper dataEntityWrapper, RawDataProcessed rawDataProcessed) {
        if (rawDataProcessed == null || dataEntityWrapper == null) {
            return;
        }

        GpxViewMode gpxViewMode = GpxViewMode.from(dataEntityWrapper.getPrimaryDataIndex());

        if (rawDataProcessedDataMap.containsKey(gpxViewMode)) {
            AtomicReference<RawDataProcessed> rawDataProcessedAtomic = rawDataProcessedDataMap.get(gpxViewMode);
            if (rawDataProcessedAtomic != null) {
                rawDataProcessedAtomic.set(rawDataProcessed);
            } else {
                rawDataProcessedDataMap.put(
                        gpxViewMode,
                        new AtomicReference<>(rawDataProcessed)
                );
            }
        } else {
            rawDataProcessedDataMap.put(
                    gpxViewMode,
                    new AtomicReference<>(rawDataProcessed)
            );
        }
    }
}
