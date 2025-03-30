package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.Single;

/**
 * Represents a chart area displaying GPX data in the application.
 * This class acts as a model for individual chart components, handling the connection
 * between the data layer (DataEntityWrapper) and the chart visualization (ChartController).
 * It manages chart configuration, data binding, and chart update operations.
 *
 * The class supports different view modes for displaying various aspects of GPX data,
 * and maintains state for visual properties like axis labels and icons.
 */
public class ChartAreaItem {

    private final ChartController chartController;
    private MutableLiveData<GpxViewMode> viewModeLiveData = new MutableLiveData<>();

    private ChartSlot chartSlot = null;

    /**
     * Creates a new ChartAreaItem with the specified configuration.
     * Uses Dagger's assisted injection to provide required dependencies.
     *
     * @param viewMode The initial view mode for this chart
     * @param drawX Whether to draw X-axis labels
     * @param drawIconsEnabled Whether to enable drawing of icons on the chart
     * @param chartController The controller for the underlying chart component
     */
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

    /**
     * Gets the chart controller associated with this chart area.
     *
     * @return The ChartController instance
     */
    public ChartController getChartController() {
        return chartController;
    }

    /**
     * Gets the view mode for this chart as a LiveData object.
     *
     * @return LiveData containing the current view mode
     */
    public LiveData<GpxViewMode> getViewMode() {
        return viewModeLiveData;
    }

    /**
     * Checks if drawing icons on the chart is enabled.
     *
     * @return true if icons are enabled, false otherwise
     */
    public boolean isDrawIconsEnabled() {
        return chartController.isDrawIconsEnabled();
    }

    /**
     * Checks if drawing ascent/descent segments on the chart is enabled.
     *
     * @return true if ascent/descent segments are enabled, false otherwise
     */
    public boolean isDrawAscDescSegEnabled() {
        return chartController.isDrawAscDescSegEnabled();
    }

    /**
     * Sets the view mode for this chart area.
     * The view mode determines which aspect of the GPX data will be visualized.
     *
     * @param viewMode The new view mode to set
     */
    public void setViewMode(GpxViewMode viewMode) {
        if (viewMode == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Attempted to set null view mode");
            return;
        }
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting view mode: " + viewMode.name());
        viewModeLiveData.setValue(viewMode);
    }

    /**
     * Sets whether to draw X-axis labels on the chart.
     *
     * @param drawX true to show X-axis labels, false to hide them
     */
    public void setDrawX(boolean drawX) {
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting drawX: " + drawX);
        chartController.setDrawXLabels(drawX);
    }

    /**
     * Updates the chart with current data.
     * This triggers a refresh of the chart visualization using the current data wrapper.
     *
     * @return A Single that emits the RequestStatus of the update operation
     */
    public Single<RequestStatus> updateChart(RawDataProcessed rawDataProcessed) {
        if (rawDataProcessed == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Cannot update chart - data wrapper is null");
            return Single.just(RequestStatus.ERROR);
        }

         return chartController.updateChartData(rawDataProcessed);
    }

    public void setChartSlot(ChartSlot chartSlot) {
        this.chartSlot = chartSlot;
    }

    public ChartSlot getChartSlot() {
        return chartSlot;
    }
}