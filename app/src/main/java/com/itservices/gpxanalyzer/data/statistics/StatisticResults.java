package com.itservices.gpxanalyzer.data.statistics;


import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityCacheMap;

import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class StatisticResults {
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private double maxValue;
    private double minValue;

    private final DataEntityCacheMap dataEntityCacheMap = new DataEntityCacheMap();

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
                                    dataEntityCacheMap.add(dataEntity.getTimestampMillis(), dataEntity);

                                    return dataEntity.getValueList().get(primaryDataIndex);
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

        dataEntityCacheMap.init(dataEntityVector.size() + 1);

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

    public DataEntityCacheMap getDataEntityCacheMap() {
        return dataEntityCacheMap;
    }

    public float getValue(int index) {
        return dataEntityVector.get(index).getValueList().get(primaryDataIndex);
    }
}
