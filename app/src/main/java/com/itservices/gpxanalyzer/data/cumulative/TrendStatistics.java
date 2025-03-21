package com.itservices.gpxanalyzer.data.cumulative;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.TrendType;

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
