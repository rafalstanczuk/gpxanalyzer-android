package com.itservices.gpxanalyzer.data.entity;

import java.util.List;

public record DataEntity(
    int id,
    long timestampMillis,
    List<Float> valueList,
    List<Float> valueAccuracyList,
    List<String> nameList,
    List<String> unitList
) {

    @Override
    public String toString() {
        return "DataEntity{" +
                "timestampMillis=" + timestampMillis +
                ", valueList=" + valueList +
                ", nameList=" + nameList +
                ", unitList=" + unitList +
                '}';
    }
}
