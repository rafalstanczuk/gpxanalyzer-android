package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.data.RequestStatus.DEFAULT;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.data.RequestStatus;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemAdapter;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewModeSeverity;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.SingleLiveEvent;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.ArrayList;
import java.util.Collection;
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

    public static final float DEFAULT_PERCENT_VALUE = 0.5f;

    public final MutableLiveData<Float> chartPercentageHeightLiveData
            = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);

    public final MutableLiveData<Integer> orientationLiveData
            = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);
    private final PublishSubject<List<ChartAreaItem>> reloadItems = PublishSubject.create();
    private final MutableLiveData<RequestStatus> requestStatusLiveData = new MutableLiveData<>(DEFAULT);
    private final MutableLiveData<Boolean> buttonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> percentageProcessingLiveData = new MutableLiveData<>(0);

    private final MutableLiveData<List<ChartAreaItem>> chartAreaItemListLiveData = new MutableLiveData<>(new ArrayList<>());


    private final MutableLiveData<ViewModeSeverity> viewModeSeverityLiveData = new MutableLiveData<>(ViewModeSeverity.TWO_CHARTS);

    private final MutableLiveData<ChartAreaItem> switchViewModeLiveData = new SingleLiveEvent<>();

    private final MutableLiveData<Pair<ChartAreaItem, Boolean>> onOnOffColorizedCirclesCheckBoxChangedLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> zoomInLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> zoomOutLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<ChartAreaItem> autoScalingLiveData = new SingleLiveEvent<>();

    private Disposable observeReloadEventDisposable;
    private Disposable observeRequestStatusDisposable;
    private Disposable observeProgressDisposable;
    private int defaultRawGpxDataId;
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

    public void bind(Context context, int defaultRawGpxDataId) {
        this.defaultRawGpxDataId = defaultRawGpxDataId;

        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());

        observeReloadEventToReload(reloadItems, context, defaultRawGpxDataId);
    }

    public void loadData(Context context) {
        assert chartAreaItemListLiveData.getValue() != null;

        multipleSyncedGpxChartUseCase.loadData(context, chartAreaItemListLiveData.getValue(), defaultRawGpxDataId);
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
        List<ChartAreaItem> list = chartAreaItemListLiveData.getValue();
        assert list != null;
        list.clear();

        list = new ArrayList<>(immutableList.subList(0, mode.getCount()));
        chartAreaItemListLiveData.setValue(list);

        multipleSyncedGpxChartUseCase.initChartAreaItemList(list);
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

        reloadItems.onNext(requireNonNull(chartAreaItemListLiveData.getValue()));
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

    private void observeReloadEventToReload(PublishSubject<List<ChartAreaItem>> listPublishSubject, Context context, int defaultRawGpxDataId) {
        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        observeReloadEventDisposable = listPublishSubject
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.newThread())
                .doOnNext(chartAreaItemsToReload -> {
                            multipleSyncedGpxChartUseCase.loadData(context, chartAreaItemsToReload, defaultRawGpxDataId);
                        }
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
                .doOnNext(percentageProcessingLiveData::postValue)
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }

    public void onOnOffColorizedCirclesCheckBoxChanged(ChartAreaItem item, boolean isChecked) {

        item.getChartController().setDrawIconsEnabled(isChecked);

        onOnOffColorizedCirclesCheckBoxChangedLiveData.postValue(Pair.create(item, isChecked));
    }

    public void onSwitchViewMode(ChartAreaItem item) {
        ViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
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

    public void switchViewMode(ChartAreaItemAdapter adapter, ChartAreaItem item, Activity activity) {
        int indexOfItem = adapter.getItems().indexOf(item);
        switch (requireNonNull(item.getViewMode().getValue())) {
            case ASL_T_1, V_T_1 -> {
                adapter.notifyItemChanged(indexOfItem);
            }
        }

        reloadItems.onNext(Collections.singletonList(item));
    }

    public void switchSeverityViewModeOrReloadAdapter(ChartAreaItemAdapter adapter) {

        adapter.notifyDataSetChanged();

        reloadItems.onNext(requireNonNull(chartAreaItemListLiveData.getValue()));
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