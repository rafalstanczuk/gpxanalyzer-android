package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.cache.MultipleChartsGlobalCache;
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

@Singleton
public class MultipleSyncedGpxChartUseCase {
    private static final String TAG = "MultipleSyncedGpxChart";
    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();
    
    @Inject
    DataEntityCachedProvider dataEntityCachedProvider;

    @Inject
    MultipleChartsGlobalCache multipleChartsGlobalCache;

    private final ChartDataLoader chartDataLoader;
    private final ChartInitializer chartInitializer;
    private final SelectionObserver selectionObserver;

    private Disposable loadDataDisposable;
    private List<ChartAreaItem> currentCharts;

    @Inject
    public MultipleSyncedGpxChartUseCase(ChartDataLoader chartDataLoader, ChartInitializer chartInitializer, SelectionObserver selectionObserver) {
        this.chartDataLoader = chartDataLoader;
        this.chartInitializer = chartInitializer;
        this.selectionObserver = selectionObserver;
        chartDataLoader.setRequestStatusPublish(requestStatus);
    }

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
        
        // Initialize global cache
        multipleChartsGlobalCache.init(list);
        Log.d(TAG, "Selection observation initialized successfully");
    }

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

    public void disposeAll() {
        Log.d(TAG, "Disposing all subscriptions");
        ConcurrentUtil.tryToDispose(loadDataDisposable);
        selectionObserver.dispose();
        currentCharts = null;
    }

    public Observable<Integer> getPercentageProgress() {
        return dataEntityCachedProvider.getPercentageProgress();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }
}

