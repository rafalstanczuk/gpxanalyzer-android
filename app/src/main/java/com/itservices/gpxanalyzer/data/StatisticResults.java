package com.itservices.gpxanalyzer.data;


import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

public final class StatisticResults {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private double maxValue;
    private double minValue;

    private Vector<DataEntity> dataEntityVector = new Vector<>();

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    private StatisticResults() {
    }

    public StatisticResults(Vector<DataEntity> dataEntityVector, int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        setDataEntityVector(dataEntityVector);
    }

    public void setPrimaryDataIndex(int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        compute();
    }

    private void clear() {
        dataEntityVector.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
    }

    private void compute() {
        DoubleSummaryStatistics stats = dataEntityVector.stream()
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

    public final Vector<DataEntity> getDataEntityVector() {
        return dataEntityVector;
    }

    public int getPrimaryDataIndex() {
        return primaryDataIndex;
    }

    private void setDataEntityVector(Vector<DataEntity> dataEntityVector) {
        this.dataEntityVector = dataEntityVector;

        compute();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public float getValue(int index) {
        return dataEntityVector.get(index).valueList().get(primaryDataIndex);
    }

    public float getValue(DataEntity dataEntity) {
        return dataEntity.valueList().get(primaryDataIndex);
    }
}
