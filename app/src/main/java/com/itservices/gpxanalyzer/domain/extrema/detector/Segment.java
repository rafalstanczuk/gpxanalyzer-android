package com.itservices.gpxanalyzer.domain.extrema.detector;

public record Segment(
        int startIndex,
        int endIndex,
        long startTime,
        long endTime,
        double startVal,
        double endVal,
        SegmentTrendType type) {

}
