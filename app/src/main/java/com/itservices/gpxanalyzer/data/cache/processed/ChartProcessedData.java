package com.itservices.gpxanalyzer.data.cache.processed;

import com.github.mikephil.charting.data.LineData;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;

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
 * @param entryCacheMapAtomic Atomic reference to the cache map of chart entries
 * @param lineData Atomic reference to the MPAndroidChart LineData object
 */
public record ChartProcessedData(
        AtomicReference<EntryCacheMap> entryCacheMapAtomic,
        AtomicReference<LineData> lineData) {
}
