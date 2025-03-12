package com.itservices.gpxanalyzer.data.entity;


import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

public final class DataEntityWrapper {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private double maxValue;
    private double minValue;

    private Vector<DataEntity> data = new Vector<>();

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    private DataEntityWrapper() {
    }

    public DataEntityWrapper(Vector<DataEntity> data, int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        setData(data);
    }

    public double getDeltaMinMax() {
        return maxValue - minValue;
    }

    public void setPrimaryDataIndex(int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        compute();
    }

    private void clear() {
        data.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
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

    private void setData(Vector<DataEntity> dataEntityVector) {
        this.data = dataEntityVector;

        compute();
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
}
