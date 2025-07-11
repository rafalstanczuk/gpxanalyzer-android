package com.itservices.gpxanalyzer.core.data.cache.processed.rawdata;


import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.domain.cumulative.TrendBoundaryDataEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public record RawDataProcessed(
        AtomicReference<Long> inputDataEntityWrapperHash,
        AtomicReference<DataEntityWrapper> dataEntityWrapperAtomic,
        AtomicReference<List<TrendBoundaryDataEntity>> trendBoundaryDataEntityListAtomic) {
}
