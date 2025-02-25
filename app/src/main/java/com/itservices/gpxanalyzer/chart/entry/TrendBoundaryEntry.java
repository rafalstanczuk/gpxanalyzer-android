package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.statistics.TrendType;

import java.util.List;

public record TrendBoundaryEntry(int id,
                                 TrendType trendType,
                                 long beginTimestamp, long endTimestamp, List<Entry> entryList) {


    public String getLabel() {
        return String.valueOf(id);
    }

}
