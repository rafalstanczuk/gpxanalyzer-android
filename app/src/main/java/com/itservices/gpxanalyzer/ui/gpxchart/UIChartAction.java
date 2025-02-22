package com.itservices.gpxanalyzer.ui.gpxchart;


import javax.annotation.Nullable;

public record UIChartAction(ChartAreaItem item,
                            UIChartActionType uiChartActionType,
                            @Nullable Object newState) {


    public enum UIChartActionType {
        SWITCH_VIEW_MODE,
        SWITCH_DOTS,
        ZOOM_IN,
        ZOOM_OUT,
        AUTO_SCALING
    }
}

