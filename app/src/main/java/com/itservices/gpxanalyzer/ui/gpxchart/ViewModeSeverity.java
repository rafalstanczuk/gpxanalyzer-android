package com.itservices.gpxanalyzer.ui.gpxchart;


import android.view.View;

import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;

public enum ViewModeSeverity {
	TWO_CHARTS(R.drawable.ic_splitscreen_two_selector, 0.4f, 0.4f, 2),
	ONE_CHART(R.drawable.ic_fullscreen_one_selector, 0.8f, 0.8f, 1);


	@DrawableRes
    private final int drawableIconResId;

	private final float percentageHeightPortrait;
    private final float percentageHeightLandscape;
	private final int count;


    ViewModeSeverity(@DrawableRes int drawableIconResId, float percentageHeightPortrait, float percentageHeightLandscape, int count){
        this.drawableIconResId = drawableIconResId;
        this.percentageHeightPortrait = percentageHeightPortrait;
        this.percentageHeightLandscape = percentageHeightLandscape;
        this.count = count;
    }

	public ViewModeSeverity getNextCyclic() {
		int currOrdinal  = ordinal();
		int maxOrdinal = values().length-1;

		return currOrdinal==maxOrdinal ? values()[0] : values()[currOrdinal+1];
	}

	public int getDrawableIconResId() {
		return drawableIconResId;
	}

	public int isMoreThanOneVisibility() {
		return ( this != ViewModeSeverity.ONE_CHART) ? View.VISIBLE : View.GONE;
	}

	public float getPercentageHeightPortrait() {
		return percentageHeightPortrait;
	}

	public float getPercentageHeightLandscape() {
		return percentageHeightLandscape;
	}

	public int getCount() {
		return count;
	}
}
