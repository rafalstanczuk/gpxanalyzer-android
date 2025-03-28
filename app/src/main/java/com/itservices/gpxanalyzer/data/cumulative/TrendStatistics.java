package com.itservices.gpxanalyzer.data.cumulative;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.TrendType;

/**
 * Statistical information about a trend segment in GPX data.
 * <p>
 * This record encapsulates statistical measurements related to a trend segment
 * (such as an ascent, descent, or flat section) within a GPX track. It tracks
 * the trend type, the absolute magnitude of the change within the segment,
 * the cumulative sum of changes across segments of this type, and the count
 * of segments of this type.
 * <p>
 * TrendStatistics objects are used by the visualization system to determine
 * how to render trend segments with appropriate styling and to provide
 * summary information about track characteristics.
 * 
 * @param trendType The type of trend this statistics represents (UP, DOWN, or CONSTANT)
 * @param absDeltaVal The absolute magnitude of change within this segment
 * @param sumCumulativeAbsDeltaValIncluded The cumulative sum of absolute changes across all segments of this type
 * @param n The count of segments of this trend type processed so far
 */
public record TrendStatistics(
        TrendType trendType,
        float absDeltaVal,
        float sumCumulativeAbsDeltaValIncluded,
        int n
) {
    /**
     * Returns a string representation of this trend statistics object.
     * <p>
     * Includes the trend type, the absolute delta value, and the cumulative
     * sum of absolute delta values across segments of this type.
     *
     * @return A formatted string representation of these trend statistics
     */
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
