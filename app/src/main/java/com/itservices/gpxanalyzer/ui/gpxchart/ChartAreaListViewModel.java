package com.itservices.gpxanalyzer.ui.gpxchart;

import static java.util.Objects.requireNonNull;

import android.content.res.Configuration;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.event.MapChartGlobalEventWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewModeSeverity;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.ArrayList;
import java.util.Collections;
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
    @Inject
    MapChartGlobalEventWrapper eventWrapper;

    public static final float DEFAULT_PERCENT_VALUE = 0.5f;
    public final MutableLiveData<Float> chartPercentageHeightLiveData = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);
    public final MutableLiveData<Integer> orientationLiveData = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);

    public final MutableLiveData<Float> mapViewPercentageHeightLiveData = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);
    private final MutableLiveData<Boolean> mapViewEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> buttonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> percentageProcessingLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<ChartAreaItem>> chartAreaItemListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ViewModeSeverity> viewModeSeverityLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS);
    private final PublishSubject<List<ChartAreaItem>> reloadItems = PublishSubject.create();
    private Disposable observeReloadEventDisposable;
    private Disposable observeRequestStatusDisposable;
    private Disposable observeProgressDisposable;
    private List<ChartAreaItem> immutableList;

    @Inject
    public ChartAreaListViewModel() {
    }

    public void setChartAreaItemList(List<ChartAreaItem> chartAreaItemList) {
        chartAreaItemListLiveData.postValue(chartAreaItemList);
    }

    public MutableLiveData<List<ChartAreaItem>> getChartAreaItemListLiveData() {
        return chartAreaItemListLiveData;
    }

    public void bind() {

        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(eventWrapper.getRequestStatus());

        observeReloadItemsRequestOn(reloadItems);
    }

    public void postEventLoadData() {
        assert chartAreaItemListLiveData.getValue() != null;

        reloadItems.onNext(chartAreaItemListLiveData.getValue());
    }

    public void switchSeverityMode() {
        ViewModeSeverity mode = viewModeSeverityLiveData.getValue();

        assert mode != null;
        ViewModeSeverity newMode = mode.getNextCyclic();
        viewModeSeverityLiveData.setValue(newMode);

        createOnSeverityMode(newMode);

        // Reload orientation-based percentage heights
        assert orientationLiveData.getValue() != null;
        setOrientation(orientationLiveData.getValue());
    }

    private void createOnSeverityMode(ViewModeSeverity mode) {
        List<ChartAreaItem> currentList = chartAreaItemListLiveData.getValue();
        assert currentList != null;
        
        int targetCount = mode.getCount();
        int currentCount = currentList.size();
        
        // Build a new list that preserves existing chart items to keep their cached data
        List<ChartAreaItem> newList;
        
        if (targetCount <= currentCount) {
            // Removing charts: keep the first targetCount items to preserve their cache
            newList = new ArrayList<>(currentList.subList(0, targetCount));
        } else {
            // Adding charts: keep all existing charts and add new ones from immutableList
            newList = new ArrayList<>(currentList); // Keep all existing charts with cached data
            
            // Add any additional charts needed from the immutable list
            for (int i = currentCount; i < targetCount; i++) {
                if (i < immutableList.size()) {
                    newList.add(immutableList.get(i));
                }
            }
        }
        
        // Update the LiveData with the new list
        chartAreaItemListLiveData.setValue(newList);

        // Log the change for debugging
        Log.d("ChartAreaListViewModel", 
              "Changed severity mode to " + mode + ", chart count: " + newList.size());
    }

    public void setOrientation(int orientation) {
        ViewModeSeverity viewModeSeverity = this.viewModeSeverityLiveData.getValue();
        assert viewModeSeverity != null;

        combineWithMapViewVisibility(orientation, viewModeSeverity);

        orientationLiveData.setValue(orientation);

        if( !requireNonNull(chartAreaItemListLiveData.getValue()).isEmpty() ) {
            reloadItems.onNext(chartAreaItemListLiveData.getValue());
        }
    }

    private void combineWithMapViewVisibility(int orientation, ViewModeSeverity viewModeSeverity) {


        float percentageHeight = 1.0f;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            percentageHeight = viewModeSeverity.getPercentageHeightLandscape();
        } else {
            percentageHeight = viewModeSeverity.getPercentageHeightPortrait();
        }


        chartPercentageHeightLiveData.postValue(percentageHeight);
    }

    public LiveData<Float> getDataEntityChartPercentageHeight() {
        return chartPercentageHeightLiveData;
    }

    public LiveData<Boolean> getButtonsEnabledByRequestStatusLiveData() {
        return buttonsEnabledLiveData;
    }

    public LiveData<ViewModeSeverity> getViewModeSeverityLiveData() {
        return viewModeSeverityLiveData;
    }

    public LiveData<Integer> getPercentageProcessingLiveData() {
        return percentageProcessingLiveData;
    }

    private void observeReloadItemsRequestOn(PublishSubject<List<ChartAreaItem>> listPublishSubject) {
        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        observeReloadEventDisposable = listPublishSubject
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.newThread())
                .doOnNext(chartAreaItemsToReload ->
                        multipleSyncedGpxChartUseCase
                                .loadData(chartAreaItemsToReload)
                )
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }

    private void observeRequestStatusOnLiveData(Observable<RequestStatus> requestStatus) {
        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        observeRequestStatusDisposable = requestStatus
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.io())
                .subscribe(
                        request -> {
                            Log.d("requestStatus", "request = [" + request.name() + "]");

                            buttonsEnabledLiveData.postValue(getButtonEnabled(request));
                        },
                        onError -> Log.e("requestStatus", onError.toString())
                );
    }

    private boolean getButtonEnabled(RequestStatus requestStatus) {
        return switch (requireNonNull(requestStatus)) {
            case LOADING, NEW_DATA_LOADING, DATA_LOADED, PROCESSING, PROCESSED, CHART_INITIALIZED, CHART_UPDATING,
                 CHART_UPDATED -> false;
            case ERROR_DATA_SETS_NULL, ERROR_LINE_DATA_SET_NULL, ERROR_NEW_DATA_SET_NULL,
                 ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW, CHART_WEAK_REFERENCE_IS_NULL, CHART_IS_NULL,
                 ERROR, DEFAULT, DONE -> true;
        };
    }

    private void observeProgressOnLiveData(Observable<Integer> integerObservable) {
        ConcurrentUtil.tryToDispose(observeProgressDisposable);
        observeProgressDisposable = integerObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(percent -> {
                    if (percent != null) {
                        percentageProcessingLiveData.setValue(percent);
                    }
                })
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }

    public void onOnOffColorizedCirclesCheckBoxChanged(ChartAreaItem item, boolean isChecked) {
        item.setDrawIconsEnabled(isChecked);
        item.getChartController().setDrawIconsEnabled(isChecked);
    }

    public void onDrawAscDescSegCheckBoxChanged(ChartAreaItem item, boolean isChecked) {
        item.setDrawAscDescSegEnabled(isChecked);
        item.getChartController().setDrawAscDescSegEnabled(isChecked);
    }

    public void onSwitchViewMode(ChartAreaItem item) {
        GpxViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
        item.setViewMode(newItemViewMode);

        reloadItems.onNext( Collections.singletonList(item) );
    }

    public void onZoomIn(ChartAreaItem item) {
        item.getChartController().animateZoomAndCenterToHighlighted(1.5f, 1.0f, 200);
    }

    public void onZoomOut(ChartAreaItem item) {
        item.getChartController().animateZoomAndCenterToHighlighted(0.50f, 1.0f, 200);
    }

    public void onAutoScaling(ChartAreaItem item) {
        item.getChartController().animateFitScreen(1000);
    }

    public void onPause() {
        multipleSyncedGpxChartUseCase.disposeAll();

        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        ConcurrentUtil.tryToDispose(observeProgressDisposable);
    }

    public void setDefaultChartAreaItemList(List<ChartAreaItem> immutableList) {
        this.immutableList = immutableList;
    }
}