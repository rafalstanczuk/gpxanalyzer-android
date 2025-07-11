package com.itservices.gpxanalyzer.domain.dsp;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FFTProcessor {

    @Inject
    public FFT fft;

    @Inject
    public FFTProcessor() {
    }

    public void init(SignalSamplingProperties signalSamplingProperties) {
        fft.init(signalSamplingProperties);
    }

    @NonNull
    public SignalSpectrum process(SignalBuffer signalBuffer) {
        short[] signalBufferArray = signalBuffer.getBufferArray();

        double[] real = new double[signalBufferArray.length];
        double[] imag = new double[signalBufferArray.length];
        for (int i = 0; i < signalBufferArray.length; i++) {
            real[i] = signalBufferArray[i];
            imag[i] = 0;
        }

        fft.fft(real, imag);

        double[] magnitude = new double[signalBufferArray.length];
        for (int i = 0; i < signalBufferArray.length; i++) {
            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        return new SignalSpectrum(magnitude, signalBuffer.getSamplingProperties().getSampleRate());
    }

}
