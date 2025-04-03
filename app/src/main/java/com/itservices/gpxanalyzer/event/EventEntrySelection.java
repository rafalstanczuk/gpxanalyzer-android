package com.itservices.gpxanalyzer.event;

import com.itservices.gpxanalyzer.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;

public record EventEntrySelection(ChartSlot chartSlot, CurveEntry curveEntry) {
}
