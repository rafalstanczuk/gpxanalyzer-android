package com.itservices.gpxanalyzer.data.statistics;


import com.itservices.gpxanalyzer.data.entity.DataEntity;

import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

public class StatisticResults {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private double maxValue;
    private double minValue;

    private Vector<DataEntity> dataEntityVector = new Vector<>();

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    private StatisticResults() {}

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
                                dataEntity -> dataEntity.getValueList().get(primaryDataIndex)
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

    public static Vector<DataEntity> copyDataEntityVector(Vector<DataEntity> dataEntityVector) {
        Vector<DataEntity> newDataEntityVector = new Vector<>();
        dataEntityVector.forEach(dataEntity -> {
            DataEntity newDataEntity = new DataEntity(dataEntity);
            newDataEntityVector.add(newDataEntity);
        });

        return newDataEntityVector;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

}
