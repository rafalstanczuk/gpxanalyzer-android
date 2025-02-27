package com.itservices.gpxanalyzer.data.gpx.calculation;

import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.List;
import java.util.stream.Collectors;

public class DataPrimitiveMapper {

    public static List<PrimitiveDataEntity> mapFrom(StatisticResults statisticResults) {
        int primaryDataIndex = statisticResults.getPrimaryDataIndex();
        return statisticResults.getDataEntityVector().stream()
                .map(dataEntity -> new PrimitiveDataEntity(
                        dataEntity.getTimestampMillis(),
                        dataEntity.getValueList().get(primaryDataIndex),
                        dataEntity.getValueAccuracyList().get(primaryDataIndex))
                ).collect(Collectors.toList());
    }
}
