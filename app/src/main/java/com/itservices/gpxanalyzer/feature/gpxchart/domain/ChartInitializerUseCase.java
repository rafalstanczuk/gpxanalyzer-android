package com.itservices.gpxanalyzer.feature.gpxchart.domain;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItem;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Use case responsible for initializing a chart associated with a {@link ChartAreaItem}.
 * This encapsulates the logic for triggering the initialization process of a chart's controller.
 */
public class ChartInitializerUseCase {

    /**
     * Constructor for dependency injection.
     */
    @Inject
    ChartInitializerUseCase() {
    }

    /**
     * Initializes the chart controlled by the {@link ChartAreaItem#getChartController()}.
     * This method delegates the initialization call to the chart controller and returns
     * a {@link Single} that emits the original {@link ChartAreaItem} upon successful
     * initialization.
     *
     * @param chartAreaItem The item containing the chart controller to initialize.
     * @return A {@link Single} emitting the {@code chartAreaItem} upon successful initialization.
     */
    @NonNull
    public Single<ChartAreaItem> initChart(ChartAreaItem chartAreaItem) {
        return chartAreaItem.getChartController()
                .initChart()
                .map(status -> chartAreaItem);
    }
}
