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
import com.itservices.gpxanalyzer.chart.DataEntitiesLineChart;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.GPXDataProvider;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.Arrays;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MultipleSyncedGpxChartUseCase {
    @Inject
    GPXDataProvider gpxDataProvider;

    @Inject
    LineChartSettings heightLineChartSettings;
    @Inject
    ChartController heightTimeChartController;

    @Inject
    LineChartSettings velocityLineChartSettings;
    @Inject
    ChartController velocityTimeChartController;

    private final PublishSubject<RequestStatus> requestStatus = PublishSubject.create();

    private Disposable disposable;

    @Inject
    public MultipleSyncedGpxChartUseCase() {}



    public void loadData(Context context, int rawResId) {

        observeSelectionOn(heightTimeChartController.getSelection(), velocityTimeChartController);
        observeSelectionOn(velocityTimeChartController.getSelection(), heightTimeChartController);

        requestStatus.onNext(LOADING);


        disposable = gpxDataProvider.provide(context, rawResId)
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

    private RequestStatus updateSpeedChart(Context context, Vector<DataEntity> gpxData) {
        int speedPrimaryIndex = getNewPrimaryIndexFromNameStringRes(context, R.string.speed);
        StatisticResults statisticResultsSpeed = new StatisticResults(gpxData, speedPrimaryIndex);

        return velocityTimeChartController.refreshStatisticResults( statisticResultsSpeed );
    }

    private RequestStatus updateAltitudeChart(Context context, Vector<DataEntity> gpxData) {
        int altitudePrimaryIndex = getNewPrimaryIndexFromNameStringRes(context, R.string.altitude);
        StatisticResults altitudeStatisticResults = new StatisticResults(gpxData, altitudePrimaryIndex);

        return heightTimeChartController.refreshStatisticResults(altitudeStatisticResults);
    }

    private void observeSelectionOn(Observable<BaseEntry> selection, ChartController chartController) {
        selection
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<BaseEntry>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntry baseEntry) {
                        chartController.manualSelectEntry( baseEntry.getDataEntity().getTimestampMillis() );
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

    public void bindHeightTimeChart(@NonNull DataEntitiesLineChart lineChart, @NonNull MainActivity mainActivity) {
        heightLineChartSettings.setDrawXLabels(false);
        heightLineChartSettings.setDragDecelerationEnabled(false);
        heightTimeChartController.bindChart(lineChart, heightLineChartSettings, mainActivity);
    }

    public void bindVelocityTimeChart(@NonNull DataEntitiesLineChart lineChart, @NonNull MainActivity mainActivity) {
        velocityLineChartSettings.setDrawXLabels(true);
        velocityLineChartSettings.setDragDecelerationEnabled(false);
        velocityTimeChartController.bindChart(lineChart, velocityLineChartSettings, mainActivity);
    }

    public Observable<Integer> getPercentageProgress() {
        return gpxDataProvider.getPercentageProgress();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatus;
    }
}
