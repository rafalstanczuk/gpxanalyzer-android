package com.itservices.gpxanalyzer.data.extrema;

import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;
import com.itservices.gpxanalyzer.data.extrema.detector.SegmentThresholds;

import java.util.*;

public class WaveletLagDataSmoother {

    /**
     * Computes an optimized window function that highlights amplitude changes rather than over-smoothing.
     *
     * @param dataEntities List of data points.
     * @param thresholds   Segment thresholds (minAscAmp & minDescAmp control how much smoothing is applied).
     * @param windowType   The type of window function to apply.
     * @return A double array representing the computed window function.
     */
    public static double[] computeAdaptiveWindowFunction(
            List<PrimitiveDataEntity> dataEntities,
            SegmentThresholds thresholds,
            ExtremaSegmentDetector.WindowType windowType) {

        if (dataEntities == null || dataEntities.isEmpty()) return new double[]{1.0}; // Default if no data

        // Extract numerical values from dataEntities
        int size = dataEntities.size();
        double[] values = new double[size];

        for (int i = 0; i < size; i++) {
            values[i] = dataEntities.get(i).getValue();
        }

        // Compute optimal smoothing lag
        int optimalLag = computeOptimalLagWavelet(values, thresholds);

        // Ensure `optimalLag` is valid (≥3 and odd)
        if (optimalLag < 3) optimalLag = 3;
        if (optimalLag % 2 == 0) optimalLag += 1;

        // Generate the window function
        return generateWindowFunction(optimalLag, windowType, thresholds);
    }

    /**
     * Computes the optimal lag for smoothing while preserving amplitude variations.
     *
     * @param values     The input data.
     * @param thresholds The amplitude thresholds for adjusting smoothing.
     * @return Optimal smoothing lag (≥3 and odd).
     */
    private static int computeOptimalLagWavelet(double[] values, SegmentThresholds thresholds) {
        int N = values.length;
        if (N < 10) return 3; // Minimum lag for small datasets

        double[] waveletScales = new double[N / 2];

        // Compute energy of different wavelet scales
        for (int scale = 1; scale < N / 2; scale++) {
            double sumEnergy = 0;
            for (int i = 0; i < N - scale; i++) {
                double diff = values[i] - values[i + scale];
                sumEnergy += diff * diff;
            }
            waveletScales[scale] = sumEnergy / (N - scale);
        }

        // Find the highest energy scale (best for smoothing)
        int optimalWaveletScale = 3;
        for (int i = 2; i < waveletScales.length; i++) {
            if (waveletScales[i] > waveletScales[optimalWaveletScale]) {
                optimalWaveletScale = i;
            }
        }

        // Reduce smoothing effect when high amplitude changes are detected
        double amplitudeFactor = 1.0 / (1.0 + Math.max(thresholds.minAscAmp(), thresholds.minDescAmp()));
        optimalWaveletScale = (int) (optimalWaveletScale * amplitudeFactor);

        // Ensure valid values (≥3 and odd)
        if (optimalWaveletScale < 3) optimalWaveletScale = 3;
        if (optimalWaveletScale % 2 == 0) optimalWaveletScale += 1;

        return optimalWaveletScale;
    }

    /**
     * Generates a smoothing window function based on the computed optimal lag.
     *
     * @param size        The computed window size (lag-based).
     * @param type        The type of window function to apply.
     * @param thresholds  The amplitude thresholds that control smoothing intensity.
     * @return A normalized double array representing the computed window function.
     */
    private static double[] generateWindowFunction(int size, ExtremaSegmentDetector.WindowType type, SegmentThresholds thresholds) {
        // Ensure the window size is valid
        if (size < 3 || (size % 2 == 0)) {
            size += 1; // Ensure odd size
        }

        double[] window = new double[size];
        int M = size - 1;
        double center = M / 2.0;

        // Adjust the smoothing strength based on amplitude thresholds
        double amplitudeFactor = 1.0 / (1.0 + Math.max(thresholds.minAscAmp(), thresholds.minDescAmp()));

        switch (type) {
            case TRIANGULAR:
                for (int n = 0; n < size; n++) {
                    window[n] = (1.0 - Math.abs(n - center) / center) * amplitudeFactor;
                }
                break;

            case HANNING:
                for (int n = 0; n < size; n++) {
                    window[n] = (0.5 - 0.5 * Math.cos((2.0 * Math.PI * n) / M)) * amplitudeFactor;
                }
                break;

            case GAUSSIAN:
                double sigma = 0.4 + (0.1 * (size / 25.0));
                for (int n = 0; n < size; n++) {
                    double x = (n - center) / (sigma * center);
                    window[n] = Math.exp(-0.5 * x * x) * amplitudeFactor;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown window type: " + type);
        }

        // Normalize the window function (sum of weights = 1)
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
