package com.itservices.gpxanalyzer.fftspectrum;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FFTProcessor {

    @Inject
    public FFT fft;

    @Inject
    public FFTProcessor(){}

    public void init(AudioCapture audioCapture) {
        fft.init(audioCapture);
    }

    @NonNull
    public AudioSpectrum process(Object audioBufferObj) {
        if (audioBufferObj instanceof AudioBuffer) {
            AudioBuffer audioBuffer = (AudioBuffer) audioBufferObj;
            short[] audioBufferArray = ((AudioBuffer) audioBufferObj).getAudioBufferArray();

            double[] real = new double[audioBufferArray.length];
            double[] imag = new double[audioBufferArray.length];
            for (int i = 0; i < audioBufferArray.length; i++) {
                real[i] = audioBufferArray[i];
                imag[i] = 0;
            }

            fft.fft(real, imag);

            double[] magnitude = new double[audioBufferArray.length];
            for (int i = 0; i < audioBufferArray.length; i++) {
                magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
            }

            return new AudioSpectrum(magnitude, audioBuffer.getSampleRate());
        } else {
            return new AudioSpectrum(new double[1], 0);
        }
    }

}
