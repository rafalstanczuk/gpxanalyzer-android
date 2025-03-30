package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.chart.RequestStatus.CHART_UPDATING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSING;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.data.provider.DataEntityCachedProvider;
import com.itservices.gpxanalyzer.data.provider.RawDataProcessedProvider;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Responsible for loading GPX data and initializing chart components.
 * This class manages the data loading process for all chart elements in the application,
 * handling the full data loading pipeline from data retrieval to chart initialization and updating.
 * It publishes status updates during the loading process.
 */
public class ChartDataLoader {
    private static final String TAG = "ChartDataLoader";
    @Inject
    public GpxViewModeMapper viewModeMapper;
    @Inject
    public DataEntityCachedProvider dataEntityCachedProvider;
    @Inject
    DataEntityCache dataEntityCache;
    @Inject
    RawDataProcessedProvider rawDataProcessedProvider;
    private PublishSubject<RequestStatus> requestStatusPublishSubject;

    /**
     * Creates a new ChartDataLoader instance.
     * Uses Dagger for dependency injection.
     */
    @Inject
    ChartDataLoader() {
    }

    /**
     * Sets the PublishSubject for reporting data loading status updates.
     *
     * @param requestStatus The PublishSubject to publish status updates to
     */
    public void setRequestStatusPublish(PublishSubject<RequestStatus> requestStatus) {
        requestStatusPublishSubject = requestStatus;
    }

    /**
     * Loads data for multiple chart items.
     * This method orchestrates the data loading, chart initialization, and update process:
     * 1. Loads data from the cached provider
     * 2. Initializes each chart item with the loaded data
     * 3. Updates each chart to display the data
     * 4. Reports progress and status through the PublishSubject
     *
     * @param chartAreaItemList List of chart items to load data for
     * @param chartInitializer  Initializer component to prepare charts with data
     * @return Observable emitting the final status of the loading process
     */
    public Observable<RequestStatus> loadData(List<ChartAreaItem> chartAreaItemList, ChartInitializer chartInitializer) {
        if (chartAreaItemList == null || chartAreaItemList.isEmpty()) {
            Log.w(TAG, "Cannot load data - chart list is null or empty");
            return Observable.just(RequestStatus.ERROR);
        }

        Log.d(TAG, "Starting data loading for " + chartAreaItemList.size() + " charts");
        requestStatusPublishSubject.onNext(LOADING);

        return dataEntityCachedProvider.provide()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnSuccess(data -> {
                    Log.d(TAG, "Data loaded successfully");
                    requestStatusPublishSubject.onNext(DATA_LOADED);
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error loading data", throwable);
                    requestStatusPublishSubject.onNext(RequestStatus.ERROR);
                })
                .flatMapObservable(data ->
                        Observable.fromIterable(chartAreaItemList)
                                .flatMapSingle(chartAreaItem -> chartInitializer.initChart(chartAreaItem)
                                        .subscribeOn(Schedulers.computation())
                                        .doOnError(e -> Log.e(TAG, "Error initializing chart item", e))
                                        .doOnSuccess(item -> {
                                            Log.d(TAG, "Chart item initialized successfully");
                                            requestStatusPublishSubject.onNext(PROCESSING);
                                        }))
                                .flatMapSingle(chartAreaItem ->
                                        rawDataProcessedProvider.provide(
                                                        createWrapperFor(chartAreaItem.getViewMode().getValue())
                                                )
                                                .observeOn(Schedulers.computation())
                                                .doOnError(e -> Log.e(TAG, "Error rawDataProcessedProvider", e))
                                                .doOnSuccess(rawDataProcessed -> {
                                                    Log.d(TAG, "Chart item initialized successfully");
                                                    requestStatusPublishSubject.onNext(PROCESSED);
                                                    requestStatusPublishSubject.onNext(CHART_UPDATING);
                                                })
                                                .map(chartAreaItem::updateChart
                                                ))
                                .flatMapSingle(requestStatusSingle -> requestStatusSingle)
                                .flatMapSingle(requestStatus -> {
                                    Log.d(TAG, "Updating chart with rawDataProcessed requestStatus: " + requestStatus);
                                    return Single.just(requestStatus);
                                }))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(requestStatusList -> {
                    RequestStatus finalStatus = requestStatusList.stream()
                            .min(Comparator.comparingInt(Enum::ordinal))
                            .orElse(RequestStatus.ERROR);

                    Log.d(TAG, "Data loading completed with status: " + finalStatus);
                    requestStatusPublishSubject.onNext(finalStatus == RequestStatus.CHART_UPDATED ?
                            RequestStatus.DONE : finalStatus);
                    return Observable.just(finalStatus == RequestStatus.CHART_UPDATED ?
                            RequestStatus.DONE : finalStatus);
                })
                .doOnError(error -> {
                    Log.e(TAG, "Fatal error in data loading chain", error);
                    requestStatusPublishSubject.onNext(RequestStatus.ERROR);
                })
                .doOnComplete(() -> {
                    Log.d(TAG, "Data loading chain completed");
                    requestStatusPublishSubject.onNext(RequestStatus.DONE);
                });
    }

    private DataEntityWrapper createWrapperFor(GpxViewMode viewMode) {
        int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(viewMode);

        return new DataEntityWrapper(primaryKeyIndex, dataEntityCache);
    }

    /*                .flatMap(status -> {
            GpxViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

            if (iChartViewMode == null) {
                Log.w("MultipleSyncedGpxChartUseCase", "ViewMode is null for chart item");
                return chartAreaItem;
            }

            int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode);

            DataEntityWrapper dataEntityWrapper = new DataEntityWrapper(primaryKeyIndex, dataEntityCache);

            return rawDataProcessedProvider.provide(dataEntityWrapper);
        });*/
    @NonNull
    private Vector<DataEntityWrapper> createDataEntityWrappers() {
        int n = dataEntityCache.getDataEntitityVector().firstElement().getMeasures().size();
        Vector<DataEntityWrapper> wrappersVector = new Vector<>(n);

        for (int i = 0; i < n; i++) {
            wrappersVector.add(new DataEntityWrapper(i, dataEntityCache));
        }
        return wrappersVector;
    }
}
