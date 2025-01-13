package com.itservices.gpxanalyzer.audio.audiocapture;

public class AudioBuffer {

    private short[] audioBufferArray;
    private int sampleRate;

    public AudioBuffer(short[] audioBufferArray, int sampleRate) {
        this.audioBufferArray = audioBufferArray;
        this.sampleRate = sampleRate;
    }

    public short[] getAudioBufferArray() {
        return audioBufferArray;
    }

    public void setAudioBufferArray(short[] audioBufferArray) {
        this.audioBufferArray = audioBufferArray;
    }

    public int getSampleRate() {
        return sampleRate;
    }
}
