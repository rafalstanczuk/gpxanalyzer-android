package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;

import java.util.List;

public record TrendBoundaryEntry(TrendBoundaryDataEntity trendBoundaryDataEntity,
                                 List<Entry> entryList) {

    public String getLabel() {
        return String.valueOf(trendBoundaryDataEntity.id());
    }

}
