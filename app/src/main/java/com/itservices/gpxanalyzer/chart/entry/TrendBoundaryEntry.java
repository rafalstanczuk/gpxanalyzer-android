package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;

/**
 * Encapsulates trend boundary data entities and their associated chart entries.
 * <p>
 * A trend boundary represents a segment of GPX data with specific characteristics,
 * such as an ascent/descent section or a speed zone. This record maintains the 
 * relationship between the trend boundary entity that defines the segment and the
 * list of entries that represent individual data points within that segment.
 * <p>
 * Each TrendBoundaryEntry is typically associated with a specific LineDataSet in the chart
 * and maintains styling information consistent with the trend type.
 * 
 * @param trendBoundaryDataEntity The trend boundary entity defining this segment
 * @param label The display label for this trend boundary
 * @param entries The list of CurveEntry objects representing points within this trend boundary
 * @param dataEntityWrapper The wrapper containing context for the dataset
 */
public record TrendBoundaryEntry(
        TrendBoundaryDataEntity trendBoundaryDataEntity,
        String label,
        List<Entry> entries,
        DataEntityWrapper dataEntityWrapper
) {
    /**
     * Gets a display label for this trend boundary.
     * <p>
     * The label typically combines the trend type (like "ascent" or "speed zone")
     * with relevant details such as grade or speed range.
     *
     * @return A formatted label suitable for display in chart legends or tooltips
     */
    public String getLabel() {
        return label;
    }
}
