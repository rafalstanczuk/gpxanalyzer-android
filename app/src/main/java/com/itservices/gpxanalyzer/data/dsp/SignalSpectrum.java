package com.itservices.gpxanalyzer.data.dsp;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalSpectrum {

    private final double[] signalSpectrum;
    private int sampleRate;

    public SignalSpectrum(double[] signalSpectrum, int sampleRate) {
        this.signalSpectrum = signalSpectrum;
        this.sampleRate = sampleRate;
    }

    public double[] getAmplitudeArray() {
        return signalSpectrum;
    }

    /**
     * Static helper method to get a list of pairs (frequency, amplitude).
     */
    public static List<Pair<Float, Double>> getPositiveFrequencyAmplitudePairList(SignalSpectrum spectrum) {
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
        return "SignalSpectrum{" +
                "signalSpectrum=" + Arrays.toString(signalSpectrum) +
                ", sampleRate=" + sampleRate +
                '}';
    }
}
