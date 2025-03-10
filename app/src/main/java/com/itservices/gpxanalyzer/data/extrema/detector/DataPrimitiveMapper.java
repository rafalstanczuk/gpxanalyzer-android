package com.itservices.gpxanalyzer.data.extrema.detector;

import com.itservices.gpxanalyzer.data.StatisticResults;

import java.util.List;
import java.util.stream.Collectors;

public final class DataPrimitiveMapper {

    public static List<PrimitiveDataEntity> mapFrom(StatisticResults statisticResults) {
        int primaryDataIndex = statisticResults.getPrimaryDataIndex();
        return statisticResults.getDataEntityVector().stream()
                .map(dataEntity -> new PrimitiveDataEntity(
                        dataEntity.id(),
                        dataEntity.timestampMillis(),
                        dataEntity.valueList().get(primaryDataIndex),
                        dataEntity.valueAccuracyList().get(primaryDataIndex))
                ).collect(Collectors.toList());
    }
}
