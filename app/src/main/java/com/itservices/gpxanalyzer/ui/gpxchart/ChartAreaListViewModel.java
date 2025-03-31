package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DEFAULT;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemAdapter;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewModeSeverity;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.SingleLiveEvent;
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
    public static final float DEFAULT_PERCENT_VALUE = 0.5f;
    public final MutableLiveData<Float> chartPercentageHeightLiveData = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);
    public final MutableLiveData<Integer> orientationLiveData = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);
    private final MutableLiveData<RequestStatus> requestStatusLiveData = new MutableLiveData<>(DEFAULT);
    private final MutableLiveData<Boolean> buttonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> percentageProcessingLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<ChartAreaItem>> chartAreaItemListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ViewModeSeverity> viewModeSeverityLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS);
    private final MutableLiveData<ChartAreaItem> switchViewModeLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<Pair<ChartAreaItem, Boolean>> onOnOffColorizedCirclesCheckBoxChangedLiveData = new SingleLiveEvent<>();

    private final MutableLiveData<Pair<ChartAreaItem, Boolean>> onDrawAscDescSegCheckBoxChangedLiveData = new SingleLiveEvent<>();



    private final MutableLiveData<ChartAreaItem> zoomInLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> zoomOutLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> autoScalingLiveData = new SingleLiveEvent<>();
    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;
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
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());


        if (chartAreaItemListLiveData.getValue() != null && !chartAreaItemListLiveData.getValue().isEmpty()) {
            multipleSyncedGpxChartUseCase.initObserveSelectionOnNeighborChart(chartAreaItemListLiveData.getValue());
        }

        observeReloadItemsRequestOn(reloadItems);
    }

    public void postEventLoadData() {
        assert chartAreaItemListLiveData.getValue() != null;

        multipleSyncedGpxChartUseCase.initObserveSelectionOnNeighborChart(chartAreaItemListLiveData.getValue());
        reloadItems.onNext(chartAreaItemListLiveData.getValue());
    }


    public void postEventLoadData(List<ChartAreaItem> chartAreaItems) {
        reloadItems.onNext(chartAreaItems);
    }

    public void postEventLoadData(ChartAreaItem item) {
        //reloadItems.onNext(Collections.singletonList(item));

        //assert chartAreaItemListLiveData.getValue() != null;

        //reloadItems.onNext(chartAreaItemListLiveData.getValue());
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
        
        // Re-initialize the observation for neighbor charts
        multipleSyncedGpxChartUseCase.initObserveSelectionOnNeighborChart(newList);
        
        // Log the change for debugging
        Log.d("ChartAreaListViewModel", 
              "Changed severity mode to " + mode + ", chart count: " + newList.size());
    }

    public void setOrientation(int orientation) {
        ViewModeSeverity viewModeSeverity = this.viewModeSeverityLiveData.getValue();
        assert viewModeSeverity != null;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            chartPercentageHeightLiveData.postValue(viewModeSeverity.getPercentageHeightLandscape());
        } else {
            chartPercentageHeightLiveData.postValue(viewModeSeverity.getPercentageHeightPortrait());
        }

        orientationLiveData.setValue(orientation);

        if( !requireNonNull(chartAreaItemListLiveData.getValue()).isEmpty() ) {
            reloadItems.onNext(chartAreaItemListLiveData.getValue());
        }
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
                            requestStatusLiveData.postValue(request);

                            buttonsEnabledLiveData.postValue(getButtonEnabled(request));
                        },
                        onError -> Log.e("requestStatus", onError.toString())
                );
    }

    private boolean getButtonEnabled(RequestStatus requestStatus) {
        return switch (requireNonNull(requestStatus)) {
            case LOADING, DATA_LOADED, PROCESSING, PROCESSED, CHART_INITIALIZED, CHART_UPDATING,
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

        item.getChartController().setDrawIconsEnabled(isChecked);

        onOnOffColorizedCirclesCheckBoxChangedLiveData.postValue(Pair.create(item, isChecked));
    }

    public void onDrawAscDescSegCheckBoxChanged(ChartAreaItem item, boolean isChecked) {

        item.getChartController().setDrawAscDescSegEnabled(isChecked);

        onDrawAscDescSegCheckBoxChangedLiveData.postValue(Pair.create(item, isChecked));
    }

    public void onSwitchViewMode(ChartAreaItem item) {
        GpxViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
        item.setViewMode(newItemViewMode);

        switchViewModeLiveData.postValue(item);
    }

    public void onZoomIn(ChartAreaItem item) {
        item.getChartController().animateZoomToCenter(1.1f, 1.0f, 200);

        zoomInLiveData.postValue(item);
    }


    public void onZoomOut(ChartAreaItem item) {
        item.getChartController().animateZoomToCenter(0.90f, 1.0f, 200);

        zoomOutLiveData.postValue(item);
    }

    public void onAutoScaling(ChartAreaItem item) {
        item.getChartController().animateFitScreen(1000);

        autoScalingLiveData.postValue(item);
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

    public void switchViewMode(ChartAreaItemAdapter adapter, ChartAreaItem item) {
        int indexOfItem = adapter.getItems().indexOf(item);
        switch (requireNonNull(item.getViewMode().getValue())) {
            case ASL_T_1, V_T_1 -> {
               // adapter.notifyItemChanged(indexOfItem);
            }
        }

        reloadItems.onNext( Collections.singletonList(item) );
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

    public void setDefaultChartAreaItemList(List<ChartAreaItem> immutableList) {
        this.immutableList = immutableList;
    }
}