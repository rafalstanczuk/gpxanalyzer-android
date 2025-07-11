package com.itservices.gpxanalyzer.core.data.cache.processed.chart;

import com.github.mikephil.charting.data.LineData;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Encapsulates processed chart data ready for display.
 * 
 * This record holds the processed data required for chart visualization, including
 * the chart entries cache map (providing fast access to chart entries by timestamp)
 * and the LineData object (containing the actual chart datasets).
 * 
 * Both components are wrapped in AtomicReference for thread safety, as they may be
 * accessed from different threads during chart updates and user interactions.
 *
 * @param inputDataEntityWrapperHash Atomic reference to the input data hash
 *                                   provided from DataEntityWrapper getDataHash()
 * @see DataEntityWrapper getDataHash()
 *
 * @param entryCacheMapAtomic Atomic reference to the cache map of chart entries
 * @param lineData Atomic reference to the MPAndroidChart LineData object
 */
public record ChartProcessedData(
        AtomicReference<Long> inputDataEntityWrapperHash,
        AtomicReference<EntryCacheMap> entryCacheMapAtomic,
        AtomicReference<LineData> lineData) {
}
