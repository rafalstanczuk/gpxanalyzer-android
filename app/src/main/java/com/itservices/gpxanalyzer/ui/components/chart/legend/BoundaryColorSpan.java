package com.itservices.gpxanalyzer.ui.components.chart.legend;

/**
 * Represents a color boundary span in a palette for chart visualizations.
 * <p>
 * This record encapsulates information about a specific interval within a color
 * palette, including its value range (min to max) and the corresponding color.
 * BoundaryColorSpan objects are used to map data values to specific colors when
 * rendering charts, enabling color-based visualization of data trends.
 * 
 * @param id Unique identifier for this boundary within its palette
 * @param name Human-readable name for this boundary (typically formatted value range)
 * @param min The minimum value in this boundary's range (inclusive)
 * @param max The maximum value in this boundary's range (exclusive)
 * @param color The integer color value (ARGB) associated with this boundary
 */
public record BoundaryColorSpan(int id, String name, float min, float max, int color) {
}
