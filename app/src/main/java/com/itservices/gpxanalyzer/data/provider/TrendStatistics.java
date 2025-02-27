package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.statistics.TrendType;

public record TrendStatistics(
        TrendType trendType,
        float deltaVal,
        float sumCumulativeDeltaValIncluded
) {
}
