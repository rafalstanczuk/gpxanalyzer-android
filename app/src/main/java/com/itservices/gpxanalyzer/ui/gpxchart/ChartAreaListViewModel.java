package com.itservices.gpxanalyzer.ui.gpxchart;

import static com.itservices.gpxanalyzer.events.PercentageUpdateEventSourceType.GPX_FILE_DATA_ENTITY_PROVIDER;
import static java.util.Objects.requireNonNull;

import android.content.res.Configuration;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.ui.components.chart.extras.OverlayViewToReloadLayoutView;
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

/**
 * ViewModel for the {@link ChartAreaListFragment}.
 * Manages the list of chart items ({@link ChartAreaItem}), handles data loading orchestration
 * via {@link MultipleSyncedGpxChartUseCase}, observes application-wide events (like status and progress)
 * via {@link GlobalEventWrapper}, and exposes UI state through LiveData.
 *
 * It also handles UI logic such as:
 * - Managing different chart display modes ({@link ViewModeSeverity}).
 * - Adjusting chart layout based on device orientation.
 * - Enabling/disabling UI controls based on loading status.
 * - Displaying loading progress indicators.
 * - Handling user interactions forwarded from the Fragment (e.g., button clicks, checkbox changes).
 */
@HiltViewModel
public class ChartAreaListViewModel extends ViewModel {
    /** Use case for loading data into multiple synchronized charts. */
    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;
    /** Global event bus for observing status and progress updates. */
    @Inject
    GlobalEventWrapper eventWrapper;

    /** Default height percentage for charts in portrait mode. */
    public static final float DEFAULT_PERCENT_VALUE = 0.5f;
    /** LiveData holding the calculated height percentage for chart views, adjusted for orientation and severity mode. */
    public final MutableLiveData<Float> chartPercentageHeightLiveData = new MutableLiveData<>(DEFAULT_PERCENT_VALUE);
    /** LiveData holding the current device orientation (Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE). */
    public final MutableLiveData<Integer> orientationLiveData = new MutableLiveData<>(Configuration.ORIENTATION_PORTRAIT);
    /** LiveData indicating whether UI buttons (like Load, Select File) should be enabled, based on the current RequestStatus. */
    private final MutableLiveData<Boolean> buttonsEnabledLiveData = new MutableLiveData<>(true);
    /** LiveData holding the current percentage progress for data loading operations. */
    private final MutableLiveData<Integer> percentageLoadingFileLiveData = new MutableLiveData<>(0);
    /** LiveData controlling the visibility (View.VISIBLE or View.GONE) of the progress loading indicator. */
    private final MutableLiveData<Integer> percentageLoadingFileIndicatorVisibilityLiveData = new MutableLiveData<>(0);
    /** LiveData controlling the visibility of the overlay view shown during loading/processing. */
    private final MutableLiveData<Integer> overlayViewToReloadIndicatorVisibilityLiveData = new MutableLiveData<>(View.VISIBLE);
    /** LiveData holding the current list of {@link ChartAreaItem}s to be displayed. */
    private final MutableLiveData<List<ChartAreaItem>> chartAreaItemListLiveData = new MutableLiveData<>(new ArrayList<>());
    /** LiveData holding the current chart display severity mode (e.g., how many charts are shown). */
    private final MutableLiveData<ViewModeSeverity> viewModeSeverityLiveData = new MutableLiveData<>(ViewModeSeverity.ONE_CHART);

    /** RxJava Subject used to trigger a reload of the chart data. */
    private final PublishSubject<List<ChartAreaItem>> reloadItems = PublishSubject.create();

    // Disposables for managing RxJava subscriptions
    private Disposable observeReloadEventDisposable;
    private Disposable observeRequestStatusDisposable;
    private Disposable observeLoadingFileProgressDisposable;

    /** A copy of the default/initial list of chart items, used when adding charts back in different severity modes. */
    private List<ChartAreaItem> immutableList;

    /**
     * Constructor used by Hilt for dependency injection.
     */
    @Inject
    public ChartAreaListViewModel() {
    }

    /**
     * Sets the list of chart items to be displayed.
     * Posts the value to {@link #chartAreaItemListLiveData}.
     *
     * @param chartAreaItemList The new list of {@link ChartAreaItem}s.
     */
    public void setChartAreaItemList(List<ChartAreaItem> chartAreaItemList) {
        Log.d(ChartAreaListViewModel.class.getSimpleName(), "setChartAreaItemList() called with: chartAreaItemList = [" + chartAreaItemList + "]");

        ViewModeSeverity viewModeSeverity = getViewModeSeverityLiveData().getValue();

        assert viewModeSeverity != null;

        createOnSeverityMode(viewModeSeverity, chartAreaItemList);
    }

    /**
     * Returns the LiveData containing the current list of chart items.
     *
     * @return MutableLiveData<List<ChartAreaItem>>
     */
    public MutableLiveData<List<ChartAreaItem>> getChartAreaItemListLiveData() {
        return chartAreaItemListLiveData;
    }

    /**
     * Binds the ViewModel to necessary event streams (RequestStatus, Progress) from the {@link GlobalEventWrapper}
     * and sets up the observer for the internal reload trigger ({@link #reloadItems}).
     * This should typically be called when the associated Fragment/Activity is started or resumed.
     */
    public void bind() {
        observeLoadingFileProgressOnLiveData(eventWrapper.getEventProgressFromType(GPX_FILE_DATA_ENTITY_PROVIDER));
        observeRequestStatusOnLiveData(eventWrapper.getRequestStatus());

        observeReloadItemsRequestOn(reloadItems);
    }

    /**
     * Triggers a data loading sequence for the currently displayed chart items
     * by posting the current list to the {@link #reloadItems} subject.
     */
    public void postEventLoadData() {
        assert chartAreaItemListLiveData.getValue() != null;

        reloadItems.onNext(chartAreaItemListLiveData.getValue());
    }

    /**
     * Cycles to the next {@link ViewModeSeverity} (e.g., from TWO_CHARTS to THREE_CHARTS).
     * Updates the {@link #viewModeSeverityLiveData}, adjusts the chart item list using
     * {@link #createOnSeverityMode(ViewModeSeverity)}, and recalculates layout percentages.
     */
    public void switchSeverityMode() {
        ViewModeSeverity mode = viewModeSeverityLiveData.getValue();

        assert mode != null;
        ViewModeSeverity newMode = mode.getNextCyclic();
        viewModeSeverityLiveData.setValue(newMode);

        assert chartAreaItemListLiveData.getValue() != null : "Current chart list should not be null";
        createOnSeverityMode(newMode, chartAreaItemListLiveData.getValue());

        // Reload orientation-based percentage heights
        assert orientationLiveData.getValue() != null;
        setOrientation(orientationLiveData.getValue());
    }

    /**
     * Adjusts the list of displayed chart items based on the target {@link ViewModeSeverity}.
     * If reducing the number of charts, it keeps the initial items to preserve cache.
     * If increasing, it adds items from the {@link #immutableList} (default items).
     *
     * @param mode The target {@link ViewModeSeverity}.
     */
    private void createOnSeverityMode(ViewModeSeverity mode, List<ChartAreaItem> currentList) {
        assert immutableList != null : "Immutable chart list must be set before changing severity";

        int targetCount = mode.getCount();
        int currentCount = currentList.size();

        // Start with the relevant subset of the current list (up to targetCount)
        // This handles shrinking correctly as subList end index is exclusive.
        List<ChartAreaItem> newList = new ArrayList<>(currentList.subList(0, Math.min(targetCount, currentCount)));

        // If expanding, add items from the immutable list
        if (targetCount > currentCount) {
            int itemsToAdd = targetCount - currentCount;
            // Calculate how many *new* items can be added from immutableList
            int availableFromImmutable = Math.max(0, immutableList.size() - currentCount);
            int limit = Math.min(itemsToAdd, availableFromImmutable);

            // Add items from immutableList starting from the index after the current ones
            for (int i = 0; i < limit; i++) {
                newList.add(immutableList.get(currentCount + i));
            }
        }

        chartAreaItemListLiveData.setValue(newList);

        Log.d("ChartAreaListViewModel",
              "Changed severity mode to " + mode + ", target count: " + targetCount + ", new count: " + newList.size());
    }

    /**
     * Handles device orientation changes.
     * Updates the {@link #orientationLiveData}, recalculates chart height percentages based on the new orientation
     * and current {@link ViewModeSeverity}, and triggers a reload if charts are present.
     *
     * @param orientation The new orientation (e.g., {@code Configuration.ORIENTATION_LANDSCAPE}).
     */
    public void setOrientation(int orientation) {
        ViewModeSeverity viewModeSeverity = this.viewModeSeverityLiveData.getValue();
        assert viewModeSeverity != null;

        combineWithMapViewVisibility(orientation, viewModeSeverity);

        orientationLiveData.setValue(orientation);

        if( !requireNonNull(chartAreaItemListLiveData.getValue()).isEmpty() ) {
            reloadItems.onNext(chartAreaItemListLiveData.getValue());
        }
    }

    /**
     * Calculates the appropriate chart height percentage based on orientation and severity mode.
     *
     * @param orientation      Current device orientation.
     * @param viewModeSeverity Current chart severity mode.
     */
    private void combineWithMapViewVisibility(int orientation, ViewModeSeverity viewModeSeverity) {
        float percentageHeight = 1.0f;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            percentageHeight = viewModeSeverity.getPercentageHeightLandscape();
        } else {
            percentageHeight = viewModeSeverity.getPercentageHeightPortrait();
        }


        chartPercentageHeightLiveData.postValue(percentageHeight);
    }

    /**
     * Returns LiveData for the calculated chart height percentage.
     *
     * @return LiveData<Float>
     */
    public LiveData<Float> getDataEntityChartPercentageHeight() {
        return chartPercentageHeightLiveData;
    }

    /**
     * Returns LiveData indicating whether UI buttons should be enabled based on the current loading status.
     *
     * @return LiveData<Boolean>
     */
    public LiveData<Boolean> getButtonsEnabledByRequestStatusLiveData() {
        return buttonsEnabledLiveData;
    }

    /**
     * Returns LiveData controlling the visibility of the loading overlay view.
     *
     * @return LiveData<Integer> (View.VISIBLE or View.GONE)
     */
    public LiveData<Integer> getOverlayViewToReloadIndicatorVisibilityLiveData() {
        return overlayViewToReloadIndicatorVisibilityLiveData;
    }

    /**
     * Returns LiveData for the current chart severity mode.
     *
     * @return LiveData<ViewModeSeverity>
     */
    public LiveData<ViewModeSeverity> getViewModeSeverityLiveData() {
        return viewModeSeverityLiveData;
    }

    /**
     * Returns LiveData for the current data loading progress percentage.
     *
     * @return LiveData<Integer>
     */
    public LiveData<Integer> getPercentageLoadingFileLiveData() {
        return percentageLoadingFileLiveData;
    }

    /**
     * Returns LiveData controlling the visibility of the percentage loading indicator.
     *
     * @return LiveData<Integer> (View.VISIBLE or View.GONE)
     */
    public LiveData<Integer> getPercentageLoadingVisibilityLiveData() {
        return percentageLoadingFileIndicatorVisibilityLiveData;
    }

    /**
     * Sets up the RxJava subscription to the internal {@link #reloadItems} subject.
     * When items are posted, it triggers the data loading process via {@link MultipleSyncedGpxChartUseCase}.
     *
     * @param listPublishSubject The subject to observe.
     */
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

    /**
     * Sets up the RxJava subscription to observe {@link RequestStatus} events from the GlobalEventWrapper.
     * Updates LiveData values for button enablement and overlay visibility based on the received status.
     *
     * @param requestStatus Observable stream of RequestStatus events.
     */
    private void observeRequestStatusOnLiveData(Observable<RequestStatus> requestStatus) {
        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        observeRequestStatusDisposable = requestStatus
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.io())
                .subscribe(
                        request -> {
                            Log.d("requestStatus", "request = [" + request.name() + "]");

                            overlayViewToReloadIndicatorVisibilityLiveData.postValue(
                                    OverlayViewToReloadLayoutView.getOverlayViewToReloadIndicatorVisibility(request)
                            );

                            boolean uiButtonsEnabled = getButtonEnabled(request);
                            buttonsEnabledLiveData.postValue(uiButtonsEnabled);
                            percentageLoadingFileIndicatorVisibilityLiveData
                                    .postValue(
                                            uiButtonsEnabled ? View.GONE : View.VISIBLE
                                    );
                        },
                        onError -> Log.e("requestStatus", onError.toString())
                );
    }

    /**
     * Determines if UI buttons should be enabled based on the current {@link RequestStatus}.
     * Buttons are typically disabled during loading, processing, and updating states.
     *
     * @param requestStatus The current status.
     * @return True if buttons should be enabled, false otherwise.
     */
    private static boolean getButtonEnabled(RequestStatus requestStatus) {
        return switch (requireNonNull(requestStatus)) {
            case LOADING, NEW_DATA_LOADING, DATA_LOADED, PROCESSING, PROCESSED, CHART_INITIALIZED, CHART_UPDATING,
                 CHART_UPDATED -> false;
            case ERROR_DATA_SETS_NULL, ERROR_LINE_DATA_SET_NULL, ERROR_NEW_DATA_SET_NULL,
                 ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW, CHART_WEAK_REFERENCE_IS_NULL, CHART_IS_NULL,
                 ERROR, DEFAULT, SELECTED_FILE, DONE -> true;
        };
    }

    /**
     * Sets up the RxJava subscription to observe {@link EventProgress} events (specifically for GPX_FILE_DATA_ENTITY_PROVIDER)
     * from the GlobalEventWrapper. Updates the {@link #percentageLoadingFileLiveData}.
     *
     * @param eventProgressObservable Observable stream of EventProgress events.
     */
    private void observeLoadingFileProgressOnLiveData(Observable<EventProgress> eventProgressObservable) {
        ConcurrentUtil.tryToDispose(observeLoadingFileProgressDisposable);
        observeLoadingFileProgressDisposable = eventProgressObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(eventProgress -> {
                    //Log.d("LoadingFileProgress", "observeLoadingFileProgressOnLiveData() called with: eventProgress = [" + eventProgress + "]");
                    if (eventProgress != null) {
                        percentageLoadingFileLiveData.setValue(eventProgress.percentage());
                    }
                })
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }

    /**
     * Callback handler for the 'Colorized Circles' checkbox change event for a specific chart item.
     * Updates the item's state and its corresponding chart controller.
     *
     * @param item      The {@link ChartAreaItem} whose setting changed.
     * @param isChecked The new checked state.
     */
    public void onOnOffColorizedCirclesCheckBoxChanged(ChartAreaItem item, boolean isChecked) {
        item.setDrawIconsEnabled(isChecked);
        item.getChartController().setDrawIconsEnabled(isChecked);
    }

    /**
     * Callback handler for the 'Draw Asc/Desc Segments' checkbox change event for a specific chart item.
     * Updates the item's state and its corresponding chart controller.
     *
     * @param item      The {@link ChartAreaItem} whose setting changed.
     * @param isChecked The new checked state.
     */
    public void onDrawAscDescSegCheckBoxChanged(ChartAreaItem item, boolean isChecked) {
        item.setDrawAscDescSegEnabled(isChecked);
        item.getChartController().setDrawAscDescSegEnabled(isChecked);
    }

    /**
     * Callback handler for the 'Switch View Mode' button click for a specific chart item.
     * Cycles through the available {@link GpxViewMode}s for that chart item.
     *
     * @param item The {@link ChartAreaItem} whose view mode should be switched.
     */
    public void onSwitchViewMode(ChartAreaItem item) {
        GpxViewMode newItemViewMode = requireNonNull(item.getViewMode().getValue()).getNextCyclic();
        item.setViewMode(newItemViewMode);

        reloadItems.onNext( Collections.singletonList(item) );
    }

    /**
     * Callback handler for the 'Zoom In' button click for a specific chart item.
     * Delegates the action to the item's chart controller.
     *
     * @param item The {@link ChartAreaItem} to zoom in.
     */
    public void onZoomIn(ChartAreaItem item) {
        item.getChartController().animateZoomAndCenterToHighlighted(1.2f, 1.0f, 200);
    }

    /**
     * Callback handler for the 'Zoom Out' button click for a specific chart item.
     * Delegates the action to the item's chart controller.
     *
     * @param item The {@link ChartAreaItem} to zoom out.
     */
    public void onZoomOut(ChartAreaItem item) {
        item.getChartController().animateZoomAndCenterToHighlighted(1f/1.2f, 1.0f, 200);
    }

    /**
     * Callback handler for the 'Auto Scaling' button click for a specific chart item.
     * Delegates the action to the item's chart controller.
     */
    public void onAutoScaling(ChartAreaItem item) {
        item.getChartController().animateFitScreen(1000);
    }

    /**
     * Disposes of all RxJava subscriptions managed by this ViewModel and instructs the
     * {@link MultipleSyncedGpxChartUseCase} to dispose of its resources.
     * This prevents memory leaks and stops ongoing background operations.
     * Should be called when the ViewModel is about to be destroyed (e.g., in {@code onCleared()})
     * or when the associated view is paused/stopped.
     */
    public void dispose() {
        multipleSyncedGpxChartUseCase.disposeAll();

        ConcurrentUtil.tryToDispose(observeReloadEventDisposable);
        ConcurrentUtil.tryToDispose(observeRequestStatusDisposable);
        ConcurrentUtil.tryToDispose(observeLoadingFileProgressDisposable);
    }

    /**
     * Stores the initial or default list of chart items.
     * This list is typically used as a reference when adding or removing chart items
     * due to changes in the {@link ViewModeSeverity}, allowing the restoration of default charts.
     *
     * @param immutableList The default list of {@link ChartAreaItem}s, treated as immutable within this context.
     */
    public void setDefaultChartAreaItemList(List<ChartAreaItem> immutableList) {
        this.immutableList = immutableList;
    }

    /**
     * Handles the resume lifecycle event of the associated Fragment/Activity.
     * Currently, the logic to automatically trigger a data reload (`postEventLoadData()`) on resume is commented out.
     * This method could be used to refresh data or re-establish observers if needed when the UI becomes active.
     */
    public void onResume() {
       // postEventLoadData();
    }
}