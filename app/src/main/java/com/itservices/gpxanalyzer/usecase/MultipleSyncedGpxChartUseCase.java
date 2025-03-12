package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.data.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.data.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.data.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.data.RequestStatus.PROCESSING;

import android.app.Activity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
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

    private Disposable chartUpdateDisposable;

    private CompositeDisposable observeSelectionCompositeDisposable = new CompositeDisposable();
    private Vector<DataEntity> gpxData;
    private Activity activity;
    private int defaultRawGpxDataId;

    @Inject
    public MultipleSyncedGpxChartUseCase() {
    }

    private void setGpxData(Vector<DataEntity> gpxData) {
        this.gpxData = gpxData;
    }

    public void switchViewMode(ChartAreaItem chartAreaItem) {

        if (activity == null)
            return;

        loadData(activity, Collections.singletonList(chartAreaItem), defaultRawGpxDataId);
    }

    public void loadData(Activity activity, List<ChartAreaItem> chartAreaItemList, int defaultRawGpxDataId) {
        if (chartAreaItemList.isEmpty())
            return;

        ConcurrentUtil.tryToDispose(loadDataDisposable);

        this.activity = activity;
        this.defaultRawGpxDataId = defaultRawGpxDataId;

        ConcurrentUtil.tryToDispose(observeSelectionCompositeDisposable);
        observeSelectionCompositeDisposable = new CompositeDisposable();

        chartAreaItemList.forEach(first ->
                chartAreaItemList.forEach(second -> {
                    if (first != second) {
                        observeSelectionCompositeDisposable.add(
                                observeSelectionOn(activity, first.getChartController().getSelection(), second.getChartController())
                        );
                    }
                })
        );

        requestStatus.onNext(LOADING);

        loadDataDisposable = provideDataEntityVector(activity, defaultRawGpxDataId)
                .map(this::updateGpxDataCache)
                .map(gpxData -> updateStatisticsForItemList(chartAreaItemList, gpxData))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .doOnError(e -> Log.e("loadData", "loadData: doOnError ", e))
                .subscribe(
                        this::processChartUpdates,
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
    private List<ChartAreaItem> updateStatisticsForItemList(List<ChartAreaItem> chartAreaItemList, Vector<DataEntity> gpxData) {
        for (ChartAreaItem chartAreaItem : chartAreaItemList) {
            ViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

            int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode);
            chartAreaItem.setDataEntityWrapper(new DataEntityWrapper(gpxData, primaryKeyIndex));
        }

        return chartAreaItemList;
    }

    private void processChartUpdates(List<ChartAreaItem> chartAreaItemList) {
        requestStatus.onNext(PROCESSING);

        List<RequestStatus> updateChartStatusList = new ArrayList<>();

        chartAreaItemList.forEach(item -> {
            chartUpdateDisposable = updateChart(item.getChartController(), item.getDataEntityWrapper())
                    .observeOn(Schedulers.io())
                    .subscribe(updateChartStatus -> {
                                updateChartStatusList.add(updateChartStatus);

                                if (updateChartStatusList.size() == chartAreaItemList.size()) {
                                    requestStatus.onNext(PROCESSED);
                                }
                            }
                    );
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


    private Single<RequestStatus> updateChart(ChartController chartController, DataEntityWrapper dataEntityWrapper) {
        return chartController.updateChartData(dataEntityWrapper);
    }

    private Disposable observeSelectionOn(Activity activity, Observable<BaseEntry> selection, ChartController chartController) {
        return selection
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnNext(baseEntry ->
                        activity.runOnUiThread(() -> {
                            chartController.select(baseEntry.getDataEntity().timestampMillis());
                        })
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
