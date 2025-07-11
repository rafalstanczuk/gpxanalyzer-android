package com.itservices.gpxanalyzer.core.ui.components.chart;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.itservices.gpxanalyzer.core.events.RequestStatus;
import com.itservices.gpxanalyzer.core.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.core.data.cache.processed.chart.EntryCacheMap;
import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartProcessedData;
import com.itservices.gpxanalyzer.core.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.feature.gpxchart.data.provider.ChartProcessedDataProvider;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides and manages data for a specific {@link DataEntityLineChart} instance.
 * Acts as a mediator between the chart view and the data processing layer ({@link ChartProcessedDataProvider}).
 * It handles chart registration, initialization, data updates, and access to chart settings and components.
 * Uses a {@link WeakReference} to the chart to avoid memory leaks.
 */
class ChartProvider {
    private static final String TAG = ChartProvider.class.getSimpleName();

    /** Provider for accessing/creating processed chart data. Injected by Hilt. */
    @Inject
    ChartProcessedDataProvider chartProcessedDataProvider;

    /** Holder for shared chart components like settings and palettes. Injected by Hilt. */
    @Inject
    ChartComponents chartComponents;

    /** Weak reference to the chart view managed by this provider. */
    private WeakReference<DataEntityLineChart> chartWeakReference;

    /**
     * Creates a new ChartProvider instance.
     * Constructor used by Dagger/Hilt for dependency injection.
     */
    @Inject
    public ChartProvider() {
    }

    /**
     * Registers a {@link DataEntityLineChart} view with this provider.
     * Stores a weak reference to the chart and initiates its initialization via {@link #initChart()}.
     * Clears any previously held weak reference.
     *
     * @param chart The {@link DataEntityLineChart} view to register.
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
     * Initializes the registered chart view.
     * Clears any previous highlighting, initializes the chart structure via {@link DataEntityLineChart#initChart(ChartComponents)},
     * and potentially triggers an initial data update using {@link #updateDataChart()} if processed data is already available.
     *
     * @return A {@link Single} emitting the {@link RequestStatus} of the initialization (e.g., CHART_INITIALIZED, CHART_UPDATED, error statuses).
     */
    public Single<RequestStatus> initChart() {
        Log.d(ChartProvider.class.getSimpleName(), "initChart chartWeakReference = [" + chartWeakReference + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        chartWeakReference.get().clearHighlighted();

        return chartWeakReference.get().initChart(chartComponents)
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
     * Updates the registered chart with new raw processed data.
     * This involves:
     * <ol>
     *     <li>Initializing chart components based on the raw data wrapper.</li>
     *     <li>Providing/processing the raw data into chart-specific format via {@link ChartProcessedDataProvider}.</li>
     *     <li>Applying the processed data to the chart view via {@link #updateChart(ChartProcessedData)}.</li>
     * </ol>
     * Operations are scheduled on appropriate RxJava Schedulers.
     *
     * @param rawDataProcessed The raw processed data (containing wrapper and entities) to visualize.
     * @return A {@link Single} emitting the {@link RequestStatus} of the update operation (e.g., CHART_UPDATED, error statuses).
     */
    public Single<RequestStatus> updateChartData(RawDataProcessed rawDataProcessed) {
        Log.d(ChartProvider.class.getSimpleName(), "updateChartData() called with: rawDataProcessed ");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return
                Single.just(chartComponents)
                        .map(components -> {
                            components.init(rawDataProcessed.dataEntityWrapperAtomic().get());
                            return components.getPaletteColorDeterminer();
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .flatMap(palette -> chartProcessedDataProvider
                                .provide(rawDataProcessed, chartComponents.settings, palette))
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::updateChart)
                        .observeOn(Schedulers.io());
    }

    /**
     * Refreshes the chart view using the currently available processed data.
     * This is typically used after settings changes that affect chart appearance but not the data itself.
     * It retrieves the existing {@link ChartProcessedData}, updates settings based on it, and applies it to the chart.
     *
     * @return A {@link Single} emitting the {@link RequestStatus} of the update operation (e.g., CHART_UPDATED, error statuses).
     */
    public Single<RequestStatus> updateDataChart() {
        return Single.just(chartProcessedDataProvider.provide())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(chartProcessedData -> {
                    chartComponents.settings.updateSettingsFor(chartProcessedData.lineData().get());
                    return chartProcessedData;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::updateChart);
    }

    /**
     * Gets the {@link DataEntityLineChart} currently registered with this provider.
     *
     * @return The {@link DataEntityLineChart} instance, or null if none is registered or the reference has been cleared.
     */
    @Nullable
    public DataEntityLineChart getChart() {
        if (chartWeakReference != null)
            return chartWeakReference.get();
        else
            return null;
    }

    /**
     * Gets the shared {@link LineChartSettings} instance associated with the chart.
     *
     * @return The {@link LineChartSettings} instance.
     */
    public synchronized LineChartSettings getSettings() {
        return chartComponents.settings;
    }

    /**
     * Gets the {@link EntryCacheMap} associated with the current chart data, if available.
     * The cache allows efficient lookup of chart entries.
     *
     * @return The {@link EntryCacheMap}, or null if no processed data is currently available.
     */
    @Nullable
    public EntryCacheMap getEntryCacheMap() {
        if (chartProcessedDataProvider.provide() != null) {
            return chartProcessedDataProvider.provide().entryCacheMapAtomic().get();
        }

        return null;
    }

    /**
     * Applies the fully processed chart data ({@link ChartProcessedData}) to the registered chart view.
     * Sets the data on the chart, applies settings, and invalidates the chart to trigger a redraw.
     * This operation is synchronized on the chart object.
     *
     * @param chartProcessedData The processed data (LineData, cache map) to display.
     * @return A {@link Single} emitting the final {@link RequestStatus} (typically CHART_UPDATED or an error status).
     */
    private Single<RequestStatus> updateChart(ChartProcessedData chartProcessedData) {
        return Single.fromCallable(() -> {

            if (chartWeakReference == null)
                return RequestStatus.CHART_WEAK_REFERENCE_IS_NULL;

            DataEntityLineChart chart = chartWeakReference.get();

            if (chart == null)
                return RequestStatus.CHART_IS_NULL;

            synchronized (chart) {
                //chart.clearHighlighted();

                chart.setData(chartProcessedData.lineData().get());
                chartComponents.loadChartSettings(chart);

                chart.invalidate();

                Log.i(TAG, "updateChart: INVALIDATED!!!");
            }
            return RequestStatus.CHART_UPDATED;
        });
    }
}
