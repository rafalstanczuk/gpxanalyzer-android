package com.itservices.gpxanalyzer.data.entity;

import java.util.List;

public class DataEntity {
    private final int id;
    private final long timestampMillis;
    private final List<Float> valueList;
    private final List<Float> valueAccuracyList;
    private final List<String> nameList;
    private final List<String> unitList;

    public DataEntity(int id, long timestampMillis, List<Float> valueList, List<Float> valueAccuracyList, List<String> nameList, List<String> unitList) {
        this.id = id;
        this.timestampMillis = timestampMillis;
        this.valueList = valueList;
        this.valueAccuracyList = valueAccuracyList;
        this.nameList = nameList;
        this.unitList = unitList;
    }

    public DataEntity(DataEntity dataEntity) {
        this.id = dataEntity.getId();
        this.timestampMillis = dataEntity.getTimestampMillis();
        this.valueList = dataEntity.getValueList();
        this.nameList = dataEntity.getNameList();
        this.unitList = dataEntity.getUnitList();
        this.valueAccuracyList = dataEntity.getValueAccuracyList();
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public List<Float> getValueList() {
        return valueList;
    }

    public List<String> getNameList() {
        return nameList;
    }

    public List<String> getUnitList() {
        return unitList;
    }

    public List<Float> getValueAccuracyList() {
        return valueAccuracyList;
    }


    @Override
    public String toString() {
        return "DataEntity{" +
                "timestampMillis=" + timestampMillis +
                ", valueList=" + valueList +
                ", nameList=" + nameList +
                ", unitList=" + unitList +
                '}';
    }

    public int getId() {
        return id;
    }
}
