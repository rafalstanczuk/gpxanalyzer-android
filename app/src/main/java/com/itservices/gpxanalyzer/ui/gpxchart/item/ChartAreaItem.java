package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.Observable;
import io.reactivex.Single;

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
        if (dataEntityWrapper == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Attempted to set null data wrapper");
            return;
        }
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting data wrapper with primary index: " + dataEntityWrapper.getPrimaryDataIndex());
        this.dataEntityWrapper = dataEntityWrapper;
    }

    public DataEntityWrapper getDataEntityWrapper() {
        return dataEntityWrapper;
    }

    public void setViewMode(GpxViewMode viewMode) {
        if (viewMode == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Attempted to set null view mode");
            return;
        }
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting view mode: " + viewMode.name());
        viewModeLiveData.setValue(viewMode);
    }

    public void setDrawX(boolean drawX) {
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting drawX: " + drawX);
        chartController.setDrawXLabels(drawX);
    }

    public Single<RequestStatus> updateChart() {
        if (dataEntityWrapper == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Cannot update chart - data wrapper is null");
            return Single.just(RequestStatus.ERROR);
        }
        Log.d(ChartAreaItem.class.getSimpleName(), "Updating chart with data wrapper index: " + dataEntityWrapper.getPrimaryDataIndex());
        return chartController.updateChartData(dataEntityWrapper);
    }
}