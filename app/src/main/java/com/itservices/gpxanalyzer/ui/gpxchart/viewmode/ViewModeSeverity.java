package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;


import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;

/**
 * Enumerates different severity or display modes for the chart area list.
 * This primarily controls how many charts are displayed simultaneously (e.g., one full-screen chart, two split-screen charts).
 * It provides associated resources (icons) and logic for layout calculations (percentage height).
 */
public enum ViewModeSeverity {
    /** Mode displaying a single chart, taking up the full available area. */
    ONE_CHART(R.drawable.ic_fullscreen_one_selector, 1),
    /** Mode displaying two charts, typically splitting the available area. */
	TWO_CHARTS(R.drawable.ic_splitscreen_two_selector, 2);


    /** Drawable resource ID for the icon representing this severity mode. */
    @DrawableRes
    private final int drawableIconResId;

    /** The number of charts displayed in this severity mode. */
    private final int count;


    /**
     * Enum constructor.
     *
     * @param drawableIconResId Drawable resource ID for the icon.
     * @param count             The number of charts associated with this mode.
     */
    ViewModeSeverity(@DrawableRes int drawableIconResId, int count) {
        this.drawableIconResId = drawableIconResId;
        this.count = count;
    }

    /**
     * Gets the next severity mode in the enum sequence, cycling back to the first one if currently at the last.
     *
     * @return The next {@link ViewModeSeverity}.
     */
    public ViewModeSeverity getNextCyclic() {
        int currOrdinal = ordinal();
        int maxOrdinal = values().length - 1;

        return currOrdinal == maxOrdinal ? values()[0] : values()[currOrdinal + 1];
    }

    /**
     * Gets the drawable resource ID for the icon associated with this severity mode.
     *
     * @return The drawable resource ID.
     */
    public int getDrawableIconResId() {
        return drawableIconResId;
    }

    /**
     * Calculates the percentage height each chart should occupy in portrait orientation for this mode.
     * Assumes equal distribution among the charts.
     *
     * @return The height percentage (e.g., 0.5f for TWO_CHARTS).
     */
    public float getPercentageHeightPortrait() {
        return 1f / (float) count;
    }

    /**
     * Calculates the percentage height each chart should occupy in landscape orientation for this mode.
     * Assumes equal distribution among the charts. (Currently same logic as portrait).
     *
     * @return The height percentage (e.g., 0.5f for TWO_CHARTS).
     */
    public float getPercentageHeightLandscape() {
        return 1f / (float) count;
    }

    /**
     * Gets the number of charts associated with this severity mode.
     *
     * @return The count of charts (e.g., 1 for ONE_CHART, 2 for TWO_CHARTS).
     */
    public int getCount() {
        return count;
    }
}
