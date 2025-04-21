package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Use case responsible for managing the data loading process for multiple synchronized GPX charts.
 * It orchestrates the loading and initialization of several {@link ChartAreaItem} instances,
 * ensuring that previous loading operations are disposed before starting new ones.
 * This is typically used when a set of charts needs to be loaded or refreshed together.
 */
@Singleton
public class MultipleSyncedGpxChartUseCase {
    private static final String TAG = MultipleSyncedGpxChartUseCase.class.getSimpleName();

    /**
     * Use case for loading data into individual charts.
     */
    private final LoadChartDataUseCase chartDataLoader;
    /**
     * Use case for initializing individual charts.
     */
    private final ChartInitializerUseCase chartInitializer;

    /**
     * Holds the subscription to the current data loading operation, allowing it to be disposed.
     */
    Disposable loadDataDisposable;

    /**
     * Constructs the use case with required dependencies.
     *
     * @param chartDataLoader  Use case for loading chart data.
     * @param chartInitializer Use case for initializing charts.
     */
    @Inject
    public MultipleSyncedGpxChartUseCase(LoadChartDataUseCase chartDataLoader, ChartInitializerUseCase chartInitializer) {
        this.chartDataLoader = chartDataLoader;
        this.chartInitializer = chartInitializer;
    }

    /**
     * Initiates the data loading process for a list of {@link ChartAreaItem}s.
     * If a previous data loading operation is in progress (i.e., {@link #loadDataDisposable} is active),
     * it is disposed before starting the new one.
     * Delegates the actual loading to {@link LoadChartDataUseCase#loadData(List, ChartInitializerUseCase)}.
     * The loading occurs on the IO scheduler, and results/errors are observed on the Android main thread.
     *
     * @param chartAreaItemList The list of chart items to load data for.
     */
    public void loadData(List<ChartAreaItem> chartAreaItemList) {
        if (chartAreaItemList == null || chartAreaItemList.isEmpty()) {
            Log.w(TAG, "Cannot load data - chart list is null or empty");
            return;
        }

        Log.d(TAG, "Loading data for " + chartAreaItemList.size() + " charts");

        // Dispose existing data loading subscription
        ConcurrentUtil.tryToDispose(loadDataDisposable);
        // Load new data
        loadDataDisposable = chartDataLoader.loadData(chartAreaItemList, chartInitializer)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(() -> {
                Log.d(TAG, "Data loading completed successfully");
            })
            .doOnError(throwable -> Log.e(TAG, "Error loading data", throwable))
            .subscribe(
                status -> Log.d(TAG, "Chart data load status: " + status),
                error -> Log.e(TAG, "Subscription error in MultipleSyncedGpxChartUseCase", error)
            );
    }

    /**
     * Disposes any active data loading subscription managed by this use case.
     * This should be called when the associated view or component is destroyed to prevent memory leaks
     * and stop ongoing background operations.
     */
    public void disposeAll() {
        Log.d(TAG, "Disposing all subscriptions");
        ConcurrentUtil.tryToDispose(loadDataDisposable);
    }
}

