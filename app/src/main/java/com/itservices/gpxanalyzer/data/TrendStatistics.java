package com.itservices.gpxanalyzer.data;

import androidx.annotation.NonNull;

public record TrendStatistics(
        TrendType trendType,
        float absDeltaVal,
        float sumCumulativeAbsDeltaValIncluded,
        int n
) {
    @NonNull
    @Override
    public String toString() {
        return "TrendStatistics{" +
                "trendType=" + trendType +
                ", absDeltaVal=" + absDeltaVal +
                ", sumCumulativeAbsDeltaValIncluded=" + sumCumulativeAbsDeltaValIncluded +
                '}';
    }
}
