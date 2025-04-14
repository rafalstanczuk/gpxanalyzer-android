package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemScaleControlLayoutBinding;
import com.itservices.gpxanalyzer.ui.components.fab.SpeedDialFabView;
import com.itservices.gpxanalyzer.ui.components.fab.UnfoldDirection;

import java.util.Arrays;
import java.util.List;

public class SpeedDialFabHelperZoom {
    private static final String TAG = SpeedDialFabHelperZoom.class.getSimpleName();

    public SpeedDialFabHelperZoom() {
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

        // Configure unfold direction
        speedDialFab.setUnfoldDirection(UnfoldDirection.LEFT);

        // Set a distance appropriate for LEFT unfolding
        speedDialFab.setTranslationDistanceDp(45f);
        speedDialFab.setMainFabRotateDegrees(360f);
        speedDialFab.setSecondaryFabsTargetAlpha(0.7f);

        List<FloatingActionButton> fabSetupList = Arrays.asList(
                binding.timeZoomInButton,
                binding.timeZoomOutButton,
                binding.timeAutoscalingButton
        );
        speedDialFab.bindActionsToExistingFabs(
                fabSetupList
        );

        // Make sure the main FAB is visible
        speedDialFab.setVisibility(View.VISIBLE);

        Log.d(TAG, "SpeedDialFabView configuration complete");
    }
}