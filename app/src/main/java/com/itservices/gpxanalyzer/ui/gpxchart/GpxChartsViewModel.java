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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@HiltViewModel
public class GpxChartsViewModel extends ViewModel {

    public static final float DEFAULT_MAX_100_PERCENT = 1f;
    public static final float DEFAULT_PERCENT_VALUE = 0.5f;

    public final MutableLiveData<Float> chartPercentageHeightLiveData
            = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);

    public final MutableLiveData<Integer> orientationLiveData
            = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);


    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;

    private final PublishSubject<Integer> orientationPublishSubject = PublishSubject.create();

    private final MutableLiveData<ViewMode> viewModeMutableLiveData = new MutableLiveData<>(ViewMode.ASL_T_1);

    private final MutableLiveData<ViewModeSeverity> viewModeSeverityMutableLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS);
    private final MutableLiveData<Integer> viewModeSeverityIconMutableLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS.getDrawableIconResId());

    private final MutableLiveData<RequestStatus> requestStatusMutableLiveData = new MutableLiveData<>(DEFAULT);

    private final MutableLiveData<Boolean> buttonsEnabledByRequestStatusLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> percentageProcessingProgressLiveData = new MutableLiveData<>(0);

    @Inject
    public GpxChartsViewModel() {
    }

    public LiveData<ViewMode> getViewModeMutableLiveData() {
        return viewModeMutableLiveData;
    }

    public void switchSeverityMode() {
        ViewModeSeverity mode = viewModeSeverityMutableLiveData.getValue();

        assert mode != null;
        viewModeSeverityMutableLiveData.setValue(mode.getNextCyclic());

        // Reload orientation-based percentage heights
        assert orientationLiveData.getValue() != null;
        setOrientation(orientationLiveData.getValue());
    }

    public LiveData<ViewModeSeverity> getViewModeSeverityLiveData() {
        return viewModeSeverityMutableLiveData;
    }

    public LiveData<Integer> getPercentageProcessingProgressLiveData() {
        return percentageProcessingProgressLiveData;
    }

    public LiveData<RequestStatus> getRequestStatusLiveData() {
        return requestStatusMutableLiveData;
    }

    private boolean getButtonEnabled(RequestStatus requestStatus) {
        switch (Objects.requireNonNull(requestStatus)) {
            case LOADING:
            case DATA_LOADED:
            case PROCESSING:
                return false;
            default:
                return true;
        }
    }

    public LiveData<Boolean> buttonsEnabledByRequestStatusLiveData() {
        return buttonsEnabledByRequestStatusLiveData;
    }

    public void setOrientation(int orientation) {
        ViewModeSeverity viewModeSeverity = viewModeSeverityMutableLiveData.getValue();
        assert viewModeSeverity != null;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartPercentageHeightLiveData.setValue(viewModeSeverity.getPercentageHeightLandscape());
        } else {
            chartPercentageHeightLiveData.setValue(viewModeSeverity.getPercentageHeightPortrait());
        }

        orientationLiveData.setValue(orientation);
        orientationPublishSubject.onNext(orientation);
    }

    public LiveData<Float> getDataEntityChartPercentageHeight() {
        return chartPercentageHeightLiveData;
    }

    public void onPause() {
        multipleSyncedGpxChartUseCase.disposeAll();
    }

    public void bindFirstChart(PropertiesControlLayoutBinding propertiesControlLayoutBinding, DataEntityLineChart firstLineChart, MainActivity requireActivity) {
        firstLineChart.getSettings().setDrawIconsEnabled(
                propertiesControlLayoutBinding.onOffColorizedCirclesCheckBox.isChecked()
        );

        multipleSyncedGpxChartUseCase.bindFirstChart(firstLineChart, requireActivity);
    }

    public void bindSecondChart(PropertiesControlLayoutBinding propertiesControlLayoutBinding, DataEntityLineChart secondLineChart, MainActivity requireActivity) {
        secondLineChart.getSettings().setDrawIconsEnabled(
                propertiesControlLayoutBinding.onOffColorizedCirclesCheckBox.isChecked()
        );

        multipleSyncedGpxChartUseCase.bindSecondChart(secondLineChart, requireActivity);
    }

    public void loadData(Context requireContext, int defaultRawGpxDataId) {
        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());

        observeOrientationChangeToReload(orientationPublishSubject, requireContext, defaultRawGpxDataId);

        multipleSyncedGpxChartUseCase.loadData(requireContext, defaultRawGpxDataId);
    }

    private void observeOrientationChangeToReload(PublishSubject<Integer> orientationPublishSubject, Context requireContext, int defaultRawGpxDataId) {
        orientationPublishSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(orientation -> multipleSyncedGpxChartUseCase.loadData(requireContext, defaultRawGpxDataId))
                .subscribe();
    }

    private void observeRequestStatusOnLiveData(Observable<RequestStatus> requestStatus) {
        requestStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(request -> {
                            requestStatusMutableLiveData.postValue(request);

                            buttonsEnabledByRequestStatusLiveData.postValue(getButtonEnabled(request));
                        }
                )
                .subscribe();
    }

    private void observeProgressOnLiveData(Observable<Integer> integerObservable) {
        integerObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(percentageProcessingProgressLiveData::postValue)
                .subscribe();
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
    public void setFirstChartDrawIconEnabled(Activity activity, boolean isChecked) {
        activity.runOnUiThread(() -> multipleSyncedGpxChartUseCase.getFirstChartController().setDrawIconsEnabled(isChecked));
    }

    @UiThread
    public void setSecondChartDrawIconEnabled(Activity activity, boolean isChecked) {
        activity.runOnUiThread(() -> multipleSyncedGpxChartUseCase.getSecondChartController().setDrawIconsEnabled(isChecked));
    }
}