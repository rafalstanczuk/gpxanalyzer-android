package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemPropertiesControlLayoutBinding;
import com.itservices.gpxanalyzer.ui.components.fab.SpeedDialFabView;
import com.itservices.gpxanalyzer.ui.components.fab.UnfoldDirection;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class dedicated to configuring the Speed Dial FAB component used for
 * chart settings actions within a {@link ChartAreaItem} layout.
 * It specifically targets the FABs defined in `chart_area_item_properties_control_layout.xml`.
 */
public class SpeedDialFabHelperChartSettings {
    private static final String TAG = SpeedDialFabHelperChartSettings.class.getSimpleName();

    /**
     * Default constructor.
     */
    public SpeedDialFabHelperChartSettings() {
    }

    /**
     * Configures the SpeedDialFabView for chart settings actions using the provided binding and chart item data.
     * Sets unfold direction, translation distance, rotation, alpha, and binds the secondary FABs
     * (switch mode, draw segments, draw circles) from the layout to the SpeedDialFabView controller.
     *
     * @param binding       The {@link ChartAreaItemPropertiesControlLayoutBinding} instance containing the SpeedDialFabView and its associated secondary FABs.
     * @param chartAreaItem The {@link ChartAreaItem} (although not directly used in this version, it's kept for potential future use or context).
     */
    public void configureSpeedDialFab(ChartAreaItemPropertiesControlLayoutBinding binding, ChartAreaItem chartAreaItem) {
        if (binding == null || chartAreaItem == null) {
            Log.e(TAG, "Cannot configure SpeedDialFabView: binding or chartAreaItem is null");
            return;
        }

        SpeedDialFabView speedDialFab = binding.settingsSpeedDialFabChartSettings;

        Log.d(TAG, "SpeedDialFabView found, configuring with chart area item: " + chartAreaItem.getChartSlot());

        // Configure unfold direction
        speedDialFab.setUnfoldDirection(UnfoldDirection.RIGHT);

        // Set a distance appropriate for LEFT unfolding
        speedDialFab.setTranslationDistanceDp(45f);
        speedDialFab.setMainFabRotateDegrees(45f);
        speedDialFab.setSecondaryFabsTargetAlpha(0.7f);

        List<FloatingActionButton> fabSetupList = Arrays.asList(
                binding.switchModeButton,
                binding.drawAscDescSegFab,
                binding.onOffColorizedCirclesCheckBox
        );
        speedDialFab.bindActionsToExistingFabs(
                fabSetupList
        );

        // Make sure the main FAB is visible
        speedDialFab.setVisibility(View.VISIBLE);

        Log.d(TAG, "SpeedDialFabView configuration complete");
    }
}