package com.itservices.gpxanalyzer.ui.components.chart;

import android.animation.Animator;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.ui.components.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.ui.components.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cache.processed.chart.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.events.EventEntrySelection;
import com.itservices.gpxanalyzer.events.EventVisibleChartEntriesTimestamp;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Controller responsible for managing the interactions, data flow, and overall behavior
 * of a {@link DataEntityLineChart} instance.
 * It acts as the primary interface for interacting with a chart component.
 *
 * Key responsibilities:
 * - Binding to a specific {@link DataEntityLineChart} instance.
 * - Providing access to the underlying {@link ChartProvider} for data and settings.
 * - Handling user gestures on the chart (tap, scale, translate) via {@link OnChartGestureListener} and {@link OnChartValueSelectedListener}.
 * - Managing value selection and highlighting on the chart.
 * - Synchronizing chart state (selection, visible range) with other components via {@link GlobalEventWrapper}.
 * - Toggling chart features like drawing icons or ascent/descent segments.
 * - Initiating chart animations (zoom, fit screen).
 * - Managing RxJava subscriptions for event observation.
 */
public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener, Animator.AnimatorListener {
    private static final String TAG = ChartController.class.getSimpleName();

    /** Global event bus for publishing/subscribing to application-wide events (e.g., selection sync). */
    @Inject
    GlobalEventWrapper mapChartGlobalEventWrapper;
    /**
     * The chart provider that manages the actual chart instance and its data.
     * Injected by Hilt.
     */
    @Inject
    ChartProvider chartProvider;
    /** Manages RxJava subscriptions for this controller. */
    private CompositeDisposable compositeDisposable;

    /**
     * Creates a new ChartController instance.
     * Constructor used by Dagger/Hilt for dependency injection.
     */
    @Inject
    public ChartController() {
    }

    /**
     * Binds this controller to a specific {@link DataEntityLineChart} view.
     * Sets up chart listeners (value selection, gestures) and registers the chart with the {@link ChartProvider}.
     * Initializes RxJava subscriptions for observing external events (e.g., selections from other charts).
     *
     * @param chartBindings The {@link DataEntityLineChart} view to bind to this controller.
     */
    @UiThread
    public void bindChart(@NonNull DataEntityLineChart chartBindings) {
        Log.d(ChartController.class.getSimpleName(), "bindChart() called with: chartBindings = [" + chartBindings + "]");

        chartBindings.setOnChartValueSelectedListener(this);
        chartBindings.setOnChartGestureListener(this);

        chartProvider.registerBinding(chartBindings);

        ConcurrentUtil.tryToDispose(compositeDisposable);
        compositeDisposable = new CompositeDisposable();

        setupSelectionObserve();
    }

    /**
     * Initializes the chart via the {@link ChartProvider}.
     * Ensures the chart has a basic structure and default settings applied before data is loaded.
     *
     * @return A {@link Single} that emits the {@link RequestStatus} of the initialization operation.
     */
    public Single<RequestStatus> initChart() {
        return chartProvider.initChart();
    }

    /**
     * Checks if drawing value-dependent icons (e.g., colored circles) on the chart is enabled.
     * Checks the state from the underlying chart data if available, otherwise falls back to settings.
     *
     * @return {@code true} if icons are enabled, {@code false} otherwise.
     */
    @UiThread
    public boolean isDrawIconsEnabled() {
        if (chartProvider.getChart() != null) {
            LineData lineData = chartProvider.getChart().getData();
            if (lineData != null && !lineData.getDataSets().isEmpty()) {
                return lineData.getDataSets().get(0).isDrawIconsEnabled();
            }
        }
        return chartProvider.getSettings().isDrawIconsEnabled();
    }

    /**
     * Enables or disables the drawing of value-dependent icons on the chart.
     * Updates the setting in the {@link ChartProvider} and triggers a chart refresh.
     *
     * @param isChecked {@code true} to enable icons, {@code false} to disable them.
     */
    @UiThread
    public void setDrawIconsEnabled(boolean isChecked) {
        chartProvider.getSettings().setDrawIconsEnabled(isChecked);

        chartProvider.updateDataChart().subscribe();
    }

    /**
     * Checks if drawing filled ascent/descent segments below the chart line is enabled.
     *
     * @return {@code true} if ascent/descent segments are enabled, {@code false} otherwise.
     */
    public boolean isDrawAscDescSegEnabled() {
        return chartProvider.getSettings().isDrawAscDescSegEnabled();
    }

    /**
     * Enables or disables the drawing of filled ascent/descent segments below the chart line.
     * Updates the setting in the {@link ChartProvider} and triggers a chart refresh.
     *
     * @param isChecked {@code true} to enable segments, {@code false} to disable them.
     */
    @UiThread
    public void setDrawAscDescSegEnabled(boolean isChecked) {
        chartProvider.getSettings().setDrawAscDescSegEnabled(isChecked);

        chartProvider.updateDataChart().subscribe();
    }

    /**
     * Initiates an animated zoom and centers the view on the currently highlighted entry.
     *
     * @param targetScaleX The target scale factor for the X-axis.
     * @param targetScaleY The target scale factor for the Y-axis.
     * @param duration     The animation duration in milliseconds.
     */
    public void animateZoomAndCenterToHighlighted(final float targetScaleX, final float targetScaleY, long duration) {
        if (chartProvider.getChart() != null) {
            chartProvider.getChart().zoomAndCenterToHighlightedAnimated(targetScaleX, targetScaleY, duration, this);
        }
    }

    /**
     * Initiates an animation to adjust the chart's viewport to fit all data.
     *
     * @param duration The animation duration in milliseconds.
     */
    public void animateFitScreen(long duration) {
        if (chartProvider.getChart() != null) {
            chartProvider.getChart().animateFitScreen(duration, this);
        }
    }

    /**
     * Sets whether the X-axis labels should be drawn.
     * Updates the setting in the {@link ChartProvider}.
     *
     * @param drawX {@code true} to show X-axis labels, {@code false} to hide them.
     */
    public void setDrawXLabels(boolean drawX) {
        chartProvider.getSettings().setDrawXLabels(drawX);
    }

    /**
     * Updates the chart with new processed data via the {@link ChartProvider}.
     *
     * @param rawDataProcessed The new data to display.
     * @return A {@link Single} that emits the {@link RequestStatus} of the update operation.
     */
    public Single<RequestStatus> updateChartData(RawDataProcessed rawDataProcessed) {
        return chartProvider.updateChartData(rawDataProcessed);
    }

    /**
     * Programmatically selects a data point on the chart corresponding to the given timestamp.
     * Highlights the entry and optionally centers the view on it.
     *
     * @param selectedTimeMillis The timestamp (in milliseconds) of the entry to select.
     */
    public void select(long selectedTimeMillis) {
        manualSelectEntryOnSelectedTime(Objects.requireNonNull(chartProvider.getChart()), selectedTimeMillis, true, false);
    }

    /**
     * Sets up the RxJava observer to listen for {@link EventEntrySelection} events from the global event bus,
     * allowing this chart to react to selections made on other charts.
     */
    private void setupSelectionObserve() {
        Log.d(TAG, "setupSelectionObserve() called");

        compositeDisposable.add(
                mapChartGlobalEventWrapper.getEventEntrySelection()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(this::handleEvent)
                        .doOnError(throwable -> Log.e(TAG, "Error in chart sync", throwable))
                        .subscribe()
        );
    }

    /**
     * Handles incoming {@link EventEntrySelection} events from other components.
     * If the event is not from this chart, it selects the corresponding entry locally.
     *
     * @param event The selection event.
     */
    private void handleEvent(EventEntrySelection event) {
        if (event == null) {
            return;
        }

        if (chartProvider.getChart() == null) {
            Log.w(TAG, "Chart is null in handleEvent selection");
            return;
        }

        if (event.chartSlot() == Objects.requireNonNull(chartProvider.getChart()).getChartSlot()) {
            return;
        }

        CurveEntry curveEntry = event.curveEntry();

        if (curveEntry == null) {
            Log.w(TAG, "Received null curveEntry in handleEvent selection");
            return;
        }

        if (curveEntry.getDataEntity() == null) {
            Log.w(TAG, "Received null DataEntity in handleEvent curveEntry");
            return;
        }

        long timestamp = curveEntry.getDataEntity().timestampMillis();

        try {
            select(timestamp);
        } catch (Exception e) {
            Log.e(TAG, String.format("Error selecting timestamp %d",
                    timestamp), e);
        }
    }

    /**
     * Manually selects an entry on the chart based on a timestamp.
     * Finds the closest entry, highlights it, optionally centers the view, and optionally notifies listeners.
     *
     * @param chart                 The chart instance.
     * @param selectedTimeMillis    The target timestamp.
     * @param centerViewToSelection If true, center the chart view on the selected entry.
     * @param callListeners         If true, trigger the {@link OnChartValueSelectedListener#onValueSelected(Entry, Highlight)} callback.
     */
    private void manualSelectEntryOnSelectedTime(DataEntityLineChart chart, long selectedTimeMillis, boolean centerViewToSelection, boolean callListeners) {
        if (chart == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart is null during selection");
            return;
        }

        chart.getChartTouchListener().setLastGesture(ChartTouchListener.ChartGesture.NONE);

        if (selectedTimeMillis < 0) {
            chart.highlightValue(null, false);
            chart.invalidate();
            return;
        }
        if (chart.getData() == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart data is null during selection");
            return;
        }

        EntryCacheMap entryCacheMap = chartProvider.getEntryCacheMap();
        if (entryCacheMap == null) {
            Log.w(ChartController.class.getSimpleName(), "Entry cache map is null during selection");
            return;
        }

        BaseEntry entryFound = entryCacheMap.get(selectedTimeMillis);
        if (entryFound != null) {
            //Log.d(ChartController.class.getSimpleName(), "Found entry for timestamp: " + selectedTimeMillis);
            setSelectionEntry(entryFound, callListeners);
            chart.highlightValue(entryFound.getX(), entryFound.getY(), entryFound.getDataSetIndex(), callListeners);

            if (centerViewToSelection) {
                chart.centerViewTo(entryFound.getX(), entryFound.getY(), YAxis.AxisDependency.LEFT);
            }
        } else {
            Log.w(ChartController.class.getSimpleName(), "No entry found for timestamp: " + selectedTimeMillis);
        }
    }

    /**
     * Sets the selected entry internally and optionally publishes the selection globally.
     *
     * @param entry           The entry that was selected.
     * @param publishSelection If true, publish an {@link EventEntrySelection} via the {@link GlobalEventWrapper}.
     */
    private void setSelectionEntry(Entry entry, boolean publishSelection) {
        DataEntityLineChart chart = chartProvider.getChart();
        if (chart == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart is null during selection entry setting");
            return;
        }

        chart.setHighlightedEntry(entry);

        if (publishSelection && (entry instanceof BaseEntry)) {
            //Log.d(ChartController.class.getSimpleName(), "Publishing selection for entry: " + entry);

            publishSelectionGlobal((CurveEntry) entry, chart);
        }
    }

    /**
     * Publishes the selected entry event to the global event bus.
     *
     * @param entry The selected {@link CurveEntry}.
     * @param chart The chart instance where the selection occurred.
     */
    private void publishSelectionGlobal(CurveEntry entry, DataEntityLineChart chart) {
        mapChartGlobalEventWrapper.onNext(
                new EventEntrySelection(chart.getChartSlot(), entry)
        );
    }

    /**
     * Resets the selection marker and clears the highlighted value on the chart.
     *
     * @param chart The chart instance.
     */
    private void resetMarkerAndClearSelection(DataEntityLineChart chart) {
        chart.setHighlightedEntry(null);

        manualSelectEntryOnSelectedTime(chart, -1, false, true);
    }

    // --- OnChartGestureListener Implementation --- //

    /**
     * Called when a gesture starts (e.g., touch start after scaling or dragging).
     * Logs the event.
     */
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    /**
     * Called when a gesture ends (e.g., touch release after scaling or dragging).
     * Publishes the current visible timestamp boundaries via {@link #publishVisibleBoundaryEntriesTimestamps()}.
     */
    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        //Log.d(ChartController.class.getSimpleName(), "onChartGestureEnd() called with: me = [" + me + "], lastPerformedGesture = [" + lastPerformedGesture + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Called when the chart is long-pressed.
     * Logs the event.
     */
    @Override
    public void onChartLongPressed(MotionEvent me) {
    }

    /**
     * Called when the chart is double-tapped.
     * Logs the event.
     */
    @Override
    public void onChartDoubleTapped(MotionEvent me) {
    }

    /**
     * Called when the chart is single-tapped.
     * Logs the event.
     */
    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    /**
     * Called when the chart is flung.
     * Logs the event.
     */
    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float speedX, float speedY) {
    }

    /**
     * Called when the chart is scaled via pinch gesture.
     * Logs the event.
     */
    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        //Log.d(ChartController.class.getSimpleName(), "onChartScale() called with: me = [" + me + "], scaleX = [" + scaleX + "], scaleY = [" + scaleY + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Called when the chart is translated (dragged).
     * Logs the event.
     */
    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        //Log.d(ChartController.class.getSimpleName(), "onChartTranslate() called with: me = [" + me + "], dX = [" + dX + "], dY = [" + dY + "]");

        Objects.requireNonNull(chartProvider.getChart()).highlightCenterValueInTranslation();

        publishVisibleBoundaryEntriesTimestamps();
    }

    // --- OnChartValueSelectedListener Implementation --- //

    /**
     * Called when a value is selected by tapping on the chart.
     * Updates the internal selection state and publishes the selection globally.
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //Log.d(ChartController.class.getSimpleName(), "Value selected: " + e + ", highlight: " + h);
        setSelectionEntry(e, true);

        //publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Publishes the current visible timestamp range of the chart to the {@link GlobalEventWrapper}.
     * This allows other components (like the map) to synchronize with the chart's viewport.
     */
    private void publishVisibleBoundaryEntriesTimestamps() {
        DataEntityLineChart chart = Objects.requireNonNull(chartProvider.getChart());

        mapChartGlobalEventWrapper.onNext(
                new EventVisibleChartEntriesTimestamp(
                        chart.getChartSlot(),
                        chart.getVisibleEntriesBoundaryTimestamps()
                )
        );
    }

    /**
     * Called when nothing is selected anymore (e.g., tapping outside the data).
     * Resets the selection marker and clears highlighting.
     */
    @Override
    public void onNothingSelected() {
        Log.d(ChartController.class.getSimpleName(), "Nothing selected");
        resetMarkerAndClearSelection(Objects.requireNonNull(chartProvider.getChart()));
    }

    /**
     * Gets a string representation of the chart's memory address (for debugging/logging).
     *
     * @return String representing the chart address, or "null" if the chart is not available.
     */
    public String getChartAddress() {
        DataEntityLineChart chart = chartProvider.getChart();

        if (chart != null) {
            return Integer.toHexString(chart.hashCode());
        }

        return null;
    }

    // --- Animator.AnimatorListener Implementation --- //

    /** Called when a chart animation starts. Publishes visible boundaries. */
    @Override
    public void onAnimationStart(@NonNull Animator animation) {

    }

    /** Called when a chart animation ends. Publishes visible boundaries. */
    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        //Log.d(ChartController.class.getSimpleName(), "onAnimationEnd() called with: animation = [" + animation + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    /** Called when a chart animation is cancelled. Publishes visible boundaries. */
    @Override
    public void onAnimationCancel(@NonNull Animator animation) {

    }

    /** Called when a chart animation repeats (typically not used for chart animations). */
    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {

    }
}
