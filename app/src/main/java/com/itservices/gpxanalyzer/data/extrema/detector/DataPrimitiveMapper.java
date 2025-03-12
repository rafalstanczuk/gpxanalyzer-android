package com.itservices.gpxanalyzer.data.extrema.detector;

import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;
import java.util.stream.Collectors;

public final class DataPrimitiveMapper {

    public static List<PrimitiveDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper) {
        return dataEntityWrapper.getData().stream()
                .map(dataEntity -> new PrimitiveDataEntity(
                        dataEntity.id(),
                        dataEntity.timestampMillis(),
                        dataEntityWrapper.getValue(dataEntity),
                        dataEntityWrapper.getAccuracy(dataEntity))
                ).collect(Collectors.toList());
    }
}
