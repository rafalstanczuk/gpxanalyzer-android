package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSING;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.chart.entry.BaseDataEntityEntry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.GPXDataProvider;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.ui.gpxchart.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.ViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.ViewModeMapper;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

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
        //Log.d(MultipleSyncedGpxChartUseCase.class.getSimpleName(), "switchViewMode() called with: chartAreaItem = [" + chartAreaItem + "]");


        if (activity==null)
            return;

        loadData(activity, Collections.singletonList(chartAreaItem), defaultRawGpxDataId);
    }

    public void loadData(Activity activity, List<ChartAreaItem> chartAreaItemList, int defaultRawGpxDataId) {
        //Log.d(MultipleSyncedGpxChartUseCase.class.getSimpleName(), "loadData() called with: activity = [" + activity + "], chartAreaItemList = [" + chartAreaItemList + "], defaultRawGpxDataId = [" + defaultRawGpxDataId + "]");

        if(chartAreaItemList.isEmpty())
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
                .map(gpxData -> {
                    requestStatus.onNext(DATA_LOADED);

                    setGpxData(gpxData);

                    return gpxData;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> Log.e("loadData", "loadData: doOnError ", e))
                .map(gpxData -> processChartUpdates(gpxData, chartAreaItemList))
                .subscribe(
                        requestStatus::onNext,
                        onError -> Log.e("loadData", "loadData: onError ", onError)
                );
    }

    private RequestStatus processChartUpdates(Vector<DataEntity> gpxData, List<ChartAreaItem> chartAreaItemList) {
        requestStatus.onNext(PROCESSING);

        AtomicReference<RequestStatus> finalRequestStatus = new AtomicReference<>();

        // activity.runOnUiThread(() -> {
        finalRequestStatus.set(updateCharts(gpxData, chartAreaItemList));
        //       });

        requestStatus.onNext(PROCESSED);

        return finalRequestStatus.get();
    }

    private Observable<Vector<DataEntity>> provideDataEntityVector(Context context, int rawResId) {

        File selectedFile = selectGpxFileUseCase.getSelectedFile();

        return (gpxData != null) ?
                Observable.just(gpxData)
                : (
                (selectedFile != null)
                        ? dataProvider.provide(selectedFile)
                        : dataProvider.provide(context, rawResId)
        );
    }

    private RequestStatus updateCharts(Vector<DataEntity> gpxData, List<ChartAreaItem> chartAreaItemList) {
        List<RequestStatus> requestStatusList = new ArrayList<>();

        for (ChartAreaItem chartAreaItem : chartAreaItemList) {
            ViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

            //Log.d(MultipleSyncedGpxChartUseCase.class.getSimpleName(), "updateCharts() called with: iChartViewMode = [" + iChartViewMode + "], chartAreaItemList = [" + chartAreaItemList + "]");

            if (iChartViewMode != ViewMode.DISABLED) {
                requestStatusList.add(
                        updateChart(
                                chartAreaItem.getChartController(), gpxData, viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode)
                        )
                );
            }
        }

        int minOrdinal = requestStatusList.stream()
                .mapToInt(Enum::ordinal)
                .min().orElse(0);

        return RequestStatus.values()[minOrdinal];
    }

    private RequestStatus updateChart(ChartController chartController, Vector<DataEntity> gpxData, int primaryKeyIndex) {
        if (primaryKeyIndex == -1)
            return RequestStatus.DONE;

        return chartController.refreshStatisticResults(new StatisticResults(gpxData, primaryKeyIndex));
    }

    private Disposable observeSelectionOn(Activity activity, Observable<BaseDataEntityEntry> selection, ChartController chartController) {
        //Log.d(MultipleSyncedGpxChartUseCase.class.getSimpleName(), "observeSelectionOn() called with: activity = [" + activity + "], selection = [" + selection + "], chartController = [" + chartController + "]");

        return selection
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(baseDataEntityEntry ->
                        activity.runOnUiThread(() -> {
                            chartController.manualSelectEntry(baseDataEntityEntry.getDataEntity().getTimestampMillis());
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
