package com.itservices.gpxanalyzer.ui.gpxchart;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.chart.ChartController;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ChartAreaItem {

    private final ChartController chartController;
    private MutableLiveData<ViewMode> viewModeLiveData = new MutableLiveData<>();

    private MutableLiveData<Integer> visibilityLiveData = new MutableLiveData<>(View.VISIBLE);

    private MutableLiveData<Boolean> enabledLiveData = new MutableLiveData<>(true);

    @AssistedInject
    public ChartAreaItem(@Assisted ViewMode viewMode,
                         @Assisted("drawX") boolean drawX,
                         @Assisted("drawIconsEnabled") boolean drawIconsEnabled,
                         ChartController chartController ) {
        this.chartController = chartController;
        visibilityLiveData.setValue( getVisibility(viewMode) );
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

    public LiveData<Integer> getVisibilityLiveData() {
        return visibilityLiveData;
    }

    public LiveData<Boolean> getEnabledLiveData() {
        return enabledLiveData;
    }

    public void setViewMode(ViewMode viewMode) {
        visibilityLiveData.setValue( getVisibility(viewMode) );
        enabledLiveData.setValue( getEnabled(viewMode) );

        viewModeLiveData.setValue(viewMode);
    }

    public void setDrawX(boolean drawX) {
        chartController.setDrawXLabels(drawX);
    }

    private int getVisibility(ViewMode viewMode) {
        return ( viewMode != ViewMode.DISABLED) ? View.VISIBLE : View.GONE;
    }
    private boolean getEnabled(ViewMode viewMode) {
        return viewMode != ViewMode.DISABLED;
    }
}