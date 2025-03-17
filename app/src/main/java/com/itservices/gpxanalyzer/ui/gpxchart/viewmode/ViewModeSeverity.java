package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;


import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;

public enum ViewModeSeverity {
    ONE_CHART(R.drawable.ic_fullscreen_one_selector, 1),
	TWO_CHARTS(R.drawable.ic_splitscreen_two_selector, 2);


    @DrawableRes
    private final int drawableIconResId;

    private final int count;


    ViewModeSeverity(@DrawableRes int drawableIconResId, int count) {
        this.drawableIconResId = drawableIconResId;
        this.count = count;
    }

    public ViewModeSeverity getNextCyclic() {
        int currOrdinal = ordinal();
        int maxOrdinal = values().length - 1;

        return currOrdinal == maxOrdinal ? values()[0] : values()[currOrdinal + 1];
    }

    public int getDrawableIconResId() {
        return drawableIconResId;
    }

    public float getPercentageHeightPortrait() {
        return 0.9f / (float) count;
    }

    public float getPercentageHeightLandscape() {
        return 0.9f / (float) count;
    }

    public int getCount() {
        return count;
    }
}
