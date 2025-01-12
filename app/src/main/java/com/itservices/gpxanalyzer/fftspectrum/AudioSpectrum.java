package com.itservices.gpxanalyzer.fftspectrum;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioSpectrum {

    private final double[] audioSpectrum;
    private int sampleRate;

    public AudioSpectrum(double[] audioSpectrum, int sampleRate) {
        this.audioSpectrum = audioSpectrum;
        this.sampleRate = sampleRate;
    }

    public double[] getAmplitudeArray() {
        return audioSpectrum;
    }

    /**
     * Static helper method to get a list of pairs (frequency, amplitude).
     */
    public static List<Pair<Float, Double>> getPositiveFrequencyAmplitudePairList(AudioSpectrum spectrum) {
        List<Pair<Float, Double>> list = new ArrayList<>();
        double[] amplitudeArray = spectrum.getAmplitudeArray();

        float deltaFreq = ((float) spectrum.sampleRate / (float) amplitudeArray.length);

        for (int i = 0; i < amplitudeArray.length / 2; i++) {
            float frequency = i * deltaFreq;
            double amplitude = amplitudeArray[i];
            list.add(new Pair<>(frequency, amplitude));
        }

        return list;
    }




    @Override
    public String toString() {
        return "AudioSpectrum{" +
                "audioSpectrum=" + Arrays.toString(audioSpectrum) +
                ", sampleRate=" + sampleRate +
                '}';
    }
}
