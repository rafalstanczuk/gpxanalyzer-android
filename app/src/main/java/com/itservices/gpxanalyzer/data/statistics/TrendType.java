package com.itservices.gpxanalyzer.data.statistics;

import static com.itservices.gpxanalyzer.data.statistics.TrendType.Constants.DEFAULT_FILL_COLOR_ALPHA;

import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

public enum TrendType {
    UP(20, ColorUtil.rgb(0.0f, 0.96f, 0.0f), 255, R.drawable.ic_trending_up_fill0),
    CONSTANT(5, ColorUtil.rgb(0.96f, 0.96f, 0.96f), DEFAULT_FILL_COLOR_ALPHA, R.drawable.ic_trending_flat_fill0),
    DOWN(20, ColorUtil.rgb(0.96f, 0.0f, 0.0f), 255, R.drawable.ic_trending_down_fill0);

    public static class Constants {
        public static final int DEFAULT_FILL_COLOR_ALPHA = (int) (0.3f * 255.0f);
    }

    private final float threshold; // [m]
    private final int fillColor;
    private final int fillAlpha;

    @DrawableRes
    private final int drawableId;

    TrendType(float threshold, int fillColor, int fillAlpha, @DrawableRes int drawableId) {
        this.threshold = threshold;
        this.fillColor = fillColor;
        this.fillAlpha = fillAlpha;
        this.drawableId = drawableId;
    }

    public float getThreshold() {
        return threshold;
    }

    public int getFillColor() {
        return fillColor;
    }

    public int getFillAlpha() {
        return fillAlpha;
    }

    @DrawableRes
    public int getDrawableId() {
        return drawableId;
    }
}
