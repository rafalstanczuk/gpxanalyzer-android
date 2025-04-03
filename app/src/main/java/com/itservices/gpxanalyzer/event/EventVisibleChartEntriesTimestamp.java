package com.itservices.gpxanalyzer.event;

import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;

import java.util.Vector;

public record EventVisibleChartEntriesTimestamp(ChartSlot chartSlot,
                                                Vector<Long> timestampBoundary) {
}
