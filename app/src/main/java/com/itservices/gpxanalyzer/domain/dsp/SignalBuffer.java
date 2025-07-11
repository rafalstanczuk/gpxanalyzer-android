package com.itservices.gpxanalyzer.domain.dsp;

public class SignalBuffer {

    private short[] bufferArray;
    private SignalSamplingProperties samplingProperties;

    public SignalBuffer(SignalSamplingProperties samplingProperties, short[] bufferArray) {
        this.bufferArray = bufferArray;
        this.samplingProperties = samplingProperties;
    }

    public SignalSamplingProperties getSamplingProperties() {
        return samplingProperties;
    }

    public short[] getBufferArray() {
        return bufferArray;
    }

    public void setBufferArray(short[] bufferArray) {
        this.bufferArray = bufferArray;
    }
}
