package com.itservices.gpxanalyzer.events;

import com.itservices.gpxanalyzer.ui.components.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;

public record EventEntrySelection(ChartSlot chartSlot, CurveEntry curveEntry) {
}
