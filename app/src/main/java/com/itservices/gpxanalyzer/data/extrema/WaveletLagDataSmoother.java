package com.itservices.gpxanalyzer.data.extrema;


import com.itservices.gpxanalyzer.data.extrema.detector.ExtremaSegmentDetector;
import com.itservices.gpxanalyzer.data.extrema.detector.PrimitiveDataEntity;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;
import java.util.Vector;

class WaveletLagDataSmoother {

    /**
     * Computes an adaptive window function that highlights amplitude changes
     * while preventing excessive smoothing. Uses FFT for noise reduction.
     *
     * @param dataEntities The list of data points.
     * @param stdDev       The standard deviation of the dataset.
     * @param windowType   The type of window function to apply.
     * @return A double array representing the computed noise-filtered window function.
     */
    public static double[] computeAdaptiveWindowFunction(
            Vector<PrimitiveDataEntity> dataEntities,
            double stdDev,
            ExtremaSegmentDetector.WindowType windowType) {

        if (dataEntities == null || dataEntities.isEmpty()) {
            return new double[]{1.0}; // Default window if no data
        }

        int dataSize = dataEntities.size();

        // Compute an adaptive lag that prioritizes amplitude preservation
        int optimalLag = computeAdaptiveLag(dataEntities, dataSize, stdDev);

        // Generate the adaptive window function using the computed lag
        return generateAdaptiveWindowFunction(optimalLag, windowType, dataSize, stdDev, dataEntities);
    }

    /**
     * Computes an adaptive lag that minimizes smoothing when amplitude changes are high.
     */
    private static int computeAdaptiveLag(Vector<PrimitiveDataEntity> dataEntities, int dataSize, double stdDev) {
        int N = dataEntities.size();
        if (N < 10) return 3;

        double[] waveletScales = new double[N / 2];

        // Compute wavelet energy
        for (int scale = 1; scale < N / 2; scale++) {
            double sumEnergy = 0;
            for (int i = 0; i < N - scale; i++) {
                double diff = dataEntities.get(i).getValue() - dataEntities.get(i + scale).getValue();
                sumEnergy += diff * diff;
            }
            waveletScales[scale] = sumEnergy / (N - scale);
        }

        int optimalLag = 3;
        for (int i = 2; i < waveletScales.length; i++) {
            if (waveletScales[i] > waveletScales[optimalLag]) {
                optimalLag = i;
            }
        }

        // Reduce smoothing when standard deviation is high
        double smoothingFactor = 1.0 - Math.min(0.8, stdDev / (10.0 + stdDev));
        optimalLag = (int) (optimalLag * smoothingFactor);

        // Limit window size
        int maxAllowedLag = Math.max(3, dataSize / 200);
        if (optimalLag > maxAllowedLag) {
            optimalLag = maxAllowedLag;
        }

        // Ensure the lag is at least 3 and is odd
        if (optimalLag < 3) {
            optimalLag = 3;
        }
        if (optimalLag % 2 == 0) {
            optimalLag -= 1;
        }

        return optimalLag;
    }

    /**
     * Generates an adaptive window function using FFT to remove high-frequency noise.
     */
    private static double[] generateAdaptiveWindowFunction(int size, ExtremaSegmentDetector.WindowType type, int dataSize, double stdDev, Vector<PrimitiveDataEntity> dataEntities) {
        int maxAllowedWindowSize = Math.max(3, dataSize / 200);
        if (size > maxAllowedWindowSize) {
            size = maxAllowedWindowSize;
        }

        // Ensure the window size is odd
        if (size < 3 || (size % 2 == 0)) {
            size = (size < 3) ? 3 : size + 1;
        }

        // Step 1: Perform FFT on the signal
        double[] signalFFT = applyFFT(dataEntities);

        // Step 2: Compute noise threshold from FFT
        double noiseThreshold = computeNoiseThreshold(signalFFT, stdDev);

        // Step 3: Apply window function and remove high-frequency noise
        return applyFilteredWindowFunction(size, type, signalFFT, noiseThreshold);
    }

    /**
     * Applies FFT using Apache Commons Math to analyze the frequency spectrum.
     * Fixes issue with non-power-of-2 input by padding to the next power of 2.
     *
     * @param dataEntities The input signal as a list of PrimitiveDataEntity.
     * @return FFT-transformed array of magnitudes.
     */
    private static double[] applyFFT(Vector<PrimitiveDataEntity> dataEntities) {
        int N = dataEntities.size();
        int fftSize = nextPowerOf2(N); // Fix: Pad to next power of 2

        double[] paddedSignal = new double[fftSize];

        // Copy original signal into padded array
        for (int i = 0; i < N; i++) {
            paddedSignal[i] = dataEntities.get(i).getValue();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] result = fft.transform(paddedSignal, TransformType.FORWARD);

        // Compute magnitudes of the FFT result
        double[] magnitudes = new double[result.length];
        for (int i = 0; i < result.length; i++) {
            magnitudes[i] = result[i].abs();
        }

        return magnitudes;
    }

    /**
     * Computes the next power of 2 greater than or equal to `n`.
     *
     * @param n The input number.
     * @return The next power of 2.
     */
    private static int nextPowerOf2(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * Computes a noise threshold by analyzing the FFT spectrum.
     */
    private static double computeNoiseThreshold(double[] fftData, double stdDev) {
        int N = fftData.length;
        double totalEnergy = 0.0;
        double noiseEnergy = 0.0;

        for (int i = 1; i < N / 2; i++) {
            totalEnergy += fftData[i];
            if (i > N / 4) {
                noiseEnergy += fftData[i];
            }
        }

        double noiseRatio = noiseEnergy / (totalEnergy + 1e-10);
        return Math.min(noiseRatio * stdDev, stdDev * 0.1);
    }

    /**
     * Applies a window function while removing high-frequency noise.
     */
    private static double[] applyFilteredWindowFunction(int size, ExtremaSegmentDetector.WindowType type, double[] fftData, double noiseThreshold) {
        double[] window = new double[size];
        int M = size - 1;
        double center = M / 2.0;

        double noiseFactor = 1.0 - Math.min(0.7, noiseThreshold);

        switch (type) {
            case TRIANGULAR:
                for (int n = 0; n < size; n++) {
                    window[n] = (1.0 - Math.abs(n - center) / center) * noiseFactor;
                }
                break;
            case HANNING:
                for (int n = 0; n < size; n++) {
                    window[n] = (0.5 - 0.5 * Math.cos((2.0 * Math.PI * n) / M)) * noiseFactor;
                }
                break;
            case GAUSSIAN:
                double sigma = 0.2 + (0.05 * (size / 25.0));
                for (int n = 0; n < size; n++) {
                    double x = (n - center) / (sigma * center);
                    window[n] = Math.exp(-0.5 * x * x) * noiseFactor;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown window type: " + type);
        }

        // Normalize the window function
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

