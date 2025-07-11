package com.itservices.gpxanalyzer.core.events;


import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartSlot;

import java.util.Vector;

/**
 * Represents an event indicating the range of timestamps currently visible within a specific chart.
 * This is typically triggered when the visible range of the chart changes due to user interaction (e.g., zooming, panning).
 *
 * @param chartSlot        The {@link ChartSlot} identifying the chart whose visible entries have changed.
 * @param timestampBoundary A {@link Vector} containing two Long values representing the minimum and maximum timestamps
 *                         (usually in milliseconds since epoch) of the entries currently visible in the chart viewport.
 *                         The first element is the minimum timestamp, and the second is the maximum.
 */
public record EventVisibleChartEntriesTimestamp(ChartSlot chartSlot,
                                                Vector<Long> timestampBoundary) {
}
