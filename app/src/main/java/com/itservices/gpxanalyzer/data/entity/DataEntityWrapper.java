package com.itservices.gpxanalyzer.data.entity;


import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public final class DataEntityWrapper {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private double maxValue;
    private double minValue;

    private final Vector<DataEntity> data;

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    private int hashCode = -1;

    private DataEntityWrapper(Vector<DataEntity> data) {
        this.data = data;
    }

    public DataEntityWrapper(final Vector<DataEntity> data, int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        this.data = data;
        compute();

        hashCode = computeHash();
    }

    public double getDeltaMinMax() {
        return maxValue - minValue;
    }

    public void setPrimaryDataIndex(int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        compute();

        hashCode = computeHash();
    }

    private void clear() {
        data.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;

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

    public Vector<DataEntity> getData() {
        return data;
    }

    public int getPrimaryDataIndex() {
        return primaryDataIndex;
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

         return computeHash();
    }

    private int computeHash() {
        return Objects.hash(getMaxValue(), getMinValue(), getData(), getPrimaryDataIndex());
    }
}
