package com.itservices.gpxanalyzer.data.gpx.calculation;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ExtremaSegmentDetector {

    private List<PrimitiveDataEntity> filtered;
    private List<PrimitiveDataEntity> smoothed;
    private List<Extremum> extrema;

    // Window function types
    public enum WindowType {
        TRIANGULAR,
        HANNING,
        GAUSSIAN
    }

    // If value changes are < ±0.01, treat derivative as zero (lower EPSILON to catch subtler slopes)
    private static final double EPSILON = 0.00000000001;

    // If accuracy is worse than 50, skip
    public static final float DEFAULT_MAX_VALUE_ACCURACY = 50.0f;

    // Extrema types
    private enum ExtremaType {
        MIN, MAX
    }

    // Container for an extremum (local min or local max)
    private static class Extremum {
        int index;         // index in the smoothed data
        ExtremaType type;  // MIN or MAX

        Extremum(int index, ExtremaType type) {
            this.index = index;
            this.type = type;
        }
    }

    // --------------------------------------------------------------------------
    // PUBLIC DETECTION METHODS
    // --------------------------------------------------------------------------

    public void preprocessAndFindExtrema(
            final List<PrimitiveDataEntity> originalData,
            float maxValueAccuracy,
            double[] windowWeights
    ) {
        // 1) Preprocess by accuracy
        filtered = preProcessPrimitiveDataEntity(originalData, maxValueAccuracy);

        // 2) Smooth
        smoothed = applyMovingFilter(filtered, windowWeights);

        // 3) Find local minima / maxima
        extrema = findLocalExtrema(smoothed);
    }

    public List<Pair<Long, Long>> detectAscendingSegments(
            double minAscendingAmplitude,
            double minAscendingDerivative
    ) {
        if (filtered.size() < 3 || smoothed == null || extrema == null) return new ArrayList<>();

        // 4) Ascending segments (MIN -> next valid MAX)
        return findAscendingSegmentsFromExtrema(
                smoothed, extrema, minAscendingAmplitude, minAscendingDerivative
        );
    }

    public List<Pair<Long, Long>> detectDescendingSegments(
            double minDescendingAmplitude,
            double minDescendingDerivative
    ) {
        if (filtered.size() < 3 || smoothed == null || extrema == null) return new ArrayList<>();

        // 4) Descending segments (MAX -> next valid MIN)
        return findDescendingSegmentsFromExtrema(
                smoothed, extrema, minDescendingAmplitude, minDescendingDerivative
        );
    }

    // --------------------------------------------------------------------------
    // 1) PREPROCESS
    // --------------------------------------------------------------------------
    public static List<PrimitiveDataEntity> preProcessPrimitiveDataEntity(
            List<PrimitiveDataEntity> data,
            float maxValueAccuracy
    ) {
        List<PrimitiveDataEntity> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;

        for (PrimitiveDataEntity entity : data) {
            if (entity == null) continue;
            // skip if no accuracy or beyond threshold
            if (!entity.hasAccuracy()) continue;
            if (entity.getAccuracy() <= maxValueAccuracy) {
                result.add(entity);
            }
        }
        return result;
    }

    // --------------------------------------------------------------------------
    // 2) SMOOTHING
    // --------------------------------------------------------------------------
    public static List<PrimitiveDataEntity> applyMovingFilter(
            List<PrimitiveDataEntity> data,
            double[] weights
    ) {
        if (data == null || data.size() < 3) return data;
        if (weights == null || weights.length < 3 || (weights.length % 2 == 0)) {
            throw new IllegalArgumentException(
                    "weights array must be non-null, odd length >= 3"
            );
        }

        int n = data.size();
        int windowSize = weights.length;
        int half = windowSize / 2;
        List<PrimitiveDataEntity> smoothedList = new ArrayList<>(n);

        // Copy original data
        for (PrimitiveDataEntity e : data) {
            smoothedList.add(PrimitiveDataEntity.copy(e));
        }

        // Weighted average
        for (int i = 0; i < n; i++) {
            double weightedSum = 0.0;
            double usedWeightSum = 0.0;

            for (int j = i - half; j <= i + half; j++) {
                if (j < 0 || j >= n) {
                    continue;
                }
                int offset = j - i + half;
                double w = weights[offset];
                double val = data.get(j).getValue();

                weightedSum += (val * w);
                usedWeightSum += w;
            }
            double smoothedValue = weightedSum / usedWeightSum;
            smoothedList.get(i).setValue((float) smoothedValue);
        }
        return smoothedList;
    }

    // --------------------------------------------------------------------------
    // 3) FIND LOCAL EXTREMA
    // --------------------------------------------------------------------------
    private static List<Extremum> findLocalExtrema(List<PrimitiveDataEntity> smoothed) {
        List<Extremum> result = new ArrayList<>();
        if (smoothed == null || smoothed.size() < 3) return result;

        int n = smoothed.size();
        // Compute discrete derivative
        double[] derivative = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            derivative[i] = smoothed.get(i + 1).getValue() - smoothed.get(i).getValue();
        }

        for (int i = 1; i < n - 1; i++) {
            double prevSign = signWithEpsilon(derivative[i - 1], EPSILON);
            double nextSign = signWithEpsilon(derivative[i], EPSILON);

            // local minimum => slope from negative to positive
            if (prevSign < 0 && nextSign > 0) {
                result.add(new Extremum(i, ExtremaType.MIN));
            }
            // local maximum => slope from positive to negative
            else if (prevSign > 0 && nextSign < 0) {
                result.add(new Extremum(i, ExtremaType.MAX));
            }
        }
        return result;
    }

    /**
     * Sign with dead-zone: values in [-eps, +eps] => 0
     */
    private static double signWithEpsilon(double value, double eps) {
        if (value > eps) return 1.0;
        if (value < -eps) return -1.0;
        return 0.0;
    }

    // --------------------------------------------------------------------------
    // 4) ASCENDING SEGMENT FORMATION
    // --------------------------------------------------------------------------
    private static List<Pair<Long, Long>> findAscendingSegmentsFromExtrema(
            List<PrimitiveDataEntity> smoothed,
            List<Extremum> extrema,
            double minAmp,
            double minDerivative
    ) {
        List<Pair<Long, Long>> segments = new ArrayList<>();

        for (int i = 0; i < extrema.size(); i++) {
            Extremum minExt = extrema.get(i);
            if (minExt.type == ExtremaType.MIN) {
                // Look for *any* subsequent MAX
                for (int j = i + 1; j < extrema.size(); j++) {
                    Extremum maxExt = extrema.get(j);
                    if (maxExt.type == ExtremaType.MAX && maxExt.index > minExt.index) {

                        PrimitiveDataEntity valMin = smoothed.get(minExt.index);
                        PrimitiveDataEntity valMax = smoothed.get(maxExt.index);

                        double amplitude = valMax.getValue() - valMin.getValue();
                        if (amplitude <= 0) {
                            // Not a valid climb; try next max
                            continue;
                        }

                        long dtMillis = valMax.getTimestamp() - valMin.getTimestamp();
                        if (dtMillis <= 0) {
                            continue;
                        }
                        double dtSec = dtMillis / 1000.0;
                        double avgDerivative = amplitude / dtSec;

                        // If meets thresholds => record it, then move on to the next MIN
                        if (amplitude >= minAmp && avgDerivative >= minDerivative) {
                            segments.add(new Pair<>(valMin.getTimestamp(), valMax.getTimestamp()));
                            // Found a valid segment => stop searching further maxima from this min
                            break;
                        }
                        // Otherwise, keep searching the next MAX
                    }
                }
            }
        }
        return segments;
    }

    // --------------------------------------------------------------------------
    // 5) DESCENDING SEGMENT FORMATION
    // --------------------------------------------------------------------------
    private static List<Pair<Long, Long>> findDescendingSegmentsFromExtrema(
            List<PrimitiveDataEntity> smoothed,
            List<Extremum> extrema,
            double minAmp,
            double minDerivative
    ) {
        List<Pair<Long, Long>> segments = new ArrayList<>();

        for (int i = 0; i < extrema.size(); i++) {
            Extremum maxExt = extrema.get(i);
            if (maxExt.type == ExtremaType.MAX) {
                // Look for *any* subsequent MIN
                for (int j = i + 1; j < extrema.size(); j++) {
                    Extremum minExt = extrema.get(j);
                    if (minExt.type == ExtremaType.MIN && minExt.index > maxExt.index) {

                        PrimitiveDataEntity valMax = smoothed.get(maxExt.index);
                        PrimitiveDataEntity valMin = smoothed.get(minExt.index);

                        double amplitude = valMax.getValue() - valMin.getValue();
                        if (amplitude <= 0) {
                            // No real drop; try next min
                            continue;
                        }

                        long dtMillis = valMin.getTimestamp() - valMax.getTimestamp();
                        if (dtMillis <= 0) {
                            continue;
                        }
                        double dtSec = dtMillis / 1000.0;
                        double avgDerivative = amplitude / dtSec;

                        // If meets thresholds => record it, then move on to the next MAX
                        if (amplitude >= minAmp && avgDerivative >= minDerivative) {
                            segments.add(new Pair<>(valMax.getTimestamp(), valMin.getTimestamp()));
                            break;
                        }
                        // else keep searching next local MIN
                    }
                }
            }
        }
        return segments;
    }

    // --------------------------------------------------------------------------
    // 6) WINDOW FUNCTION GENERATION
    // --------------------------------------------------------------------------
    public static double[] generateWindowFunction(int size, WindowType type, double param) {
        if (size < 3 || (size % 2 == 0)) {
            throw new IllegalArgumentException("Window size must be odd and >= 3");
        }

        double[] window = new double[size];
        int M = size - 1;

        switch (type) {
            case TRIANGULAR:
                // w[n] = 1 - |(n - M/2) / (M/2)|
                for (int n = 0; n < size; n++) {
                    window[n] = 1.0 - Math.abs(n - (M / 2.0)) / (M / 2.0);
                }
                break;

            case HANNING:
                // w[n] = 0.5 - 0.5 * cos(2πn / M)
                for (int n = 0; n < size; n++) {
                    window[n] = 0.5 - 0.5 * Math.cos((2.0 * Math.PI * n) / M);
                }
                break;

            case GAUSSIAN:
                // w[n] = exp(-0.5 * ((n - M/2) / (sigma * M/2))^2)
                double sigma = (param <= 0) ? 0.4 : param;
                double center = M / 2.0;
                for (int n = 0; n < size; n++) {
                    double x = (n - center) / (sigma * center);
                    window[n] = Math.exp(-0.5 * x * x);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown window type: " + type);
        }
        return window;
    }
}
