package com.itservices.gpxanalyzer.logbook;

public enum ViewMode {
	TREND_CURVE,
	INFO_ONLY_VIEW;

	public ViewMode getNextCyclic() {
		int currOrdinal  = ordinal();
		int maxOrdinal = values().length-1;

		return currOrdinal==maxOrdinal ? values()[0] : values()[currOrdinal+1];
	}
}
