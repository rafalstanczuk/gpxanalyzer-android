package com.itservices.gpxanalyzer.ui.gpxchart.item;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.Single;

public class ChartInitializer {
    @Inject
    ChartInitializer() {
    }

    @NonNull
    public Single<ChartAreaItem> initChart(ChartAreaItem chartAreaItem) {
        return chartAreaItem.getChartController()
                .initChart()
                .map(status -> chartAreaItem);
    }
}
