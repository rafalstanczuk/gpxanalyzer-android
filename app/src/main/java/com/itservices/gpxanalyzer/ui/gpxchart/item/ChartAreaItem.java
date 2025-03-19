package com.itservices.gpxanalyzer.ui.gpxchart.item;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.Observable;

public class ChartAreaItem {

    private DataEntityWrapper dataEntityWrapper;
    private final ChartController chartController;
    private MutableLiveData<GpxViewMode> viewModeLiveData = new MutableLiveData<>();


    @AssistedInject
    public ChartAreaItem(@Assisted GpxViewMode viewMode,
                         @Assisted("drawX") boolean drawX,
                         @Assisted("drawIconsEnabled") boolean drawIconsEnabled,
                         ChartController chartController) {
        this.chartController = chartController;

        viewModeLiveData.setValue(viewMode);

        this.chartController.setDrawIconsEnabled(drawIconsEnabled);
        this.chartController.setDrawXLabels(drawX);
    }

    public ChartController getChartController() {
        return chartController;
    }

    public LiveData<GpxViewMode> getViewMode() {
        return viewModeLiveData;
    }

    public boolean isDrawIconsEnabled() {
        return chartController.isDrawIconsEnabled();
    }

    public boolean isDrawAscDescSegEnabled() {
        return chartController.isDrawAscDescSegEnabled();
    }

    public void setDataEntityWrapper(DataEntityWrapper dataEntityWrapper) {
        if (this.dataEntityWrapper != null) {
            this.dataEntityWrapper = null;
        }
        this.dataEntityWrapper = dataEntityWrapper;
    }

    public DataEntityWrapper getDataEntityWrapper() {
        return dataEntityWrapper;
    }

    public void setViewMode(GpxViewMode viewMode) {
        viewModeLiveData.setValue(viewMode);
    }

    public void setDrawX(boolean drawX) {
        chartController.setDrawXLabels(drawX);
    }

    public Observable<RequestStatus> updateChart() {
        return chartController.updateChartData(dataEntityWrapper);
    }
}