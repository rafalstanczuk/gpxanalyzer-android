package com.itservices.gpxanalyzer.core.ui.components.chart;

import com.itservices.gpxanalyzer.core.ui.components.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.core.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.core.ui.components.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.core.data.model.entity.DataEntityWrapper;

import javax.inject.Inject;

/**
 * Container class holding various components responsible for configuring and managing
 * the appearance and behavior of a {@link DataEntityLineChart}.
 * This includes settings, scaling, color palettes, and limit lines.
 */
public class ChartComponents {

    /**
     * Manages the limit lines (horizontal boundaries) displayed on the chart.
     * Injected by Hilt.
     */
    @Inject
    LimitLinesBoundaries limitLinesBoundaries;

    /**
     * Manages the scaling and zooming behavior of the chart axes.
     * Injected by Hilt.
     */
    @Inject
    LineChartScaler scaler;

    /**
     * Determines the appropriate color palette for chart elements based on the displayed data.
     * Injected by Hilt.
     */
    @Inject
    PaletteColorDeterminer paletteColorDeterminer;

    /**
     * Manages general chart settings (visual appearance, interactions).
     * Injected by Hilt.
     */
    @Inject
    LineChartSettings settings;

    /**
     * Constructor used by Hilt for dependency injection.
     */
    @Inject
    public ChartComponents() {

    }

    /**
     * Initializes the components that depend on the specific data being displayed.
     * Updates the {@link PaletteColorDeterminer} and {@link LineChartScaler} with the provided data wrapper.
     *
     * @param dataEntityWrapper The data entity wrapper containing the GPX data context for the chart.
     */
    public void init(DataEntityWrapper dataEntityWrapper) {
        paletteColorDeterminer.setDataEntityWrapper(dataEntityWrapper);
        scaler.setDataEntityWrapper(dataEntityWrapper);
    }

    /**
     * Applies the configurations from all managed components to a specific chart instance.
     * Initializes limit lines, sets up the scaler, applies general settings, and links
     * settings with limit lines.
     *
     * @param chart The {@link DataEntityLineChart} instance to configure.
     */
    public void loadChartSettings(DataEntityLineChart chart) {
        limitLinesBoundaries.initLimitLines(paletteColorDeterminer);
        scaler.setLimitLinesBoundaries(limitLinesBoundaries);
        scaler.scale(chart);
        settings.setLimitLinesBoundaries(limitLinesBoundaries);

        settings.setChartSettingsFor(chart);
    }

    /**
     * Gets the {@link PaletteColorDeterminer} instance managed by this container.
     *
     * @return The {@link PaletteColorDeterminer}.
     */
    public PaletteColorDeterminer getPaletteColorDeterminer() {
        return paletteColorDeterminer;
    }

}
