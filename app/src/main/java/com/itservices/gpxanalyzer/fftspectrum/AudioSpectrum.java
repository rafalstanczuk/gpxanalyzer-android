package com.itservices.gpxanalyzer.fftspectrum;

public class AudioSpectrum {

    private final double[] audioSpectrum;

    public AudioSpectrum(double[] audioSpectrum) {
        this.audioSpectrum = audioSpectrum;
    }

    public double[] getAudioSpectrum() {
        return audioSpectrum;
    }
}
