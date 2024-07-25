package com.itservices.gpxanalyzer.fftspectrum;

import java.util.Arrays;

public class AudioSpectrum {

    private final double[] audioSpectrum;

    public AudioSpectrum(double[] audioSpectrum) {
        this.audioSpectrum = audioSpectrum;
    }

    public double[] getAudioSpectrum() {
        return audioSpectrum;
    }

    @Override
    public String toString() {
        return "AudioSpectrum{" +
                "audioSpectrum=" + Arrays.toString(audioSpectrum) +
                '}';
    }
}
