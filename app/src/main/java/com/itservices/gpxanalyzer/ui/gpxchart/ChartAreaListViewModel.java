package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DEFAULT;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.SingleLiveEvent;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@HiltViewModel
public class ChartAreaListViewModel extends ViewModel {
    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;

    public static final float DEFAULT_PERCENT_VALUE = 0.5f;

    public final MutableLiveData<Float> chartPercentageHeightLiveData
            = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);

    public final MutableLiveData<Integer> orientationLiveData
            = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);
    private final PublishSubject<Boolean> reloadEvent = PublishSubject.create();
    private final MutableLiveData<RequestStatus> requestStatusLiveData = new MutableLiveData<>(DEFAULT);
    private final MutableLiveData<Boolean> buttonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> percentageProcessingLiveData = new MutableLiveData<>(0);

    private final MutableLiveData<List<ChartAreaItem>> chartAreaItemListLiveData = new MutableLiveData<>();


    private final MutableLiveData<ViewModeSeverity> viewModeSeverityLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS);

    private final MutableLiveData<ChartAreaItem> switchViewModeLiveData = new SingleLiveEvent<>();

    private final MutableLiveData<Pair<ChartAreaItem, Boolean>> onOnOffColorizedCirclesCheckBoxChangedLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> zoomInLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> zoomOutLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> autoScalingLiveData = new SingleLiveEvent<>();

    private Disposable observeReloadEventDisposable;
    private Disposable observeRequestStatusDisposable;
    private Disposable observeProgressDisposable;

    @Inject
    public ChartAreaListViewModel() {
    }

    public void setChartAreaItemList(List<ChartAreaItem> chartAreaItemList) {
        chartAreaItemListLiveData.setValue(chartAreaItemList);
    }

    public void loadData(Activity activity, int defaultRawGpxDataId) {
        assert chartAreaItemListLiveData.getValue() != null;

        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());

        observeReloadEventToReload(reloadEvent, activity, defaultRawGpxDataId);

        multipleSyncedGpxChartUseCase.loadData(activity, chartAreaItemListLiveData.getValue(), defaultRawGpxDataId);
    }

    public void switchSeverityMode() {
        ViewModeSeverity mode = viewModeSeverityLiveData.getValue();

        assert mode != null;
        viewModeSeverityLiveData.setValue(mode.getNextCyclic());

        if (viewModeSeverityLiveData.getValue() == ViewModeSeverity.ONE_CHART) {
            tryToDisableLastChart();
        } else {
            tryToEnableDisabledChart();
        }

        // Reload orientation-based percentage heights
        assert orientationLiveData.getValue() != null;
        setOrientation(orientationLiveData.getValue());
    }

    private void tryToEnableDisabledChart() {
        List<ChartAreaItem> list = chartAreaItemListLiveData.getValue();
        assert list != null;


        list.forEach(chartAreaItem -> {
            if (chartAreaItem.getViewMode().getValue() == ViewMode.DISABLED) {
                chartAreaItem.setViewMode(ViewMode.ASL_T_1);
                notifySwitchingViewModeToLiveData(chartAreaItem);
            }
        });


    }

    private void tryToDisableLastChart() {
        List<ChartAreaItem> list = chartAreaItemListLiveData.getValue();
        assert list != null;

        boolean hasAnyDisabled = list.stream()
                .anyMatch(item -> item.getViewMode().getValue() == ViewMode.DISABLED);

        if (!hasAnyDisabled) {
            makeLastItemDisabled(chartAreaItemListLiveData.getValue());
        }
    }

    private void makeLastItemDisabled(List<ChartAreaItem> list) {
        assert list != null;
        ChartAreaItem lastItem = list.get(list.size() - 1);
        lastItem.setViewMode(ViewMode.DISABLED);

        notifySwitchingViewModeToLiveData(lastItem);

    }

    public LiveData<Float> getDataEntityChartPercentageHeight() {
        return chartPercentageHeightLiveData;
    }

    public LiveData<Boolean> buttonsEnabledByRequestStatusLiveData() {
        return buttonsEnabledLiveData;
    }

    public LiveData<ViewModeSeverity> getViewModeSeverityLiveData() {
        return viewModeSeverityLiveData;
    }

    public MutableLiveData<ChartAreaItem> getOnSwitchViewModeChangedLiveData() {
        return switchViewModeLiveData;
    }

    public MutableLiveData<Pair<ChartAreaItem, Boolean>> getOnOnOffColorizedCirclesCheckBoxChangedLiveData() {
        return onOnOffColorizedCirclesCheckBoxChangedLiveData;
    }

    public MutableLiveData<ChartAreaItem> getOnZoomInClickedLiveData() {
        return zoomInLiveData;
    }

    public MutableLiveData<ChartAreaItem> getOnZoomOutLiveData() {
        return zoomOutLiveData;
    }

    public MutableLiveData<ChartAreaItem> getOnAutoScalingLiveData() {
        return autoScalingLiveData;
    }

    public LiveData<Integer> getPercentageProcessingLiveData() {
        return percentageProcessingLiveData;
    }

    public void setOrientation(int orientation) {
        ViewModeSeverity viewModeSeverity = this.viewModeSeverityLiveData.getValue();
        assert viewModeSeverity != null;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartPercentageHeightLiveData.setValue(viewModeSeverity.getPercentageHeightLandscape());
        } else {
            chartPercentageHeightLiveData.setValue(viewModeSeverity.getPercentageHeightPortrait());
        }

        orientationLiveData.setValue(orientation);
        reloadEvent.onNext(true);
    }

    private void observeReloadEventToReload(PublishSubject<Boolean> reloadEvent, Activity activity, int defaultRawGpxDataId) {
        ////Log.d(ChartAreaListViewModel.class.getSimpleName(), "observeReloadEventToReload() called with: reloadEvent = [" + reloadEvent + "], activity = [" + activity + "], defaultRawGpxDataId = [" + defaultRawGpxDataId + "]");

        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        observeReloadEventDisposable = reloadEvent
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(orientation -> multipleSyncedGpxChartUseCase.loadData(activity, requireNonNull(chartAreaItemListLiveData.getValue()), defaultRawGpxDataId))
                .subscribe();
    }

    private void observeRequestStatusOnLiveData(Observable<RequestStatus> requestStatus) {
        ////Log.d(ChartAreaListViewModel.class.getSimpleName(), "observeRequestStatusOnLiveData() called with: requestStatus = [" + requestStatus + "]");

        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        observeRequestStatusDisposable = requestStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(request -> {
                            //Log.d(ChartAreaListViewModel.class.getSimpleName(), "observeRequestStatusOnLiveData() called with: requestStatus = [" + request.name() + "]");
                            requestStatusLiveData.setValue(request);

                            buttonsEnabledLiveData.setValue(getButtonEnabled(request));
                        }
                )
                .subscribe();
    }

    private boolean getButtonEnabled(RequestStatus requestStatus) {
        switch (requireNonNull(requestStatus)) {
            case LOADING:
            case DATA_LOADED:
            case PROCESSING:
                return false;
            default:
                return true;
        }
    }

    private void observeProgressOnLiveData(Observable<Integer> integerObservable) {
        ConcurrentUtil.tryToDispose(observeProgressDisposable);
        observeProgressDisposable = integerObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(percentageProcessingLiveData::postValue)
                .subscribe();
    }

    public void onOnOffColorizedCirclesCheckBoxChanged(ChartAreaItem item, boolean isChecked) {


        //activity.runOnUiThread(() -> {
        item.getChartController().setDrawIconsEnabled(isChecked);
        //});

        onOnOffColorizedCirclesCheckBoxChangedLiveData.setValue(Pair.create(item, isChecked));
    }

    public void onSwitchViewMode(ChartAreaItem item) {
        ViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
        item.setViewMode(newItemViewMode);

        notifySeverityViewModeLiveData(item);

        notifySwitchingViewModeToLiveData(item);
    }

    private void notifySeverityViewModeLiveData(ChartAreaItem item) {

        List<ChartAreaItem> list = chartAreaItemListLiveData.getValue();
        assert list != null;

        boolean hasAnyDisabled = list.stream()
                .anyMatch(itemToCheck -> itemToCheck.getViewMode().getValue() == ViewMode.DISABLED);

        viewModeSeverityLiveData.setValue(hasAnyDisabled?ViewModeSeverity.ONE_CHART: ViewModeSeverity.TWO_CHARTS);
    }

    private void notifySwitchingViewModeToLiveData(ChartAreaItem item) {
        multipleSyncedGpxChartUseCase.switchViewMode(item);
        switchViewModeLiveData.setValue(item);
    }


    public void onZoomIn(ChartAreaItem item) {
        // activity.runOnUiThread(() -> {
        item.getChartController().animateZoomToCenter(1.1f, 1.0f, 200);
        //  });

        zoomInLiveData.setValue(item);
    }


    public void onZoomOut(ChartAreaItem item) {
        // activity.runOnUiThread(() -> {
        item.getChartController().animateZoomToCenter(0.90f, 1.0f, 200);
        //    });

        zoomOutLiveData.setValue(item);
    }

    public void onAutoScaling(ChartAreaItem item) {
        //  activity.runOnUiThread(() -> {
        item.getChartController().animateFitScreen(1000);
        //  });

        autoScalingLiveData.setValue(item);
    }

    public void onPause() {
        multipleSyncedGpxChartUseCase.disposeAll();

        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        ConcurrentUtil.tryToDispose(observeProgressDisposable);
    }

    public void changeOnOffColorizedCircles(ChartAreaItemAdapter adapter, Pair<ChartAreaItem, Boolean> pair, Activity activity) {
        // adapter.notifyItemChanged(adapter.getItems().indexOf(pair.first));
        // reloadEvent.onNext(true);
    }

    public void switchViewMode(ChartAreaItemAdapter adapter, ChartAreaItem item, Activity activity) {
        adapter.notifyItemChanged(adapter.getItems().indexOf(item));
        reloadEvent.onNext(true);
    }

    public void zoomIn(ChartAreaItemAdapter adapter, ChartAreaItem item, Activity activity) {
        // adapter.notifyItemChanged(adapter.getItems().indexOf(item));
        //reloadEvent.onNext(true);
    }

    public void zoomOut(ChartAreaItemAdapter adapter, ChartAreaItem item, Activity activity) {
        //adapter.notifyItemChanged(adapter.getItems().indexOf(item));
        //reloadEvent.onNext(true);
    }

    public void autoScaling(ChartAreaItemAdapter adapter, ChartAreaItem item, Activity activity) {
        //adapter.notifyItemChanged(adapter.getItems().indexOf(item));
        //reloadEvent.onNext(true);
    }
}