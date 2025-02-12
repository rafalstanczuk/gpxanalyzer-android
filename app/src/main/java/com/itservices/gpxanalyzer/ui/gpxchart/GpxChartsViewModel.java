package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DEFAULT;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.databinding.PropertiesControlLayoutBinding;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@HiltViewModel
public class GpxChartsViewModel extends ViewModel {
    public static final float CHART_PERCENTAGE_HEIGHT_LANDSCAPE = 0.4f;
    public static final float CHART_PERCENTAGE_HEIGHT_PORTRAIT = 0.4f;
    public static final float DEFAULT_MAX_100_PERCENT = 1f;
    public static final float DEFAULT_PERCENT_VALUE = 0.5f;

    public final MutableLiveData<Float> chartPercentageHeightLiveData
            = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);

    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;

    private final PublishSubject<Integer> orientationPublishSubject = PublishSubject.create();


    private final MutableLiveData<RequestStatus> requestStatusMutableLiveData = new MutableLiveData<>(DEFAULT);
    private final MutableLiveData<Integer> percentageProcessingProgressLiveData = new MutableLiveData<>(0);

    @Inject
    public GpxChartsViewModel() {
    }

    public LiveData<Integer> getPercentageProcessingProgressLiveData() {
        return percentageProcessingProgressLiveData;
    }

    public LiveData<RequestStatus> getRequestStatusLiveData() {
        return requestStatusMutableLiveData;
    }

    public boolean getButtonEnabled(RequestStatus requestStatus) {
        switch (Objects.requireNonNull(requestStatus)) {
            case LOADING:
            case DATA_LOADED:
            case PROCESSING:
                return false;
            default:
                return true;
        }
    }

    public void setOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartPercentageHeightLiveData.setValue(CHART_PERCENTAGE_HEIGHT_LANDSCAPE);
        } else {
            chartPercentageHeightLiveData.setValue(CHART_PERCENTAGE_HEIGHT_PORTRAIT);
        }

        orientationPublishSubject.onNext(orientation);
    }

    public LiveData<Float> getDataEntityChartPercentageHeight() {
        return chartPercentageHeightLiveData;
    }

    public void onPause() {
        multipleSyncedGpxChartUseCase.disposeAll();
    }

    public void bindAltitudeTimeChart(PropertiesControlLayoutBinding propertiesControlLayoutBinding, DataEntityLineChart altitudeTimeLineChart, MainActivity requireActivity) {
        altitudeTimeLineChart.getSettings().setDrawIconsEnabled(
                propertiesControlLayoutBinding.onOffColorizedCirclesCheckBox.isChecked()
        );

        multipleSyncedGpxChartUseCase.bindAltitudeTimeChart(altitudeTimeLineChart, requireActivity);
    }

    public void bindSpeedTimeChart(PropertiesControlLayoutBinding propertiesControlLayoutBinding, DataEntityLineChart speedTimeLineChart, MainActivity requireActivity) {
        speedTimeLineChart.getSettings().setDrawIconsEnabled(
                propertiesControlLayoutBinding.onOffColorizedCirclesCheckBox.isChecked()
        );

        multipleSyncedGpxChartUseCase.bindSpeedTimeChart(speedTimeLineChart, requireActivity);
    }

    public void loadData(Context requireContext, int rawGpxDataId) {
        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());

        observeOrientationChangeToReload(orientationPublishSubject, requireContext, rawGpxDataId);

        multipleSyncedGpxChartUseCase.loadData(requireContext, rawGpxDataId);
    }

    private void observeOrientationChangeToReload(PublishSubject<Integer> orientationPublishSubject, Context requireContext, int rawGpxDataId) {
        orientationPublishSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer orientation) {
                        multipleSyncedGpxChartUseCase.loadData(requireContext, rawGpxDataId);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void observeRequestStatusOnLiveData(Observable<RequestStatus> requestStatus) {
        requestStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RequestStatus>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(RequestStatus newStatus) {
                        requestStatusMutableLiveData.postValue(newStatus);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void observeProgressOnLiveData(Observable<Integer> integerObservable) {
        integerObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer percent) {
                        percentageProcessingProgressLiveData.postValue(percent);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void resetTimeScale(DataEntityLineChart dataEntitiesLineChart) {
        dataEntitiesLineChart.animateFitScreen(1000);
    }

    public void zoomIn(DataEntityLineChart dataEntitiesLineChart) {
        dataEntitiesLineChart.animateZoomToCenter(1.1f, 1.0f, 200);
    }

    public void zoomOut(DataEntityLineChart dataEntitiesLineChart) {
        dataEntitiesLineChart.animateZoomToCenter(0.90f, 1.0f, 200);
    }

    @UiThread
    public void setAltitudeDrawIconEnabled(Activity activity, boolean isChecked) {
        activity.runOnUiThread(() -> multipleSyncedGpxChartUseCase.getAltitudeTimeChartController().setDrawIconsEnabled(isChecked));
    }

    @UiThread
    public void setSpeedDrawIconEnabled(Activity activity, boolean isChecked) {
        activity.runOnUiThread(() -> multipleSyncedGpxChartUseCase.getSpeedTimeChartController().setDrawIconsEnabled(isChecked));
    }
}