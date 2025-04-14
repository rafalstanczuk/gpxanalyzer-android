package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.data.provider.GpxDataEntityCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Singleton
public class MultipleSyncedGpxChartUseCase {
    private static final String TAG = MultipleSyncedGpxChartUseCase.class.getSimpleName();

    @Inject
    GpxDataEntityCachedProvider dataEntityCachedProvider;

    private final LoadChartDataUseCase chartDataLoader;
    private final ChartInitializerUseCase chartInitializer;

    private Disposable loadDataDisposable;

    @Inject
    public MultipleSyncedGpxChartUseCase(LoadChartDataUseCase chartDataLoader, ChartInitializerUseCase chartInitializer) {
        this.chartDataLoader = chartDataLoader;
        this.chartInitializer = chartInitializer;
    }

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
            .subscribe();
    }

    public void disposeAll() {
        Log.d(TAG, "Disposing all subscriptions");
        ConcurrentUtil.tryToDispose(loadDataDisposable);
    }
}

