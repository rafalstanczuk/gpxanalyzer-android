package com.itservices.gpxanalyzer.data.entity;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntity that)) return false;
        return id == that.id && timestampMillis == that.timestampMillis && Objects.equals(valueList, that.valueList) && Objects.equals(nameList, that.nameList) && Objects.equals(unitList, that.unitList) && Objects.equals(valueAccuracyList, that.valueAccuracyList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestampMillis, valueList, valueAccuracyList, nameList, unitList);
    }
}
