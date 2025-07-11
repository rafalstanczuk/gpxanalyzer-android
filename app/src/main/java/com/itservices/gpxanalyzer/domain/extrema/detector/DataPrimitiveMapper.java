package com.itservices.gpxanalyzer.domain.extrema.detector;

import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;

import java.util.Vector;
import java.util.stream.Collectors;

public final class DataPrimitiveMapper {

    public static Vector<PrimitiveDataEntity> mapFrom(DataEntityWrapper dataEntityWrapper) {
        return dataEntityWrapper.getData().stream()
                .map(dataEntity -> new PrimitiveDataEntity(
                        dataEntity.id(),
                        dataEntity.timestampMillis(),
                        dataEntityWrapper.getValue(dataEntity),
                        dataEntityWrapper.getAccuracy(dataEntity))
                ).collect(Collectors.toCollection(Vector::new));
    }
}
