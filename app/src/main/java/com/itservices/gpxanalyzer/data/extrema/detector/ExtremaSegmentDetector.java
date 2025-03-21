package com.itservices.gpxanalyzer.data.extrema.detector;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ExtremaSegmentDetector {

    private List<PrimitiveDataEntity> filtered;
    private List<PrimitiveDataEntity> smoothed;
    private List<Extremum> extrema;

    public List<Segment> addMissingSegments(List<Segment> extremumSegmentList, SegmentThresholds segmentThresholds) {

        List<Segment> newExtremumSegmentList = new ArrayList<>();
        if (extremumSegmentList.isEmpty()) {
            return extremumSegmentList;
        }
        newExtremumSegmentList.add(extremumSegmentList.get(0));

        for (int i = 1; i < extremumSegmentList.size(); i++) {

            Segment prevSegment = extremumSegmentList.get(i - 1);
            Segment segment = extremumSegmentList.get(i);

            if (prevSegment.endTime() != segment.startTime()) {

                Segment missingSegment = getMissingSegment(segment, prevSegment, segmentThresholds);

                newExtremumSegmentList.add(missingSegment);
            }

            newExtremumSegmentList.add(segment);
        }

        return newExtremumSegmentList;
    }

    @NonNull
    private static Segment getMissingSegment(Segment segment, Segment prevSegment, SegmentThresholds segmentThresholds) {
        double deltaValMissing = segment.startVal() - prevSegment.endVal();

        SegmentTrendType segmentTrendType = SegmentTrendType.CONSTANT;

        if (deltaValMissing > 0.0) {
            if (Math.abs(deltaValMissing) > segmentThresholds.deviationThreshold()) {
                segmentTrendType = SegmentTrendType.UP;
            }
        } else {
            if (Math.abs(deltaValMissing) > segmentThresholds.deviationThreshold()) {
                segmentTrendType = SegmentTrendType.DOWN;
            }
        }

        return new Segment(
                prevSegment.endIndex(), segment.startIndex(),
                prevSegment.endTime(), segment.startTime(),
                prevSegment.endVal(), segment.startVal(),
                segmentTrendType);
    }

    // Window function types
    public enum WindowType {
        TRIANGULAR,
        HANNING,
        GAUSSIAN
    }

    // If value changes are < ±EPSILON, treat derivative as zero (lower EPSILON to catch subtler slopes)
    private static final double EPSILON = 0.000000000001;

    // If accuracy is worse than 50, skip
    public static final float DEFAULT_MAX_VALUE_ACCURACY = 50.0f;

    // Extrema types
    private enum ExtremaType {
        MIN, MAX
    }

    // Container for an extremum (local min or local max)
    private record Extremum(int index,         // index in the smoothed data
                            ExtremaType type // MIN or MAX
    ) {
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

    // --------------------------------------------------------------------------
    // SINGLE-PASS DETECTION OF ASC/DESC
    // --------------------------------------------------------------------------

    /**
     * Detects both ascending (MIN->MAX) and descending (MAX->MIN) segments
     * in one pass through the extrema list, preventing overlaps.
     *
     * @param segmentThresholds
     * @return A single list of non-overlapping segments (UP or DOWN).
     * @see SegmentThresholds
     */
    public List<Segment> detectSegmentsOneRun(
            SegmentThresholds segmentThresholds
    ) {

        // 4) Build segments from consecutive pairs of extrema
        List<Segment> segments = new ArrayList<>();

        for (int i = 0; i < extrema.size() - 1; i++) {
            Extremum e1 = extrema.get(i);
            Extremum e2 = extrema.get(i + 1);

            //Log.d(ExtremaSegmentDetector.class.getSimpleName(), "detectSegmentsOneRun() e1 = [" + e1.index + "], e2 = [" + e2.index + "]");

            // e1 must come before e2 in time
/*            if (e2.index <= e1.index) {
                continue;
            }*/

            // Get the start/end points
            PrimitiveDataEntity p1 = smoothed.get(e1.index);
            PrimitiveDataEntity p2 = smoothed.get(e2.index);

            double amplitude = Math.abs(p2.getValue() - p1.getValue());
            long dtMillis = p2.getTimestamp() - p1.getTimestamp();
            if (dtMillis <= 0) {
                continue;
            }
            double dtSec = dtMillis / 1000.0;
            double avgDerivative = amplitude / dtSec;

            // Check if (MIN -> MAX)
            if (e1.type == ExtremaType.MIN && e2.type == ExtremaType.MAX) {
                // ascending candidate
                if ((p2.getValue() > p1.getValue()) &&
                        (amplitude >= segmentThresholds.deviationThreshold())) {

                    segments.add(new Segment(e1.index, e2.index, p1.getTimestamp(), p2.getTimestamp(), p1.getValue(), p2.getValue(), SegmentTrendType.UP));
                }
            }
            // or (MAX -> MIN)
            else if (e1.type == ExtremaType.MAX && e2.type == ExtremaType.MIN) {
                // descending candidate
                if ((p1.getValue() > p2.getValue()) &&
                        (amplitude >= segmentThresholds.deviationThreshold())) {

                    segments.add(new Segment(e1.index, e2.index, p1.getTimestamp(), p2.getTimestamp(), p1.getValue(), p2.getValue(), SegmentTrendType.DOWN));
                }
            }
        }

        // Because this uses consecutive pairs of extrema in chronological order,
        // none of these segments overlap in time.
        return segments;
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
            smoothedList.get(i).setValue(smoothedValue);
        }
        return smoothedList;
    }


    private static double[] derivativeRungeKutta(List<PrimitiveDataEntity> smoothed) {
        double[] derivative = new double[smoothed.size() - 1];
        for (int i = 0; i < smoothed.size() - 1; i++) {
            PrimitiveDataEntity next = smoothed.get(i + 1);
            PrimitiveDataEntity current = smoothed.get(i);
            double h = TimeUnit.MILLISECONDS.toSeconds(next.getTimestamp() - current.getTimestamp());


            derivative[i] = smoothed.get(i + 1).getValue() - smoothed.get(i).getValue();
        }

        return derivative;
    }

    // --------------------------------------------------------------------------
    // 3) FIND LOCAL EXTREMA
    // --------------------------------------------------------------------------
    private static List<Extremum> findLocalExtrema(List<PrimitiveDataEntity> smoothed) {
        List<Extremum> result = new ArrayList<>();
        if (smoothed == null || smoothed.size() < 3) return result;

        int n = smoothed.size();
        // Compute discrete derivative
        double[] derivative = new double[n];
        for (int i = 1; i < n; i++) {
            PrimitiveDataEntity prev = smoothed.get(i - 1);
            PrimitiveDataEntity current = smoothed.get(i);


            double dt = (double)Math.abs(current.getTimestamp() - prev.getTimestamp()) / 1000.0;

            derivative[i] = (current.getValue() - prev.getValue()) / dt;

        }

        for (int i = 1; i < n; i++) {
            //Log.d(ExtremaSegmentDetector.class.getSimpleName(), "findLocalExtrema()  derivative["+i+"] = [" + derivative[i] + "]");

            double prevSign = signWithEpsilon(derivative[i - 1], EPSILON);
            double currSign = signWithEpsilon(derivative[i], EPSILON);

            // local minimum => slope from negative to positive
            if (prevSign < 0 && currSign > 0) {
                result.add(new Extremum(i, ExtremaType.MIN));
            }
            // local maximum => slope from positive to negative
            else if (prevSign > 0 && currSign < 0) {
                result.add(new Extremum(i, ExtremaType.MAX));
            }
        }

        // Find missing ending extremum :
        Extremum lastOne = result.get( result.size() - 1 );
        Extremum beforeLastOne = result.get( result.size() - 2 );

        int lastIndex = n-1;

        if ( lastOne.index < n-1 ) {
            //Missing ending extremum!
            switch (beforeLastOne.type) {
                case MIN -> {
                    if (lastOne.type == ExtremaType.MAX) {
                        result.add(new Extremum(lastIndex, ExtremaType.MIN));
                    }
                }
                case MAX -> {
                    if (lastOne.type == ExtremaType.MIN) {
                        result.add(new Extremum(lastIndex, ExtremaType.MAX));
                    }
                }
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
        // Ensure the window size is at least 3 and an odd number
        if (size < 3 || (size % 2 == 0)) {
            throw new IllegalArgumentException("Window size must be odd and >= 3");
        }

        double[] window = new double[size];
        int M = size - 1;  // Maximum index for the window
        double center = M / 2.0;  // Center index of the window

        switch (type) {
            case TRIANGULAR:
                // Triangular window function:
                // w[n] = 1 - |(n - M/2) / (M/2)|
                for (int n = 0; n < size; n++) {
                    window[n] = 1.0 - Math.abs(n - center) / center;
                }
                break;

            case HANNING:
                // Hanning window function:
                // w[n] = 0.5 - 0.5 * cos(2πn / M)
                // The scaleFactor allows parameter tuning if param > 0
                double scaleFactor = (param > 0) ? param : 1.0;
                for (int n = 0; n < size; n++) {
                    window[n] = (0.5 - 0.5 * Math.cos((2.0 * Math.PI * n) / M)) * scaleFactor;
                }
                break;

            case GAUSSIAN:
                // Gaussian window function:
                // w[n] = exp(-0.5 * ((n - M/2) / (sigma * M/2))^2)
                // Sigma controls the spread; if param is not set, it is dynamically adjusted based on size
                double sigma = (param > 0) ? param : (0.4 + (0.1 * (size / 25.0)));
                for (int n = 0; n < size; n++) {
                    double x = (n - center) / (sigma * center);
                    window[n] = Math.exp(-0.5 * x * x);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown window type: " + type);
        }

        // Normalize the window so that the sum of weights equals 1
        double sum = 0;
        for (double w : window) {
            sum += w;
        }
        for (int i = 0; i < size; i++) {
            window[i] /= sum;
        }

        return window;
    }
}
