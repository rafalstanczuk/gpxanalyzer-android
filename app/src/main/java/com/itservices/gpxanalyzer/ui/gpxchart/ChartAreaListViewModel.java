package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.chart.RequestStatus.DEFAULT;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.chart.RequestStatus;
import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.SingleLiveEvent;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.ArrayList;
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

    public void bind(Activity activity, int defaultRawGpxDataId) {
        this.defaultRawGpxDataId = defaultRawGpxDataId;

        observeProgressOnLiveData(multipleSyncedGpxChartUseCase.getPercentageProgress());
        observeRequestStatusOnLiveData(multipleSyncedGpxChartUseCase.getRequestStatus());

        observeReloadEventToReload(reloadEvent, activity, defaultRawGpxDataId);
    }

    public void loadData(Activity activity) {
        assert chartAreaItemListLiveData.getValue() != null;

        multipleSyncedGpxChartUseCase.loadData(activity, chartAreaItemListLiveData.getValue(), defaultRawGpxDataId);
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

        list = new ArrayList<>( immutableList.subList(0, mode.getCount()) );
        chartAreaItemListLiveData.setValue(list);
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
        reloadEvent.onNext(true);
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

    private void observeReloadEventToReload(PublishSubject<Boolean> reloadEvent, Activity activity, int defaultRawGpxDataId) {
        ////Log.d(ChartAreaListViewModel.class.getSimpleName(), "observeReloadEventToReload() called with: reloadEvent = [" + reloadEvent + "], activity = [" + activity + "], defaultRawGpxDataId = [" + defaultRawGpxDataId + "]");

        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        observeReloadEventDisposable = reloadEvent
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(orientation -> multipleSyncedGpxChartUseCase.loadData(activity, requireNonNull(chartAreaItemListLiveData.getValue()), defaultRawGpxDataId))
                .doOnError(Throwable::printStackTrace)
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
                            requestStatusLiveData.postValue(request);

                            buttonsEnabledLiveData.postValue(getButtonEnabled(request));
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

        onOnOffColorizedCirclesCheckBoxChangedLiveData.postValue(Pair.create(item, isChecked));
    }

    public void onSwitchViewMode(ChartAreaItem item) {
        ViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
        item.setViewMode(newItemViewMode);

        notifySwitchingViewModeToLiveData(item);
    }

    private void notifySwitchingViewModeToLiveData(ChartAreaItem item) {
        multipleSyncedGpxChartUseCase.switchViewMode(item);
        switchViewModeLiveData.postValue(item);
    }


    public void onZoomIn(ChartAreaItem item) {
        // activity.runOnUiThread(() -> {
        item.getChartController().animateZoomToCenter(1.1f, 1.0f, 200);
        //  });

        zoomInLiveData.postValue(item);
    }


    public void onZoomOut(ChartAreaItem item) {
        // activity.runOnUiThread(() -> {
        item.getChartController().animateZoomToCenter(0.90f, 1.0f, 200);
        //    });

        zoomOutLiveData.postValue(item);
    }

    public void onAutoScaling(ChartAreaItem item) {
        //  activity.runOnUiThread(() -> {
        item.getChartController().animateFitScreen(1000);
        //  });

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
            case DISABLED -> {
                adapter.getItems().remove(indexOfItem);
                adapter.notifyItemRemoved(indexOfItem);
            }
            case ASL_T_1, V_T_1 -> {
                adapter.notifyItemChanged(indexOfItem);
            }
        }

        reloadEvent.onNext(true);
    }

    public void switchSeverityViewModeOrReloadAdapter(ChartAreaItemAdapter adapter) {

        adapter.notifyDataSetChanged();

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

    public void setDefaultChartAreaItemList(List<ChartAreaItem> immutableList) {
        this.immutableList = immutableList;
    }
}