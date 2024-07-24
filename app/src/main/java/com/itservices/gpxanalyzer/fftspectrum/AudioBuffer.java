package com.itservices.gpxanalyzer.fftspectrum;

public class AudioBuffer {

    private final short[] audioBuffer;

    public AudioBuffer(short[] audioBuffer) {
        this.audioBuffer = audioBuffer;
    }

    public short[] getAudioBuffer() {
        return audioBuffer;
    }
}
