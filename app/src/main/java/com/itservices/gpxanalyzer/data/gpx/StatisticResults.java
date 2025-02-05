package com.itservices.gpxanalyzer.data.gpx;

import static com.itservices.gpxanalyzer.data.DataEntity.DEFAULT_PRIMARY_DATA_INDEX;

import com.itservices.gpxanalyzer.data.DataEntity;

import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

public class StatisticResults {
    private double maxValue;
    private double minValue;

    private Vector<DataEntity> dataEntityVector = new Vector<>();

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;

    public StatisticResults(Vector<DataEntity> dataEntityVector) {
        setMeasurements(dataEntityVector);
    }

    public StatisticResults(Vector<DataEntity> dataEntityVector, int primaryDataIndex) {
        this.primaryDataIndex = primaryDataIndex;
        setMeasurements(dataEntityVector);
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
                                    dataEntity.setPrimaryDataIndex(primaryDataIndex); // update primary data index
                                    return dataEntity.getValueList().get(dataEntity.getPrimaryDataIndex());
                                }
                        )
                );
        minValue = stats.getMin();
        maxValue = stats.getMax();
    }

    public final Vector<DataEntity> getDataEntityVector() {
        return dataEntityVector;
    }

    public void setMeasurements(Vector<DataEntity> dataEntityVector) {
        clear();

        this.dataEntityVector = copyDataEntityVector(dataEntityVector);

        compute();
    }

    private static Vector<DataEntity> copyDataEntityVector(Vector<DataEntity> dataEntityVector) {
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
