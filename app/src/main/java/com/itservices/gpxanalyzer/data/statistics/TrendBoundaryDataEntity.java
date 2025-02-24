package com.itservices.gpxanalyzer.data.statistics;

import com.itservices.gpxanalyzer.data.entity.DataEntity;

import java.util.Vector;

public record TrendBoundaryDataEntity(int id,
                                      TrendType trendType,
                                      long beginTimestamp, long endTimestamp, Vector<DataEntity> dataEntityVector) {

    public String getLabel() {
        return String.valueOf(id);
    }
}
