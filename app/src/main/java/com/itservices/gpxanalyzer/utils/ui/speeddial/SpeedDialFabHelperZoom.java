package com.itservices.gpxanalyzer.utils.ui.speeddial;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemScaleControlLayoutBinding;
import com.itservices.gpxanalyzer.ui.gpxchart.ChartAreaListViewModel;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SpeedDialFabHelperZoom {
    // Action IDs for the speed dial actions
    private static final int ACTION_ZOOM_IN = 1001;
    private static final int ACTION_ZOOM_OUT = 1002;
    private static final int ACTION_AUTO_SCALING = 1003;
    private static final String TAG = SpeedDialFabHelperZoom.class.getSimpleName();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final ChartAreaListViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    /**
     * Creates a new ChartAreaItemSpeedDialHelper.
     *
     * @param viewModel      The ChartAreaListViewModel that will handle the actions
     * @param lifecycleOwner The LifecycleOwner to manage RxJava subscription lifecycle
     */
    public SpeedDialFabHelperZoom(ChartAreaListViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    /**
     * Configures the SpeedDialFabView with the chart area item.
     *
     * @param binding       The binding object for the chart area item scale control layout
     * @param chartAreaItem The chart area item to connect with the speed dial
     */
    public void configureSpeedDialFab(ChartAreaItemScaleControlLayoutBinding binding, ChartAreaItem chartAreaItem) {
        if (binding == null || chartAreaItem == null) {
            Log.e(TAG, "Cannot configure SpeedDialFabView: binding or chartAreaItem is null");
            return;
        }

        SpeedDialFabView speedDialFab = binding.settingsSpeedDialFabZoom;

        Log.d(TAG, "SpeedDialFabView found, configuring with chart area item: " + chartAreaItem.getChartSlot());

        // Set main FAB icon
        speedDialFab.setMainFabIconResource(R.drawable.ic_zoom_settings_selector);

        // Configure unfold direction
        speedDialFab.setUnfoldDirection(UnfoldDirection.LEFT);

        // Set a distance appropriate for LEFT unfolding
        speedDialFab.setTranslationDistanceDp(45f);

        // Use the convenience method to bind actions to existing FABs in the layout
        List<FabSetup> fabSetupList = Arrays.asList(
                new FabSetup(binding.timeZoomInButton, R.drawable.ic_zoom_in_selector, ACTION_ZOOM_IN),
                new FabSetup(binding.timeZoomOutButton, R.drawable.ic_zoom_out_selector, ACTION_ZOOM_OUT),
                new FabSetup(binding.timeAutoscalingButton, R.drawable.ic_recenter_selector, ACTION_AUTO_SCALING)
        );
        speedDialFab.setupWithCommonActions(
                fabSetupList
        );

        // Make sure the main FAB is visible
        speedDialFab.setVisibility(View.VISIBLE);

        // Enable or disable the SpeedDialFabView based on ViewModel's LiveData for button enablement
        viewModel.getButtonsEnabledByRequestStatusLiveData().observe(lifecycleOwner, enabled -> {
            speedDialFab.setEnabled(enabled);
            fabSetupList.forEach(fabSetup -> fabSetup.floatingActionButton().setEnabled(enabled));
        });

        // Subscribe to action clicks and handle them
        disposables.add(speedDialFab.observeActionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(actionId -> {
                    Log.d(TAG, "SpeedDialFabView action clicked: " + actionId);
                    handleActionClick(actionId, chartAreaItem);
                }, throwable -> {
                    Log.e(TAG, "Error handling speed dial action click", throwable);
                }));

        Log.d(TAG, "SpeedDialFabView configuration complete");
    }

    /**
     * Handles clicks on speed dial actions by forwarding them to the appropriate ViewModel methods.
     *
     * @param actionId      The ID of the clicked action
     * @param chartAreaItem The chart area item to apply the action to
     */
    private void handleActionClick(int actionId, ChartAreaItem chartAreaItem) {
        switch (actionId) {
            case ACTION_ZOOM_IN:
                viewModel.onZoomIn(chartAreaItem);
                break;
            case ACTION_ZOOM_OUT:
                viewModel.onZoomOut(chartAreaItem);
                break;
            case ACTION_AUTO_SCALING:
                viewModel.onAutoScaling(chartAreaItem);
                break;
            default:
                Log.w(TAG, "Unknown action ID: " + actionId);
                break;
        }
    }

    /**
     * Cleans up resources and disposes of RxJava subscriptions.
     * Should be called when the chart area item is destroyed.
     */
    public void dispose() {
        disposables.clear();
    }
} 