package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.provider.DataEntityCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Orchestrates the management of multiple synchronized GPX charts.
 * This use case is responsible for coordinating data loading, chart initialization,
 * and synchronization between multiple chart components in the application.
 * It manages the lifecycle of chart-related operations and provides status updates.
 *
 * The class is a singleton to ensure consistent management of chart data and synchronization
 * across the application.
 */
@Singleton
public class MultipleSyncedGpxChartUseCase {
    private static final String TAG = "MultipleSyncedGpxChart";
    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();
    
    @Inject
    DataEntityCachedProvider dataEntityCachedProvider;

    private final ChartDataLoader chartDataLoader;
    private final ChartInitializer chartInitializer;
    private final SelectionObserver selectionObserver;

    private Disposable loadDataDisposable;
    private List<ChartAreaItem> currentCharts;

    /**
     * Creates a new MultipleSyncedGpxChartUseCase with required dependencies.
     *
     * @param chartDataLoader The data loader for chart data
     * @param chartInitializer The initializer for chart components
     * @param selectionObserver The observer for chart selection synchronization
     */
    @Inject
    public MultipleSyncedGpxChartUseCase(ChartDataLoader chartDataLoader, ChartInitializer chartInitializer, SelectionObserver selectionObserver) {
        this.chartDataLoader = chartDataLoader;
        this.chartInitializer = chartInitializer;
        this.selectionObserver = selectionObserver;
        chartDataLoader.setRequestStatusPublish(requestStatus);
    }

    /**
     * Initializes selection observation between charts.
     * This method sets up synchronization between all charts in the list, ensuring
     * that when a selection is made in one chart, it is reflected in all others.
     *
     * @param list The list of chart items to synchronize
     */
    public void initObserveSelectionOnNeighborChart(List<ChartAreaItem> list) {
        if (list == null || list.isEmpty()) {
            Log.w(TAG, "Cannot initialize selection observation - chart list is null or empty");
            return;
        }

        Log.d(TAG, "Initializing selection observation for " + list.size() + " charts");
        
        // Store current charts for later use
        this.currentCharts = list;
        
        // Initialize chart synchronization
        selectionObserver.initChartSync(list);
        
        Log.d(TAG, "Selection observation initialized successfully");
    }

    /**
     * Loads data for multiple chart items.
     * This method orchestrates the entire data loading process, including
     * notifying the selection observer, disposing of previous loading operations,
     * and initiating new data loading.
     *
     * @param chartAreaItemList The list of chart items to load data for
     */
    public void loadData(List<ChartAreaItem> chartAreaItemList) {
        if (chartAreaItemList == null || chartAreaItemList.isEmpty()) {
            Log.w(TAG, "Cannot load data - chart list is null or empty");
            return;
        }

        Log.d(TAG, "Loading data for " + chartAreaItemList.size() + " charts");
        
        // Notify selection observer about new file
        selectionObserver.onFileLoaded();
        
        // Dispose existing data loading subscription
        ConcurrentUtil.tryToDispose(loadDataDisposable);
        
        // Load new data
        loadDataDisposable = chartDataLoader.loadData(chartAreaItemList, chartInitializer)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(() -> {
                Log.d(TAG, "Data loading completed successfully");
                // Reinitialize chart sync after data is loaded
                if (currentCharts != null) {
                    initObserveSelectionOnNeighborChart(currentCharts);
                }
            })
            .doOnError(throwable -> Log.e(TAG, "Error loading data", throwable))
            .subscribe();
    }

    /**
     * Disposes all resources held by this use case.
     * This method should be called when the use case is no longer needed to prevent memory leaks.
     */
    public void disposeAll() {
        Log.d(TAG, "Disposing all subscriptions");
        ConcurrentUtil.tryToDispose(loadDataDisposable);
        selectionObserver.dispose();
        currentCharts = null;
    }

    /**
     * Returns an Observable that emits percentage progress updates during data loading.
     *
     * @return Observable emitting integer percentage values
     */
    public Observable<Integer> getPercentageProgress() {
        return dataEntityCachedProvider.getPercentageProgress();
    }

    /**
     * Returns an Observable that emits status updates during the data loading process.
     *
     * @return Observable emitting RequestStatus values
     */
    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }
}

