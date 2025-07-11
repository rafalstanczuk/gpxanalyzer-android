package com.itservices.gpxanalyzer.feature.gpxchart.ui.item;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.core.ui.components.chart.ChartController;
import com.itservices.gpxanalyzer.core.events.RequestStatus;
import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.core.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewMode;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.reactivex.Single;

/**
 * Represents a single chart area item within the chart list UI.
 * This class acts as a model for an individual chart component, holding its state and
 * providing an interface to interact with its underlying {@link ChartController}.
 * It bridges the gap between the ViewModel/data layer and the specific chart view.
 *
 * Key responsibilities include:
 * - Holding the current {@link GpxViewMode} for the chart.
 * - Managing state for chart drawing options (e.g., draw icons, draw ascent/descent segments) via LiveData.
 * - Providing access to the {@link ChartController} for initialization and updates.
 * - Storing the associated {@link ChartSlot} for event identification.
 */
public class ChartAreaItem {

    /** The controller responsible for managing the actual chart view (e.g., MPAndroidChart). */
    private final ChartController chartController;
    /** LiveData holding the current {@link GpxViewMode} (e.g., Altitude vs. Time). */
    private MutableLiveData<GpxViewMode> viewModeLiveData = new MutableLiveData<>();

    /** LiveData indicating whether drawing ascent/descent segments is enabled. */
    private MutableLiveData<Boolean> drawAscDescSegEnabledLiveData = new MutableLiveData<>(false);
    /** LiveData indicating whether drawing value-dependent icons (e.g., colored circles) is enabled. */
    private MutableLiveData<Boolean> drawIconsEnabledLiveData = new MutableLiveData<>(false);

    /** Identifier for the specific slot this chart occupies, used for event routing. */
    private ChartSlot chartSlot = null;

    /**
     * Creates a new ChartAreaItem using Dagger's assisted injection.
     *
     * @param viewMode           The initial {@link GpxViewMode} for this chart.
     * @param drawX              Whether to initially draw X-axis labels.
     * @param drawIconsEnabled   Whether to initially enable drawing of icons on the chart.
     * @param chartController    The controller for the underlying chart component (injected).
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

        // Initialize LiveData based on initial controller state if needed, or ensure consistency
        this.drawIconsEnabledLiveData.setValue(this.chartController.isDrawIconsEnabled());
        this.drawAscDescSegEnabledLiveData.setValue(this.chartController.isDrawAscDescSegEnabled());
    }

    /**
     * Gets the chart controller associated with this chart area item.
     *
     * @return The {@link ChartController} instance.
     */
    public ChartController getChartController() {
        return chartController;
    }

    /**
     * Gets the LiveData holding the current view mode for this chart.
     *
     * @return {@link LiveData} containing the current {@link GpxViewMode}.
     */
    public LiveData<GpxViewMode> getViewMode() {
        return viewModeLiveData;
    }

    /**
     * Checks directly with the controller if drawing icons on the chart is currently enabled.
     * Note: Prefer observing {@link #getIsDrawIconsEnabledLiveData()} for UI updates.
     *
     * @return true if icons are enabled in the controller, false otherwise.
     */
    public boolean isDrawIconsEnabledFromController() {
        return chartController.isDrawIconsEnabled();
    }

    /**
     * Checks directly with the controller if drawing ascent/descent segments is currently enabled.
     * Note: Prefer observing {@link #getIsDrawAscDescSegEnabledLiveData()} for UI updates.
     *
     * @return true if ascent/descent segments are enabled in the controller, false otherwise.
     */
    public boolean isDrawAscDescSegEnabledFromController() {
        return chartController.isDrawAscDescSegEnabled();
    }

    /**
     * Gets the LiveData indicating whether drawing ascent/descent segments is enabled.
     * UI components should observe this for state changes.
     *
     * @return {@link LiveData} containing the boolean state.
     */
    public LiveData<Boolean> getIsDrawAscDescSegEnabledLiveData() {
        return drawAscDescSegEnabledLiveData;
    }

    /**
     * Gets the LiveData indicating whether drawing value-dependent icons is enabled.
     * UI components should observe this for state changes.
     *
     * @return {@link LiveData} containing the boolean state.
     */
    public LiveData<Boolean> getIsDrawIconsEnabledLiveData() {
        return drawIconsEnabledLiveData;
    }

    /**
     * Sets the view mode for this chart area.
     * Updates the {@link #viewModeLiveData}.
     *
     * @param viewMode The new {@link GpxViewMode} to set. Must not be null.
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
     * Sets whether to draw X-axis labels on the chart via the controller.
     *
     * @param drawX true to show X-axis labels, false to hide them.
     */
    public void setDrawX(boolean drawX) {
        Log.d(ChartAreaItem.class.getSimpleName(), "Setting drawX: " + drawX);
        chartController.setDrawXLabels(drawX);
    }

    /**
     * Updates the chart with new processed data.
     * Delegates the update call to the {@link ChartController#updateChartData(RawDataProcessed)}.
     *
     * @param rawDataProcessed The processed data to display on the chart.
     * @return A {@link Single} that emits the {@link RequestStatus} of the update operation (e.g., CHART_UPDATED, ERROR).
     *         Returns {@code Single.just(RequestStatus.ERROR)} if {@code rawDataProcessed} is null.
     */
    public Single<RequestStatus> updateChart(RawDataProcessed rawDataProcessed) {
        if (rawDataProcessed == null) {
            Log.w(ChartAreaItem.class.getSimpleName(), "Cannot update chart - data wrapper is null");
            return Single.just(RequestStatus.ERROR);
        }

         return chartController.updateChartData(rawDataProcessed);
    }

    /**
     * Sets the {@link ChartSlot} associated with this chart item.
     * This identifier is used to route events related to this specific chart.
     *
     * @param chartSlot The {@link ChartSlot} identifier.
     */
    public void setChartSlot(ChartSlot chartSlot) {
        this.chartSlot = chartSlot;
    }

    /**
     * Gets the {@link ChartSlot} associated with this chart item.
     *
     * @return The {@link ChartSlot} identifier.
     */
    public ChartSlot getChartSlot() {
        return chartSlot;
    }

    /**
     * Sets the enabled state for drawing ascent/descent segments.
     * Updates the corresponding LiveData {@link #drawAscDescSegEnabledLiveData}.
     *
     * @param isChecked {@code true} to enable, {@code false} to disable.
     */
    public void setDrawAscDescSegEnabled(boolean isChecked) {
        drawAscDescSegEnabledLiveData.setValue(isChecked);
    }

    /**
     * Sets the enabled state for drawing value-dependent icons.
     * Updates the corresponding LiveData {@link #drawIconsEnabledLiveData}.
     *
     * @param isChecked {@code true} to enable, {@code false} to disable.
     */
    public void setDrawIconsEnabled(boolean isChecked) {
        drawIconsEnabledLiveData.setValue(isChecked);
    }
}