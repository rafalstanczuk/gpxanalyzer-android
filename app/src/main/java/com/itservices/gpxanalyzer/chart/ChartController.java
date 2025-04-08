package com.itservices.gpxanalyzer.chart;

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
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.event.EventEntrySelection;
import com.itservices.gpxanalyzer.event.EventVisibleChartEntriesTimestamp;
import com.itservices.gpxanalyzer.event.MapChartGlobalEventWrapper;
import com.itservices.gpxanalyzer.data.cache.processed.chart.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener, Animator.AnimatorListener {
    private static final String TAG = ChartController.class.getSimpleName();

    @Inject
    MapChartGlobalEventWrapper mapChartGlobalEventWrapper;
    private Disposable disposableSelectionObserveGlobal;
    
    /**
     * The chart provider that manages the actual chart instances and data.
     * Injected by Dagger to promote separation of concerns and testability.
     */
    @Inject
    ChartProvider chartProvider;

    /**
     * Creates a new ChartController instance.
     * <p>
     * Uses Dagger for dependency injection of required components.
     * This constructor is intended to be called by the dependency injection framework,
     * not directly by application code.
     */
    @Inject
    public ChartController() {
    }

    /**
     * Binds this controller to a chart view.
     * <p>
     * Sets up listeners for user interactions with the chart and registers the chart with the provider.
     * This method should be called after the chart view has been created and before it is displayed
     * to the user. It establishes the connection between the controller and the actual chart UI component.
     *
     * @param chartBindings The chart view to bind to this controller
     */
    @UiThread
    public void bindChart(@NonNull DataEntityLineChart chartBindings) {
        Log.d(ChartController.class.getSimpleName(), "bindChart() called with: chartBindings = [" + chartBindings + "]");

        chartBindings.setOnChartValueSelectedListener(this);
        chartBindings.setOnChartGestureListener(this);

        chartProvider.registerBinding(chartBindings);

        setupSelectionObserve();
    }

    /**
     * Initializes the chart with default settings.
     * <p>
     * This should be called before displaying the chart to ensure it has proper visual 
     * configuration. The initialization process sets up the chart's appearance, behavior,
     * and empty data containers.
     *
     * @return A Single that emits the status of the initialization operation
     */
    public Single<RequestStatus> initChart() {
        return chartProvider.initChart();
    }

    /**
     * Checks if drawing icons on the chart is enabled.
     * <p>
     * Icons can be drawn at data points to highlight specific features or properties
     * of the GPX data being displayed.
     *
     * @return true if icons are enabled, false otherwise
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
     * Sets whether to enable drawing icons on the chart.
     * <p>
     * This will update the chart to reflect the change immediately. Icons can provide
     * visual indicators at specific data points on the chart.
     *
     * @param isChecked true to enable icons, false to disable them
     */
    @UiThread
    public void setDrawIconsEnabled(boolean isChecked) {
        chartProvider.getSettings().setDrawIconsEnabled(isChecked);

        chartProvider.updateDataChart().subscribe();
    }

    /**
     * Checks if drawing ascent/descent segments on the chart is enabled.
     * <p>
     * When enabled, areas between the chart line and the axis are filled with color
     * to highlight uphill (ascent) and downhill (descent) sections of the track.
     *
     * @return true if ascent/descent segments are enabled, false otherwise
     */
    public boolean isDrawAscDescSegEnabled() {
        return chartProvider.getSettings().isDrawAscDescSegEnabled();
    }

    /**
     * Sets whether to enable drawing ascent/descent segments on the chart.
     * <p>
     * This will update the chart to reflect the change immediately. These segments
     * provide a visual indication of elevation changes throughout the track.
     *
     * @param isChecked true to enable segments, false to disable them
     */
    @UiThread
    public void setDrawAscDescSegEnabled(boolean isChecked) {
        chartProvider.getSettings().setDrawAscDescSegEnabled(isChecked);

        chartProvider.updateDataChart().subscribe();
    }

    public void animateZoomAndCenterToHighlighted(final float targetScaleX, final float targetScaleY, long duration) {
        Objects.requireNonNull(chartProvider.getChart()).zoomAndCenterToHighlightedAnimated(targetScaleX, targetScaleY, duration, this);
    }

    /**
     * Animates the chart to fit all data on screen.
     * <p>
     * This is useful after loading new data or when resetting the view to show
     * the entire track at once. The animation provides a smooth transition.
     *
     * @param duration The animation duration in milliseconds
     */
    public void animateFitScreen(long duration) {
        Objects.requireNonNull(chartProvider.getChart()).animateFitScreen(duration, this);
    }

    /**
     * Sets whether to draw X-axis labels on the chart.
     * <p>
     * X-axis labels typically show time or distance values along the horizontal axis.
     * This setting can be useful when displaying multiple charts vertically stacked,
     * where only the bottom chart might need X-axis labels.
     *
     * @param drawX true to show X-axis labels, false to hide them
     */
    public void setDrawXLabels(boolean drawX) {
        chartProvider.getSettings().setDrawXLabels(drawX);
    }

    /**
     * Updates the chart with new data.
     * <p>
     * This method should be called whenever new GPX data becomes available or
     * when switching to a different data set. It triggers data processing and
     * visualization updates on the chart.
     *
     * @param rawDataProcessed The data wrapper containing GPX data to visualize
     * @return A Single that emits the status of the update operation
     */
    public Single<RequestStatus> updateChartData(RawDataProcessed rawDataProcessed) {
        return chartProvider.updateChartData(rawDataProcessed);
    }

    /**
     * Programmatically selects a data point at the specified timestamp.
     * <p>
     * This will highlight the corresponding entry on the chart and center the
     * chart view on that point. This is useful for synchronizing multiple charts
     * or responding to external selection events.
     *
     * @param selectedTimeMillis The timestamp (in milliseconds) to select
     */
    public void select(long selectedTimeMillis) {
        manualSelectEntryOnSelectedTime(Objects.requireNonNull(chartProvider.getChart()), selectedTimeMillis, true, false);
    }

    private void setupSelectionObserve() {
        Log.d(TAG, "setupSelectionObserve() called");

        ConcurrentUtil.tryToDispose(disposableSelectionObserveGlobal);

        disposableSelectionObserveGlobal =
                mapChartGlobalEventWrapper.getEventEntrySelection()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(this::handleEvent)
                        .doOnError(throwable -> Log.e(TAG, "Error in chart sync", throwable))
                        .subscribe();
    }

    private void handleEvent(EventEntrySelection event) {
        if (event == null){
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
     * Selects an entry on the chart based on timestamp.
     * <p>
     * This method handles finding the appropriate entry, highlighting it,
     * and optionally centering the view on the selection. It provides more
     * control over the selection behavior than the public select() method.
     *
     * @param chart The chart to select on
     * @param selectedTimeMillis The timestamp to select
     * @param centerViewToSelection Whether to center the chart view on the selection
     * @param callListeners Whether to notify selection listeners
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

        BaseEntry entryFound = (BaseEntry) entryCacheMap.get(selectedTimeMillis);
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
     * Sets the currently selected entry and optionally publishes a selection event.
     * <p>
     * This method updates the internal selection state and, if requested, notifies
     * observers about the new selection. This is used both for user-initiated selections
     * and programmatic selections.
     *
     * @param entry The entry to select
     * @param publishSelection Whether to publish a selection event
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

    private void publishSelectionGlobal(CurveEntry entry, DataEntityLineChart chart) {
        mapChartGlobalEventWrapper.onNext(
                    new EventEntrySelection(chart.getChartSlot(), entry)
        );
    }

    /**
     * Resets the marker and clears the current selection.
     * <p>
     * This method removes any highlighting or selection from the chart,
     * returning it to an unselected state. This is typically called when
     * the user taps in an empty area of the chart.
     *
     * @param chart The chart to reset
     */
    private void resetMarkerAndClearSelection(DataEntityLineChart chart) {
        chart.setHighlightedEntry(null);

        manualSelectEntryOnSelectedTime(chart, -1, false, true);
    }

    /**
     * Called when a chart gesture starts.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior when a gesture begins.
     * 
     * @param me The MotionEvent that triggered the gesture
     * @param lastPerformedGesture The type of gesture that was performed
     */
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    /**
     * Called when a chart gesture ends.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior when a gesture completes.
     * 
     * @param me The MotionEvent that completed the gesture
     * @param lastPerformedGesture The type of gesture that was performed
     */
    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        //Log.d(ChartController.class.getSimpleName(), "onChartGestureEnd() called with: me = [" + me + "], lastPerformedGesture = [" + lastPerformedGesture + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Called when the chart is long-pressed.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior for long-press gestures.
     * 
     * @param me The MotionEvent for the long press
     */
    @Override
    public void onChartLongPressed(MotionEvent me) {
    }

    /**
     * Called when the chart is double-tapped.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior for double-tap gestures.
     * 
     * @param me The MotionEvent for the double tap
     */
    @Override
    public void onChartDoubleTapped(MotionEvent me) {
    }

    /**
     * Called when the chart is single-tapped.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior for single-tap gestures.
     * 
     * @param me The MotionEvent for the single tap
     */
    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    /**
     * Called when the chart is flung.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior for fling gestures.
     * 
     * @param me1 The initial MotionEvent that started the fling
     * @param me2 The final MotionEvent that completed the fling
     * @param speedX The horizontal speed of the fling
     * @param speedY The vertical speed of the fling
     */
    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float speedX, float speedY) {
    }

    /**
     * Called when the chart is scaled.
     * <p>
     * Implementation of OnChartGestureListener.
     * Override this method to add custom behavior for scaling gestures.
     * 
     * @param me The MotionEvent that triggered the scaling
     * @param scaleX The horizontal scale factor
     * @param scaleY The vertical scale factor
     */
    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        //Log.d(ChartController.class.getSimpleName(), "onChartScale() called with: me = [" + me + "], scaleX = [" + scaleX + "], scaleY = [" + scaleY + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Called when the chart is translated (panned).
     * <p>
     * Implementation of OnChartGestureListener.
     * This method highlights the center value during translation to provide
     * visual feedback about the current position in the data.
     * 
     * @param me The MotionEvent that triggered the translation
     * @param dX The distance translated in X direction
     * @param dY The distance translated in Y direction
     */
    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        //Log.d(ChartController.class.getSimpleName(), "onChartTranslate() called with: me = [" + me + "], dX = [" + dX + "], dY = [" + dY + "]");

        Objects.requireNonNull(chartProvider.getChart()).highlightCenterValueInTranslation();

        publishVisibleBoundaryEntriesTimestamps();
    }

    /**
     * Called when a value is selected on the chart.
     * <p>
     * Implementation of OnChartValueSelectedListener.
     * Updates the selection state and publishes a selection event to notify
     * other components about the selection.
     * 
     * @param e The entry that was selected
     * @param h The highlight object representing the selection
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //Log.d(ChartController.class.getSimpleName(), "Value selected: " + e + ", highlight: " + h);
        setSelectionEntry(e, true);

        //publishVisibleBoundaryEntriesTimestamps();
    }

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
     * Called when no value is selected on the chart.
     * <p>
     * Implementation of OnChartValueSelectedListener.
     * Resets the selection state when the user taps in an empty area or
     * otherwise clears the selection.
     */
    @Override
    public void onNothingSelected() {
        Log.d(ChartController.class.getSimpleName(), "Nothing selected");
        resetMarkerAndClearSelection(Objects.requireNonNull(chartProvider.getChart()));
    }

    /**
     * Gets a unique identifier for the chart managed by this controller.
     * <p>
     * This can be used to track and identify charts in multi-chart scenarios,
     * such as when synchronizing selection between multiple charts.
     *
     * @return A string representing the chart's address, or null if no chart is bound
     */
    public String getChartAddress() {
        DataEntityLineChart chart = chartProvider.getChart();

        if (chart != null) {
            return Integer.toHexString(chart.hashCode());
        }

        return null;
    }

    @Override
    public void onAnimationStart(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        //Log.d(ChartController.class.getSimpleName(), "onAnimationEnd() called with: animation = [" + animation + "]");

        publishVisibleBoundaryEntriesTimestamps();
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {

    }
}
