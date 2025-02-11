package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.chart.RequestStatus.PROCESSING;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.chart.entry.BaseDataEntityEntry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.GPXDataProvider;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class MultipleSyncedGpxChartUseCase {
    @Inject
    GPXDataProvider dataProvider;

    @Inject
    ChartController altitudeTimeChartController;

    @Inject
    ChartController speedTimeChartController;

    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();

    private Disposable disposable;
    private File selectedGpxFile = null;

    @Inject
    public MultipleSyncedGpxChartUseCase() {}

    public void selectFile(File gpxFile) {
        this.selectedGpxFile = gpxFile;
    }

    public void loadData(Context context, int rawResId) {

        observeSelectionOn(altitudeTimeChartController.getSelection(), speedTimeChartController);
        observeSelectionOn(speedTimeChartController.getSelection(), altitudeTimeChartController);

        requestStatus.onNext(LOADING);

        disposable = provideDataEntityVector(context, rawResId)
                .map(gpxData -> {
                    requestStatus.onNext(DATA_LOADED);
                    return gpxData;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> Log.e("loadData", "loadData: doOnError ", e))
                .map(gpxData -> {

                    requestStatus.onNext(PROCESSING);

                    RequestStatus requestStatusAltitude = updateAltitudeChart(context, gpxData);

                    RequestStatus requestStatusSpeed = updateSpeedChart(context, gpxData);

                    requestStatus.onNext(PROCESSED);

                    return RequestStatus.values()[ Math.min( requestStatusAltitude.ordinal(), requestStatusSpeed.ordinal() ) ];

                })
                .subscribe(
                        requestStatus::onNext,
                        onError -> Log.e("loadData", "loadData: onError ", onError)
                );
    }

    private Observable<Vector<DataEntity>> provideDataEntityVector(Context context, int rawResId) {
        return (selectedGpxFile != null) ? dataProvider.provide(selectedGpxFile) : dataProvider.provide(context, rawResId);
    }

    private RequestStatus updateSpeedChart(Context context, Vector<DataEntity> gpxData) {
        int speedPrimaryIndex = getNewPrimaryIndexFromNameStringRes(context, R.string.speed);
        StatisticResults statisticResultsSpeed = new StatisticResults(gpxData, speedPrimaryIndex);

        return speedTimeChartController.refreshStatisticResults( statisticResultsSpeed );
    }

    private RequestStatus updateAltitudeChart(Context context, Vector<DataEntity> gpxData) {
        int altitudePrimaryIndex = getNewPrimaryIndexFromNameStringRes(context, R.string.altitude);
        StatisticResults altitudeStatisticResults = new StatisticResults(gpxData, altitudePrimaryIndex);

        return altitudeTimeChartController.refreshStatisticResults(altitudeStatisticResults);
    }

    private void observeSelectionOn(Observable<BaseDataEntityEntry> selection, ChartController chartController) {
        selection
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<BaseDataEntityEntry>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseDataEntityEntry baseDataEntityEntry) {
                        chartController.manualSelectEntry( baseDataEntityEntry.getDataEntity().getTimestampMillis() );
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private static int getNewPrimaryIndexFromNameStringRes(Context context, int id) {
        String speedKeyName = context.getResources().getString(id);
        final int newPrimaryIndex = Arrays.asList( context.getResources().getStringArray(R.array.gpx_name_unit_array) ).indexOf(speedKeyName);
        return newPrimaryIndex;
    }

    public void disposeAll() {
        ConcurrentUtil.tryToDispose(disposable);
    }

    public void bindAltitudeTimeChart(@NonNull DataEntityLineChart lineChart, @NonNull MainActivity mainActivity) {
        lineChart.getSettings().setDrawXLabels(false);
        lineChart.getSettings().setDragDecelerationEnabled(false);
        altitudeTimeChartController.bindChart(lineChart, mainActivity);
    }

    public void bindSpeedTimeChart(@NonNull DataEntityLineChart lineChart, @NonNull MainActivity mainActivity) {
        lineChart.getSettings().setDrawXLabels(true);
        lineChart.getSettings().setDragDecelerationEnabled(false);
        speedTimeChartController.bindChart(lineChart, mainActivity);
    }

    /*
            altitudeChartSettingsDotsMutableLiveData.postValue(lineChart.getMarker().);
    * 		LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSets().get(0);
		lineDataSet.setDrawIcons(drawIconsEnabled);*/

    public Observable<Integer> getPercentageProgress() {
        return dataProvider.getPercentageProgress();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }

    public ChartController getAltitudeTimeChartController() {
        return altitudeTimeChartController;
    }

    public ChartController getSpeedTimeChartController() {
        return speedTimeChartController;
    }
}
