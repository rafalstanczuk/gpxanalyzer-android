package com.itservices.gpxanalyzer.core.data.cache.processed.chart;

public enum ChartSlot {
    SLOT_0,
    SLOT_1;


    public static ChartSlot fromPosition(int position) throws IndexOutOfBoundsException {
      return ChartSlot.values()[position];
    }
}
