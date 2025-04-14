package com.itservices.gpxanalyzer.ui.components.chart;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.cache.processed.chart.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartProcessedData;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.provider.ChartProcessedDataProvider;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class ChartProvider {
    private static final String TAG = ChartProvider.class.getSimpleName();

    @Inject
    ChartProcessedDataProvider chartProcessedDataProvider;

    @Inject
    ChartComponents chartComponents;

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
     * Updates the chart with new data.
     * <p>
     * This method updates the data wrapper in the chart, processes the data,
     * and applies it to the chart view. The operation is performed asynchronously
     * using RxJava with computation for data processing and main thread for UI updates.
     * <p>
     * This method should be called when new GPX data is available for visualization.
     *
     * @param rawDataProcessed The data wrapper containing GPX data to visualize
     * @return A Single that emits the status of the update operation
     */
    @UiThread
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
                .observeOn(Schedulers.computation())
                .map(chartProcessedData -> {
                    chartComponents.settings.updateSettingsFor(chartProcessedData.lineData().get());
                    return chartProcessedData;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::updateChart);
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
        return chartComponents.settings;
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
