package com.itservices.gpxanalyzer.data;

import androidx.annotation.NonNull;

public record TrendStatistics(
        TrendType trendType,
        float deltaVal,
        float sumCumulativeDeltaValIncluded
) {
    @NonNull
    @Override
    public String toString() {
        return "TrendStatistics{" +
                "trendType=" + trendType +
                ", deltaVal=" + deltaVal +
                ", sumCumulativeDeltaValIncluded=" + sumCumulativeDeltaValIncluded +
                '}';
    }
}
