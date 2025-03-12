package com.itservices.gpxanalyzer.data.extrema.detector;

/**
 * @param minAscAmp             Minimum amplitude for ascending
 * @param minAscDerivative      Minimum derivative for ascending
 * @param minDescAmp            Minimum amplitude for descending
 * @param minDescDerivative     Minimum derivative for descending
 */
public record SegmentThresholds(
        double minAscAmp,
        double minAscDerivative,
        double minDescAmp,
        double minDescDerivative
) {
}
