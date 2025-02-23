package com.itservices.gpxanalyzer.ui.gpxchart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ChartAreaItem {

    private StatisticResults statisticResults;
    private final ChartController chartController;
    private MutableLiveData<ViewMode> viewModeLiveData = new MutableLiveData<>();

    private MutableLiveData<Boolean> enabledLiveData = new MutableLiveData<>(true);

    private MutableLiveData<Boolean> isDrawIconsEnabledLiveData = new MutableLiveData<>(false);

    @AssistedInject
    public ChartAreaItem(@Assisted ViewMode viewMode,
                         @Assisted("drawX") boolean drawX,
                         @Assisted("drawIconsEnabled") boolean drawIconsEnabled,
                         ChartController chartController) {
        this.chartController = chartController;

        isDrawIconsEnabledLiveData.setValue(chartController.isDrawIconsEnabled());
        viewModeLiveData.setValue(viewMode);

        this.chartController.setDrawIconsEnabled(drawIconsEnabled);
        this.chartController.setDrawXLabels(drawX);
    }

    public ChartController getChartController() {
        return chartController;
    }

    public LiveData<ViewMode> getViewMode() {
        return viewModeLiveData;
    }

    public boolean isDrawIconsEnabledLiveData() {
        return chartController.isDrawIconsEnabled();
    }

    public LiveData<Boolean> getEnabledLiveData() {
        return enabledLiveData;
    }

    public void setStatisticResults(StatisticResults statisticResults) {
        this.statisticResults = statisticResults;
    }

    public StatisticResults getStatisticResults() {
        return statisticResults;
    }

    public void setViewMode(ViewMode viewMode) {
        viewModeLiveData.setValue(viewMode);
    }

    public void setDrawX(boolean drawX) {
        chartController.setDrawXLabels(drawX);
    }
}