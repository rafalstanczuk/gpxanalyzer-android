package com.itservices.gpxanalyzer.data.cache;

import com.github.mikephil.charting.data.LineData;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;

import java.util.concurrent.atomic.AtomicReference;

public record ChartProcessedData(
        AtomicReference<EntryCacheMap> entryCacheMapAtomic,
        AtomicReference<LineData> lineData) {
}
