package com.itservices.gpxanalyzer.data.entity;

import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DataEntity {
    private final int id;
    private final long timestampMillis;
    private final List<Float> valueList;
    private final List<Float> valueAccuracyList;
    private final List<String> nameList;
    private final List<String> unitList;
    private final List<Map<CumulativeProcessedDataType, CumulativeStatistics>>
            cumulativeStatisticsMapList
            = new ArrayList<>(
                Arrays.asList(
                        new HashMap<>(CumulativeProcessedDataType.values().length),
                        new HashMap<>(CumulativeProcessedDataType.values().length)
                )
            );

    public DataEntity(
            int id,
            long timestampMillis,
            List<Float> valueList,
            List<Float> valueAccuracyList,
            List<String> nameList,
            List<String> unitList
    ) {
        this.id = id;
        this.timestampMillis = timestampMillis;
        this.valueList = valueList;
        this.valueAccuracyList = valueAccuracyList;
        this.nameList = nameList;
        this.unitList = unitList;
    }

    public boolean hasValuesWithName(String name) {
        return nameList().contains(name);
    }

    public int indexValuesWithName(String name) {
        return nameList().indexOf(name);
    }

    public float getValue(int index) {
        return valueList().get(
                index
        );
    }

    public String getUnit(int index) {
        return unitList().get(
                index
        );
    }

    public float getValueAccuracy(int index) {
        return valueAccuracyList().get(
                index
        );
    }

    public float getValueWithName(String name) {
        return valueList().get(
                nameList().indexOf(name)
        );
    }

    public String getUnitWithName(String name) {
        return unitList().get(
                nameList().indexOf(name)
        );
    }

    public int id() {
        return id;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public List<Float> valueList() {
        return valueList;
    }

    public List<Float> valueAccuracyList() {
        return valueAccuracyList;
    }

    public List<String> nameList() {
        return nameList;
    }

    public List<String> unitList() {
        return unitList;
    }

    public CumulativeStatistics get(int index, CumulativeProcessedDataType type) {
        if (cumulativeStatisticsMapList.get(index).containsKey(type)) {
            return cumulativeStatisticsMapList.get(index).get(type);
        }
        cumulativeStatisticsMapList.get(index).put(type, new CumulativeStatistics());

        return cumulativeStatisticsMapList.get(index).get(type);
    }

    public void put(int index, CumulativeProcessedDataType type, CumulativeStatistics statistics) {
        cumulativeStatisticsMapList.get(index).put(type, statistics);
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
