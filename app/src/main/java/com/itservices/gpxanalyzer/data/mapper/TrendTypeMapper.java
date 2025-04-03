package com.itservices.gpxanalyzer.data.mapper;

import com.itservices.gpxanalyzer.data.cumulative.TrendType;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentTrendType;

public class TrendTypeMapper {
    public static TrendType map(SegmentTrendType segmentTrendType){
        return TrendType.values()[segmentTrendType.ordinal()];
    }
    public static SegmentTrendType map(TrendType trendType){
        return SegmentTrendType.values()[trendType.ordinal()];
    }
}
