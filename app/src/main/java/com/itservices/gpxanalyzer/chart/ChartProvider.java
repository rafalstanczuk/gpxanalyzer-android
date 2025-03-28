package com.itservices.gpxanalyzer.chart;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.processed.ChartProcessedData;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides chart functionality and manages the lifecycle of chart components.
 * <p>
 * This class serves as the bridge between high-level chart controllers and the
 * actual chart implementation. It maintains a weak reference to the chart view,
 * handles initialization and updating of charts, and manages chart settings and data.
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Initializing charts with settings and empty data</li>
 *   <li>Updating charts with new data from {@link DataEntityWrapper} objects</li>
 *   <li>Managing chart highlights and selections</li>
 *   <li>Handling concurrent updates using RxJava for thread management</li>
 *   <li>Providing access to chart components like settings and entry cache</li>
 * </ul>
 * <p>
 * This class uses weak references to avoid memory leaks when charts are destroyed,
 * and atomic references to ensure thread safety for concurrent operations.
 */
class ChartProvider {
    private static final String TAG = ChartProvider.class.getSimpleName();

    private final AtomicReference<Highlight> currentHighlightRef = new AtomicReference<>();
    @Inject
    LineChartSettings settings;
    @Inject
    ChartProcessedDataProvider chartProcessedDataProvider;
    private WeakReference<DataEntityLineChart> chartWeakReference;

    /**
     * Creates a new ChartProvider instance.
     * <p>
     * Uses Dagger for dependency injection to obtain required components
     * like {@link LineChartSettings} and {@link ChartProcessedDataProvider}.
     */
    @Inject
    public ChartProvider() {
    }

    /**
     * Registers a chart view with this provider.
     * <p>
     * Stores a weak reference to the chart view and initializes it.
     * If a previous chart was registered, it is cleared to avoid memory leaks.
     * After registration, the chart is initialized with empty data and default settings.
     *
     * @param chart The chart view to register, typically a {@link DataEntityLineChart}
     */
    public void registerBinding(DataEntityLineChart chart) {
        if (chart != null) {
            if (chartWeakReference != null)
                chartWeakReference.clear();

            chartWeakReference = new WeakReference<>(chart);

            initChart().subscribe();
            Log.d(ChartProvider.class.getSimpleName(), "chartWeakReference = [" + chartWeakReference + "]");
        }
    }

    /**
     * Initializes the chart with empty data and styling.
     * <p>
     * If processed data is available, the chart will be updated with that data.
     * This method should be called after a chart is registered to ensure proper initialization.
     * The operation is performed asynchronously using RxJava to avoid blocking the UI thread.
     *
     * @return A Single that emits the status of the initialization operation
     */
    public Single<RequestStatus> initChart() {
        Log.d(ChartProvider.class.getSimpleName(), "initChart chartWeakReference = [" + chartWeakReference + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return chartWeakReference.get().initChart(settings)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(req -> {
                    ChartProcessedData chartProcessedData = chartProcessedDataProvider.provide();
                    if (chartProcessedData != null) {
                        //sets.clear();
                        return updateDataChart();

                    }
                    return Single.just(RequestStatus.CHART_INITIALIZED);
                });
    }

    /**
     * Sets the current selection highlight on the chart.
     * <p>
     * This is used to maintain selection state across chart updates.
     * When the chart is redrawn (e.g., after data changes), the highlight
     * can be reapplied to preserve user selection context.
     *
     * @param h The highlight to set, or null to clear the highlight
     */
    public void setSelectionHighlight(Highlight h) {
        currentHighlightRef.set(h);
    }

    /**
     * Updates the chart with new data.
     * <p>
     * This method updates the data wrapper in the chart, processes the data,
     * and applies it to the chart view. The operation is performed asynchronously
     * using RxJava with computation for data processing and main thread for UI updates.
     * <p>
     * This method should be called when new GPX data is available for visualization.
     *
     * @param dataEntityWrapper The data wrapper containing GPX data to visualize
     * @return A Single that emits the status of the update operation
     */
    @UiThread
    public Single<RequestStatus> updateChartData(DataEntityWrapper dataEntityWrapper) {
        Log.d(ChartProvider.class.getSimpleName(), "updateChartData() called with: dataEntityWrapper = [" + dataEntityWrapper + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return
                Single.just(chartWeakReference.get())
                        .map(chart -> {
                            chart.setDataEntityWrapper(dataEntityWrapper);
                            return chart.getPaletteColorDeterminer();
                        })
                        .flatMap(palette -> chartProcessedDataProvider
                                .provide(dataEntityWrapper, settings, palette))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(lineData -> updateChart(lineData, currentHighlightRef.get()))
                        .observeOn(Schedulers.io());
    }

    /**
     * Updates the chart with the currently processed data.
     * <p>
     * This method is used when settings have changed but the underlying data remains the same.
     * It retrieves the current processed data, updates settings based on the data,
     * and applies the data to the chart view.
     * <p>
     * This method is particularly useful for applying visual changes (like chart style or
     * format settings) without needing to reprocess the entire dataset.
     *
     * @return A Single that emits the status of the update operation
     */
    public Single<RequestStatus> updateDataChart() {
        return Single.just(chartProcessedDataProvider.provide())
                .subscribeOn(Schedulers.io())
                .doOnEvent((chartProcessedData, throwable) -> settings.updateSettingsFor(chartProcessedData.lineData().get()))
                .observeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(chartProcessedData -> updateChart(chartProcessedData, currentHighlightRef.get()));
    }

    /**
     * Gets the current chart view.
     * <p>
     * Returns the chart view that was previously registered with this provider.
     * If no chart is registered or if the weak reference has been cleared,
     * this method returns null.
     * 
     * @return The current chart view, or null if no chart is registered
     */
    @Nullable
    public DataEntityLineChart getChart() {
        if (chartWeakReference != null)
            return chartWeakReference.get();
        else
            return null;
    }

    /**
     * Gets the chart settings.
     * <p>
     * Returns the LineChartSettings instance that controls the visual appearance
     * and behavior of the chart. This method is synchronized to ensure thread safety
     * when accessing settings.
     * 
     * @return The LineChartSettings instance
     */
    public synchronized LineChartSettings getSettings() {
        return settings;
    }

    /**
     * Gets the entry cache map for the current chart data.
     * <p>
     * The entry cache map provides fast access to chart entries by timestamp.
     * This is useful for operations that need to lookup or manipulate specific
     * chart entries, such as highlighting points or retrieving data for tooltips.
     * 
     * @return The EntryCacheMap, or null if no chart data is available
     */
    @Nullable
    public EntryCacheMap getEntryCacheMap() {
        if (chartProcessedDataProvider.provide() != null) {
            return chartProcessedDataProvider.provide().entryCacheMapAtomic().get();
        }

        return null;
    }

    /**
     * Updates the chart with processed data and applies a highlight.
     * <p>
     * This method sets the processed data in the chart, applies chart settings,
     * applies the highlight (if provided), and invalidates the chart to trigger a redraw.
     * <p>
     * This is an internal method used by the public update methods to perform the actual
     * chart update on the UI thread.
     * 
     * @param chartProcessedData The processed chart data to display
     * @param highlight The highlight to apply, or null for no highlight
     * @return A Single that emits the status of the update operation
     */
    private Single<RequestStatus> updateChart(ChartProcessedData chartProcessedData,
                                              Highlight highlight) {
        return Single.fromCallable(() -> {

            if (chartWeakReference == null)
                return RequestStatus.CHART_WEAK_REFERENCE_IS_NULL;

            DataEntityLineChart chart = chartWeakReference.get();

            if (chart == null)
                return RequestStatus.CHART_IS_NULL;

            synchronized (chart) {
                //chart.clear();
                chart.setData(chartProcessedData.lineData().get());
                chart.loadChartSettings(settings);
                chart.highlightValue(highlight, true);
                chart.invalidate();

                Log.i(TAG, "updateChart: INVALIDATED!!!");
            }
            return RequestStatus.CHART_UPDATED;
        });
    }

}
