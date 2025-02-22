package com.itservices.gpxanalyzer.ui.gpxchart;

public interface ChartAreaItemHandler {
    void onOnOffColorizedCirclesCheckBoxChanged(ChartAreaItem item, boolean isChecked);
    void onSwitchViewMode(ChartAreaItem item);

    void onZoomIn(ChartAreaItem item);

    void onZoomOut(ChartAreaItem item);

    void onAutoScaling(ChartAreaItem item);
}