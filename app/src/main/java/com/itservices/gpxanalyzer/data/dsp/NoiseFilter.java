package com.itservices.gpxanalyzer.data.dsp;

public class NoiseFilter {

    private static final double THRESHOLD_FACTOR = 1.5;

    /**
     * Applies a naive noise gate to the given signal buffer. Samples
     * below a computed threshold are set to zero, reducing background noise.
     *
     * @param signalBuffer short[] containing the raw PCM signal samples.
     */
    public static SignalBuffer filter(SignalBuffer signalBuffer) {
        short[] signalBufferArray = signalBuffer.getBufferArray();
        // 1. Compute average of absolute values to estimate the "noise floor"
        long sum = 0;
        for (short sample : signalBufferArray) {
            sum += Math.abs(sample);
        }
        double average = (double) sum / signalBufferArray.length;

        // 2. Define a threshold above the average noise floor
        //    The factor can be tweaked as needed. For instance, 1.5 or 2.0
        double threshold = average * THRESHOLD_FACTOR;

        // 3. Zero out samples below threshold
        for (int i = 0; i < signalBufferArray.length; i++) {
            if (Math.abs(signalBufferArray[i]) < threshold) {
                signalBufferArray[i] = 0;
            }
        }

        signalBuffer.setBufferArray(signalBufferArray);

        return signalBuffer;
    }
}
