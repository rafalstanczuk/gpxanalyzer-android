package com.itservices.gpxanalyzer.fftspectrum;

import java.util.Arrays;

public class AudioSpectrum {

    private final double[] audioSpectrum;
    private int sampleRate;

    public AudioSpectrum(double[] audioSpectrum, int sampleRate) {
        this.audioSpectrum = audioSpectrum;
        this.sampleRate = sampleRate;
    }

    public double[] getAudioSpectrum() {
        return audioSpectrum;
    }

    public float getFrequency(int index) {
        return index * ( (float)sampleRate / (float)audioSpectrum.length);
    }

    @Override
    public String toString() {
        return "AudioSpectrum{" +
                "audioSpectrum=" + Arrays.toString(audioSpectrum) +
                '}';
    }
}
