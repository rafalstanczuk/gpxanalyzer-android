package com.itservices.gpxanalyzer.data.cache.processed.chart;

import static com.itservices.gpxanalyzer.data.raw.DataEntityWrapper.isNotEqualByDataHash;

import android.util.Log;

import com.github.mikephil.charting.data.LineData;
import com.itservices.gpxanalyzer.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChartProcessedDataCachedProvider {
    private static final String TAG = ChartProcessedDataCachedProvider.class.getSimpleName();

    public static ChartProcessedData EMPTY_CHART_PROCESSED_DATA =
            new ChartProcessedData(
                    new AtomicReference<>(0L),
                    new AtomicReference<>(new EntryCacheMap()),
                    new AtomicReference<>(new LineData(new ArrayList<>()))
            );
    private final ConcurrentMap<ChartSlot, ConcurrentMap<GpxViewMode, AtomicReference<ChartProcessedData>>> chartSlotProcessedDataMap =
            new ConcurrentHashMap<>(ChartSlot.values().length);

    @Inject
    public ChartProcessedDataCachedProvider() {
    }

    public ChartProcessedData provide(RawDataProcessed rawDataProcessed, LineChartSettings settings) {
        if (rawDataProcessed == null || settings == null) {
            return null;
        }

        if (rawDataProcessed.dataEntityWrapperAtomic() == null) {
            return null;
        }

        if (rawDataProcessed.dataEntityWrapperAtomic().get() == null) {
            return null;
        }

        DataEntityWrapper currentWrapper = rawDataProcessed.dataEntityWrapperAtomic().get();

        ChartSlot chartSlot = settings.getChartSlot();

        if (chartSlot == null) {
            return null;
        }

        clearOldCachedData(currentWrapper);

        ConcurrentMap<GpxViewMode, AtomicReference<ChartProcessedData>> chartSlotMap =
                chartSlotProcessedDataMap.get(chartSlot);

        if (chartSlotMap == null) {
            return null;
        }

        GpxViewMode gpxViewMode = null;
        AtomicReference<ChartProcessedData> chartProcessedDataAtomic = null;
        try {
            gpxViewMode = GpxViewMode.from(currentWrapper.getPrimaryDataIndex());
            chartProcessedDataAtomic = chartSlotMap.get(gpxViewMode);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "provide: ", e);
        }

        if (chartProcessedDataAtomic == null) {
            return null;
        }

        ChartProcessedData chartProcessedData = chartProcessedDataAtomic.get();
        if (chartProcessedData == null) {
            return null;
        }

        settings.updateSettingsFor(chartProcessedData.lineData().get());

        Log.i(TAG, "provide: FOUND ChartProcessedData for ChartSlot: " + chartSlot + ", and GpxViewMode: " + gpxViewMode);

        // Return a defensive copy to prevent external modifications
        return new ChartProcessedData(
                new AtomicReference<>(currentWrapper.getDataHash()),
                new AtomicReference<>(chartProcessedData.entryCacheMapAtomic().get()),
                new AtomicReference<>(chartProcessedData.lineData().get())
        );
    }

    private void clearOldCachedData(DataEntityWrapper currentDataEntityWrapper) {
        if (currentDataEntityWrapper == null || currentDataEntityWrapper.getData() == null) {
            return;
        }

        chartSlotProcessedDataMap.forEach((chartSlot, dataMap) -> {
            if (dataMap != null) {
                dataMap.keySet().forEach(gpxViewMode -> {

                    AtomicReference<ChartProcessedData> chartProcessedDataAtomic = dataMap.get(gpxViewMode);

                    if (chartProcessedDataAtomic != null && chartProcessedDataAtomic.get() != null
                            && chartProcessedDataAtomic.get().inputDataEntityWrapperHash() != null) {
                        long inputDataWrapperHash = chartProcessedDataAtomic.get().inputDataEntityWrapperHash().get();

                        boolean toRemove = isNotEqualByDataHash(inputDataWrapperHash, currentDataEntityWrapper);

                        if (toRemove) {
                            Log.i(TAG, "clearOldCachedData() remove old inputDataWrapperHash: hash1 = ["
                                    + inputDataWrapperHash + "], currentDataEntityWrapper = ["
                                    + currentDataEntityWrapper.getDataHash() + "]");

                            chartProcessedDataAtomic.set(null);
                        }
                    }
                });
            }
        });
    }

    public void add(ChartSlot chartSlot, RawDataProcessed rawDataProcessed, ChartProcessedData chartProcessedData) {
        if (chartSlot == null || rawDataProcessed == null || chartProcessedData == null) {
            return;
        }

        if (rawDataProcessed.dataEntityWrapperAtomic() == null) {
            return;
        }

        if (rawDataProcessed.dataEntityWrapperAtomic().get() == null) {
            return;
        }

        DataEntityWrapper dataEntityWrapper = rawDataProcessed.dataEntityWrapperAtomic().get();

        GpxViewMode gpxViewMode = GpxViewMode.from(dataEntityWrapper.getPrimaryDataIndex());
        ConcurrentMap<GpxViewMode, AtomicReference<ChartProcessedData>> chartSlotMap;

        if (chartSlotProcessedDataMap.containsKey(chartSlot)) {
            chartSlotMap = chartSlotProcessedDataMap.get(chartSlot);

            if (chartSlotMap == null) {
                chartSlotMap = new ConcurrentHashMap<>(GpxViewMode.values().length);
                chartSlotProcessedDataMap.put(chartSlot, chartSlotMap);
            }

            if (chartSlotMap.containsKey(gpxViewMode)) {
                AtomicReference<ChartProcessedData> ref = chartSlotMap.get(gpxViewMode);
                if (ref != null) {
                    ref.set(chartProcessedData);
                } else {
                    chartSlotMap.put(gpxViewMode, new AtomicReference<>(chartProcessedData));  // Put defensive copy
                }

            } else {
                chartSlotMap.put(gpxViewMode, new AtomicReference<>(chartProcessedData));
            }
        } else {
            // Create new map if it doesn't exist
            chartSlotMap = new ConcurrentHashMap<>(GpxViewMode.values().length);
            chartSlotMap.put(gpxViewMode, new AtomicReference<>(chartProcessedData));

            chartSlotProcessedDataMap.put(chartSlot, chartSlotMap);
        }
    }
}
