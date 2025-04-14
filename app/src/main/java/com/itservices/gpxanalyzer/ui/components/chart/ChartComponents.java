package com.itservices.gpxanalyzer.ui.components.chart;

import com.itservices.gpxanalyzer.ui.components.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.ui.components.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.ui.components.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;

import javax.inject.Inject;

public class ChartComponents {

    /**
     * Manages the limit lines (boundaries) of the chart.
     * <p>
     * This component defines horizontal reference lines that indicate important
     * thresholds or boundaries in the data, such as elevation zones or speed limits.
     */
    @Inject
    LimitLinesBoundaries limitLinesBoundaries;

    /**
     * Manages scaling and zooming of the chart.
     * <p>
     * This component handles the scaling behavior of the chart, ensuring that
     * GPX data is displayed at appropriate scales and with proper boundaries.
     */
    @Inject
    LineChartScaler scaler;

    /**
     * Determines colors for chart elements based on the data being displayed.
     * <p>
     * This component provides a consistent color scheme for the chart based on
     * the type of data being visualized (elevation, speed, etc.).
     */
    @Inject
    PaletteColorDeterminer paletteColorDeterminer;

    @Inject
    LineChartSettings settings;

    @Inject
    public ChartComponents() {

    }

    /**
     * Sets the data entity wrapper for this chart.
     * <p>
     * This method updates the palette color determiner and scaler with the new data.
     * The data entity wrapper provides the GPX data that will be visualized on the chart.
     *
     * @param dataEntityWrapper The data entity wrapper containing GPX data to visualize
     */
    public void init(DataEntityWrapper dataEntityWrapper) {
        paletteColorDeterminer.setDataEntityWrapper(dataEntityWrapper);
        scaler.setDataEntityWrapper(dataEntityWrapper);
    }

    /**
     * Loads chart settings and applies them to this chart.
     * <p>
     * This method initializes limit lines, scales the chart, and applies settings.
     * It configures all visual and behavioral aspects of the chart according to
     * the provided settings.
     *
     * @param chart The chart settings to apply
     */
    public void loadChartSettings(DataEntityLineChart chart) {
        limitLinesBoundaries.initLimitLines(paletteColorDeterminer);
        scaler.setLimitLinesBoundaries(limitLinesBoundaries);
        scaler.scale(chart);
        settings.setLimitLinesBoundaries(limitLinesBoundaries);

        settings.setChartSettingsFor(chart);
    }

    /**
     * Gets the palette color determiner for this chart.
     * <p>
     * The palette color determiner provides consistent colors for chart elements
     * based on the type of data being visualized.
     *
     * @return The PaletteColorDeterminer for this chart
     */
    public PaletteColorDeterminer getPaletteColorDeterminer() {
        return paletteColorDeterminer;
    }

}
