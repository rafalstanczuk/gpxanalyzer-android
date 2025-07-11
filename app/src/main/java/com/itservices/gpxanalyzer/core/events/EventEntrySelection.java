package com.itservices.gpxanalyzer.core.events;

import com.itservices.gpxanalyzer.core.ui.components.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartSlot;

/**
 * Represents an event triggered when an entry is selected on a chart.
 * This record holds information about the chart slot and the specific curve entry that was selected.
 *
 * @param chartSlot  The {@link ChartSlot} identifying the specific chart where the selection occurred.
 * @param curveEntry The {@link CurveEntry} representing the data point that was selected on the chart.
 */
public record EventEntrySelection(ChartSlot chartSlot, CurveEntry curveEntry) {
}
