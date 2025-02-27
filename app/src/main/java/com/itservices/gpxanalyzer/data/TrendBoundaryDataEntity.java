package com.itservices.gpxanalyzer.data;

import androidx.annotation.NonNull;

import java.util.Vector;

public record TrendBoundaryDataEntity(int id,
                                      TrendStatistics trendStatistics,
                                      long beginTimestamp, long endTimestamp, Vector<DataEntity> dataEntityVector) {

    public String getLabel() {
        return String.valueOf(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "TrendBoundaryDataEntity{" +
                "id=" + id +
                ", trendStatistics=" + trendStatistics.toString() +
                ", beginTimestamp=" + beginTimestamp +
                ", endTimestamp=" + endTimestamp +"}\n";
    }
}
