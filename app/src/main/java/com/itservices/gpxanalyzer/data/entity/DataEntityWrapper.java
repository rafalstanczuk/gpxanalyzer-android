package com.itservices.gpxanalyzer.data.entity;


import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;

import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public final class DataEntityWrapper {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private final Vector<DataEntity> data;
    private double maxValue;
    private double minValue;
    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    private int hashCode = -1;
    private long dataEntityTimestampHash = -1;

    private DataEntityWrapper(Vector<DataEntity> data) {
        this.data = data;
    }

    public DataEntityWrapper(final Vector<DataEntity> data, int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        this.data = data;
        compute();

        dataEntityTimestampHash = computeDataEntityTimestampHash();
        hashCode = computeHash();
    }

    public double getDeltaMinMax() {
        return maxValue - minValue;
    }

    private void clear() {
        data.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;

        dataEntityTimestampHash = computeDataEntityTimestampHash();
        hashCode = computeHash();
    }

    private void compute() {
        DoubleSummaryStatistics stats = data.stream()
                .collect(
                        Collectors.summarizingDouble(
                                dataEntity -> {
                                    return dataEntity.valueList().get(primaryDataIndex);
                                }
                        )
                );
        minValue = stats.getMin();
        maxValue = stats.getMax();
    }

    private long computeDataEntityTimestampHash() {
        return Objects.hash(
                getMaxValue(),
                getMinValue(),
                        data
                        .stream()
                        .mapToLong(DataEntity::timestampMillis)
                        .sum(),
                getPrimaryDataIndex()
        );
    }

    public long getDataHash() {
        if (dataEntityTimestampHash > 0)
            return dataEntityTimestampHash;
        else {
            dataEntityTimestampHash = computeDataEntityTimestampHash();
        }

        return dataEntityTimestampHash;
    }

    public Vector<DataEntity> getData() {
        return data;
    }

    public int getPrimaryDataIndex() {
        return primaryDataIndex;
    }

    public void setPrimaryDataIndex(int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        compute();

        dataEntityTimestampHash = computeDataEntityTimestampHash();
        hashCode = computeHash();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public float getAccuracy(int index) {
        return data.get(index).valueAccuracyList().get(primaryDataIndex);
    }

    public float getAccuracy(DataEntity dataEntity) {
        return dataEntity.valueAccuracyList().get(primaryDataIndex);
    }

    public String getName(int index) {
        return data.get(index).nameList().get(primaryDataIndex);
    }

    public String getName(DataEntity dataEntity) {
        return dataEntity.nameList().get(primaryDataIndex);
    }

    public String getUnit(int index) {
        return data.get(index).unitList().get(primaryDataIndex);
    }

    public String getUnit(DataEntity dataEntity) {
        return dataEntity.unitList().get(primaryDataIndex);
    }

    public float getValue(int index) {
        return data.get(index).valueList().get(primaryDataIndex);
    }

    public float getValue(DataEntity dataEntity) {
        return dataEntity.valueList().get(primaryDataIndex);
    }

    public CumulativeStatistics getCumulativeStatistics(DataEntity dataEntity, CumulativeProcessedDataType type) {
        return dataEntity.get(primaryDataIndex, type);
    }

    public void putCumulativeStatistics(DataEntity dataEntity, CumulativeProcessedDataType type, CumulativeStatistics statistics) {
        dataEntity.put(primaryDataIndex, type, statistics);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntityWrapper that)) return false;
        return Double.compare(getMaxValue(), that.getMaxValue()) == 0 && Double.compare(getMinValue(), that.getMinValue()) == 0 && getPrimaryDataIndex() == that.getPrimaryDataIndex() && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        if (hashCode > 0) {
            return hashCode;
        }

        hashCode = computeHash();

        return hashCode;
    }

    private int computeHash() {
        return Objects.hash(getMaxValue(), getMinValue(), getData(), getPrimaryDataIndex());
    }
}
