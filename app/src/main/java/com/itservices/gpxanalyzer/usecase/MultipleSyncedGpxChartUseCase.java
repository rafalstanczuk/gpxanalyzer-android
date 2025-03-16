package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.data.RequestStatus.CHART_UPDATING;
import static com.itservices.gpxanalyzer.data.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.data.RequestStatus.DONE;
import static com.itservices.gpxanalyzer.data.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.data.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.data.RequestStatus.PROCESSING;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.data.RequestStatus;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.provider.GPXDataProvider;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewModeMapper;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
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

    @Inject
    ViewModeMapper viewModeMapper;

    @Inject
    GPXDataProvider dataProvider;

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();

    private Disposable loadDataDisposable;

    private CompositeDisposable chartUpdateCompositeDisposable = new CompositeDisposable();
    private CompositeDisposable observeSelectionCompositeDisposable = new CompositeDisposable();
    private Vector<DataEntity> gpxData;
    private int defaultRawGpxDataId;

    private WeakReference<Context> contextWeakReference;

    @Inject
    public MultipleSyncedGpxChartUseCase() {
    }

    private void setGpxData(Vector<DataEntity> gpxData) {
        this.gpxData = gpxData;
    }

    public void initChartAreaItemList(List<ChartAreaItem> list) {
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

    public void loadData(Context context, List<ChartAreaItem> chartAreaItemList, int defaultRawGpxDataId) {
        if (chartAreaItemList.isEmpty())
            return;

        contextWeakReference = new WeakReference<>(context);
        this.defaultRawGpxDataId = defaultRawGpxDataId;

        requestStatus.onNext(LOADING);

        ConcurrentUtil.tryToDispose(loadDataDisposable);
        loadDataDisposable = provideDataEntityVector(contextWeakReference.get(), defaultRawGpxDataId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(this::updateGpxDataCache)
                .flatMap(gpxData ->
                        Observable.fromIterable(chartAreaItemList)
                                .flatMap(chartAreaItem -> updateDataWrapperItem(chartAreaItem, gpxData))
                )
                .doOnNext(chartAreaItem -> requestStatus.onNext(CHART_UPDATING))
                .flatMap(chartAreaItem ->
                        updateChart(chartAreaItem.getChartController(), chartAreaItem.getDataEntityWrapper())
                )
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

    private Vector<DataEntity> updateGpxDataCache(Vector<DataEntity> gpxData) {
        requestStatus.onNext(DATA_LOADED);

        /**
         * Use selected file once - next time use cached from memory(gpxData or default from rawResId) - don't load twice!
         */
        selectGpxFileUseCase.setSelectedFile(null);
        setGpxData(gpxData);

        return gpxData;
    }

    @NonNull
    private Observable<ChartAreaItem> updateDataWrapperItem(ChartAreaItem chartAreaItem, Vector<DataEntity> gpxData) {
        return chartAreaItem.getChartController()
                .reinitChart()
                .map(status -> {
                    //requestStatus.onNext(status);

                    ViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

                    int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode);
                    chartAreaItem.setDataEntityWrapper(new DataEntityWrapper(gpxData, primaryKeyIndex));

                    return chartAreaItem;
                });
    }

    private Observable<Vector<DataEntity>> provideDataEntityVector(Context context, int rawResId) {

        File selectedFile = selectGpxFileUseCase.getSelectedFile();
        /**
         * Use selected file once - next time use cached from memory(gpxData or default from rawResId) - don't load twice!
         */
        return (selectedFile != null)
                ? dataProvider.provide(selectedFile)
                : (gpxData != null) ?
                Observable.just(gpxData)
                : dataProvider.provide(context, rawResId);
    }

    private Observable<RequestStatus> updateChart(ChartController chartController, DataEntityWrapper dataEntityWrapper) {
        return chartController.updateChartData(dataEntityWrapper);
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
        return dataProvider.getPercentageProgress();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }

}
