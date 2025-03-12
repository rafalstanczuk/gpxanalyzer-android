package com.itservices.gpxanalyzer.data;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.entity.DataEntity;

import java.util.Vector;

public record TrendBoundaryDataEntity(int id,
                                      TrendStatistics trendStatistics,
                                      Vector<DataEntity> dataEntityVector) {

    public String getLabel() {
        return String.valueOf(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "TrendBoundaryDataEntity{" +
                "id=" + id +
                ", trendStatistics=" + trendStatistics.toString() +
                ", beginTimestamp=" + dataEntityVector.firstElement().timestampMillis() +
                ", endTimestamp=" + dataEntityVector.lastElement().timestampMillis() +"}\n";
    }
}
