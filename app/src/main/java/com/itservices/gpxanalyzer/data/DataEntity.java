package com.itservices.gpxanalyzer.data;

import java.util.List;

public class DataEntity {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private final long timestampMillis;

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;
    private final List<Float> valueList;
    private final List<String> nameList;
    private final List<String> unitList;

    public DataEntity(long timestampMillis, int primaryDataIndex, List<Float> valueList, List<String> nameList, List<String> unitList) {
        this.timestampMillis = timestampMillis;
        this.primaryDataIndex = primaryDataIndex;
        this.valueList = valueList;
        this.nameList = nameList;
        this.unitList = unitList;
    }

    public DataEntity(DataEntity dataEntity) {
        this.timestampMillis = dataEntity.getTimestampMillis();
        this.primaryDataIndex = dataEntity.getPrimaryDataIndex();
        this.valueList = dataEntity.getValueList();
        this.nameList = dataEntity.getNameList();
        this.unitList = dataEntity.getUnitList();
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public int getPrimaryDataIndex() {
        return primaryDataIndex;
    }

    public void setPrimaryDataIndex(int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
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
