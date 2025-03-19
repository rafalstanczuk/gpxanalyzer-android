package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.chart.RequestStatus.CHART_UPDATING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.LOADING;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.data.provider.DataEntityCachedProvider;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.provider.DataEntityWrapperCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class MultipleSyncedGpxChartUseCase {

    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();
    @Inject
    GpxViewModeMapper viewModeMapper;

    @Inject
    DataEntityWrapperCachedProvider dataEntityWrapperCachedProvider;

    @Inject
    DataEntityCachedProvider dataEntityCachedProvider;

    private Disposable loadDataDisposable;

    private CompositeDisposable observeSelectionCompositeDisposable = new CompositeDisposable();

    @Inject
    public MultipleSyncedGpxChartUseCase() {
    }

    public void initObserveSelectionOnNeighborChart(List<ChartAreaItem> list) {
        Log.d(MultipleSyncedGpxChartUseCase.class.getSimpleName(), "initObservableSelection() called with: list = [" + list + "]");

        ConcurrentUtil.tryToDispose(observeSelectionCompositeDisposable);
        observeSelectionCompositeDisposable = new CompositeDisposable();
        list.forEach(first ->
                list.forEach(second -> {
                    if (first != second) {
                        observeSelectionCompositeDisposable.add(
                                observeSelectionOn(first.getChartController().getSelection(), second.getChartController())
                        );
                    }
                })
        );
    }

    public void loadData(List<ChartAreaItem> chartAreaItemList) {
        if (chartAreaItemList.isEmpty())
            return;

        requestStatus.onNext(LOADING);

        ConcurrentUtil.tryToDispose(loadDataDisposable);
        loadDataDisposable = dataEntityCachedProvider.provide()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .doOnNext(data -> requestStatus.onNext(DATA_LOADED))
                .flatMap(data ->
                        Observable.fromIterable(chartAreaItemList)
                                .flatMap(chartAreaItem -> initChartItemWithDataWrapper(chartAreaItem, data))
                )
                .doOnNext(chartAreaItem -> requestStatus.onNext(CHART_UPDATING))
                .flatMap(ChartAreaItem::updateChart)
                .doOnNext(requestStatus::onNext)
                .toList()
                .toObservable()
                .doOnError(Throwable::printStackTrace)
                .subscribe(
                        requestStatusList ->
                                requestStatusList.stream()
                                        .min(Comparator.comparingInt(Enum::ordinal))
                                        .ifPresent(status -> {
                                            if (status == RequestStatus.CHART_UPDATED) {
                                                requestStatus.onNext(RequestStatus.DONE);
                                            } else {
                                                requestStatus.onNext(status);
                                            }
                                        }),

                        onError -> Log.e("loadData", "loadData: onError ", onError)
                );
    }

    @NonNull
    private Observable<ChartAreaItem> initChartItemWithDataWrapper(ChartAreaItem chartAreaItem, Vector<DataEntity> data) {
        return chartAreaItem.getChartController()
                .initChart()
                .map(status -> {
                    GpxViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

                    int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode);

                    DataEntityWrapper dataEntityWrapper = dataEntityWrapperCachedProvider.provide(data, (short) primaryKeyIndex);

                    chartAreaItem.setDataEntityWrapper(dataEntityWrapper);

                    return chartAreaItem;
                });
    }

    private Disposable observeSelectionOn(Observable<BaseEntry> selection, ChartController chartController) {
        return selection
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(baseEntry ->
                        chartController.select(baseEntry.getDataEntity().timestampMillis())
                )
                .subscribe();
    }

    public void disposeAll() {
        ConcurrentUtil.tryToDispose(loadDataDisposable);
        ConcurrentUtil.tryToDispose(observeSelectionCompositeDisposable);
    }

    public Observable<Integer> getPercentageProgress() {
        return dataEntityCachedProvider.getPercentageProgress();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }
}
