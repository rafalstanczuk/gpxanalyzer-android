package com.itservices.gpxanalyzer.domain.dsp;

public class SignalSamplingProperties {
    private static final int SAMPLE_RATE = 44100;

    private final int sampleRate;

    private final int bufferSize;


    /**
     * Ex.
     *         int sampleRate = 44100;
     *
     *         int bufferSize = 1024;
     * @param sampleRate
     * @param bufferSize
     */
    public SignalSamplingProperties(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}