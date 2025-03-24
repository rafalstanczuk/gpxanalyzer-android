package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.chart.RequestStatus.CHART_UPDATING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.LOADING;

import android.util.Log;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.provider.DataEntityCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ChartDataLoader {
    private static final String TAG = "ChartDataLoader";

    @Inject
    public DataEntityCachedProvider dataProvider;
    private PublishSubject<RequestStatus> requestStatusPublishSubject;

    @Inject
    ChartDataLoader() {
    }

    public void setRequestStatusPublish(PublishSubject<RequestStatus> requestStatus) {
        requestStatusPublishSubject = requestStatus;
    }

    public Observable<RequestStatus> loadData(List<ChartAreaItem> chartAreaItemList, ChartInitializer chartInitializer) {
        if (chartAreaItemList == null || chartAreaItemList.isEmpty()) {
            Log.w(TAG, "Cannot load data - chart list is null or empty");
            return Observable.just(RequestStatus.ERROR);
        }

        Log.d(TAG, "Starting data loading for " + chartAreaItemList.size() + " charts");
        requestStatusPublishSubject.onNext(LOADING);

        return dataProvider.provide()
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
            .flatMapObservable(data -> Observable.fromIterable(chartAreaItemList)
                .flatMapSingle(chartAreaItem -> {
                    Log.d(TAG, "Initializing chart item: " + chartAreaItem.getChartController().getChartAddress());
                    return chartInitializer.initChartItemWithDataWrapper(chartAreaItem, data)
                        .subscribeOn(Schedulers.computation())
                        .doOnError(e -> Log.e(TAG, "Error initializing chart item", e))
                        .doOnSuccess(item -> {
                            Log.d(TAG, "Chart item initialized successfully");
                            requestStatusPublishSubject.onNext(CHART_UPDATING);
                        });
                })
                .flatMapSingle(chartAreaItem -> {
                    Log.d(TAG, "Updating chart: " + chartAreaItem.getChartController().getChartAddress());
                    return chartAreaItem.updateChart()
                        .doOnError(e -> Log.e(TAG, "Error updating chart", e));
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
}
