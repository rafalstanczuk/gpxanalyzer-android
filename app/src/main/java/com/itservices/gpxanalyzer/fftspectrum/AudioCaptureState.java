package com.itservices.gpxanalyzer.fftspectrum;

public enum AudioCaptureState {
	OFF, ON;

	public AudioCaptureState getNextCyclic() {
		int currOrdinal  = ordinal();
		int maxOrdinal = values().length-1;

		return currOrdinal==maxOrdinal ? values()[0] : values()[currOrdinal+1];
	}
}
