package com.itservices.gpxanalyzer.usecase;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;

import javax.inject.Inject;

import io.reactivex.Single;

public class ChartInitializer {
    @Inject
    ChartInitializer() {
    }

    @NonNull
    Single<ChartAreaItem> initChart(ChartAreaItem chartAreaItem) {
        return chartAreaItem.getChartController()
                .initChart()
                .map(status -> chartAreaItem);
    }
}
