package com.itservices.gpxanalyzer.chart.palette;

/**
 * Defines the direction in which to read colors from a color palette.
 * <p>
 * This enum controls how colors are mapped to data values when creating
 * visualizations. The direction determines whether higher data values
 * correspond to colors at the beginning or end of the palette.
 */
public enum PaletteDirection {
    /**
     * Maps minimum data values to colors at the top of the palette (index 0).
     * <p>
     * With this direction, colors are read from top to bottom as values increase.
     */
    MIN_IS_ZERO_INDEX_Y_PIXEL,
    
    /**
     * Maps maximum data values to colors at the top of the palette (index 0).
     * <p>
     * With this direction, colors are read from bottom to top as values increase.
     */
    MAX_IS_ZERO_INDEX_Y_PIXEL
}
