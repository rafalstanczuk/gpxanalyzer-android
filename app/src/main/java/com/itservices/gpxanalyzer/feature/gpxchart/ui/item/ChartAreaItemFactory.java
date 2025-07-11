package com.itservices.gpxanalyzer.feature.gpxchart.ui.item;

import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewMode;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

/**
 * Factory interface for creating ChartAreaItem instances.
 * This interface leverages Dagger's AssistedFactory to create ChartAreaItem objects
 * with both runtime parameters and dependency-injected components.
 * 
 * The factory pattern abstracts the creation logic and enables flexibility in chart
 * configuration while maintaining proper dependency injection.
 */
@AssistedFactory
public interface ChartAreaItemFactory {
    
    /**
     * Creates a new ChartAreaItem instance with the specified configuration.
     * 
     * @param viewMode The view mode that determines which GPX data aspect to visualize
     * @param drawX Whether to draw X-axis labels on the chart
     * @param drawIconsEnabled Whether to enable drawing of icons on the chart
     * @return A new ChartAreaItem instance configured with the specified parameters
     */
    ChartAreaItem create(
            GpxViewMode viewMode,
            @Assisted("drawX") boolean drawX,
            @Assisted("drawIconsEnabled") boolean drawIconsEnabled
    );
}