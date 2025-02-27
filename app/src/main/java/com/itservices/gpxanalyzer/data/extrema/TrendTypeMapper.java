package com.itservices.gpxanalyzer.data.extrema;

import com.itservices.gpxanalyzer.data.TrendType;

public class TrendTypeMapper {
    public static TrendType map(SegmentTrendType segmentTrendType){
        return TrendType.values()[segmentTrendType.ordinal()];
    }
    public static SegmentTrendType map(TrendType trendType){
        return SegmentTrendType.values()[trendType.ordinal()];
    }
}
