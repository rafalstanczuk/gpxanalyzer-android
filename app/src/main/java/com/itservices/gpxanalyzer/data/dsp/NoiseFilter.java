package com.itservices.gpxanalyzer.data.dsp;

import com.itservices.gpxanalyzer.audio.audiocapture.AudioBuffer;

public class NoiseFilter {


    private static final double THRESHOLD_FACTOR = 1.5;

    /**
     * Applies a naive noise gate to the given audio buffer. Samples
     * below a computed threshold are set to zero, reducing background noise.
     *
     * @param audioBuffer short[] containing the raw PCM audio samples.
     */
    public static AudioBuffer filter(AudioBuffer audioBuffer) {
        short[] audioBufferArray = audioBuffer.getAudioBufferArray();
        // 1. Compute average of absolute values to estimate the "noise floor"
        long sum = 0;
        for (short sample : audioBufferArray) {
            sum += Math.abs(sample);
        }
        double average = (double) sum / audioBufferArray.length;

        // 2. Define a threshold above the average noise floor
        //    The factor can be tweaked as needed. For instance, 1.5 or 2.0
        double threshold = average * THRESHOLD_FACTOR;

        // 3. Zero out samples below threshold
        for (int i = 0; i < audioBufferArray.length; i++) {
            if (Math.abs(audioBufferArray[i]) < threshold) {
                audioBufferArray[i] = 0;
            }
        }

        audioBuffer.setAudioBufferArray(audioBufferArray);

        return audioBuffer;
    }
}
