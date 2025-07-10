package com.itservices.gpxanalyzer.data.cache.processed.rawdata;

import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.data.model.entity.DataEntityWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public record RawDataProcessed(
        AtomicReference<Long> inputDataEntityWrapperHash,
        AtomicReference<DataEntityWrapper> dataEntityWrapperAtomic,
        AtomicReference<List<TrendBoundaryDataEntity>> trendBoundaryDataEntityListAtomic) {
}
