package com.itservices.gpxanalyzer.data.statistics;

import static com.itservices.gpxanalyzer.data.statistics.TrendType.Constants.DEFAULT_FILL_COLOR_ALPHA;

import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

public enum TrendType {
    UP(20, ColorUtil.rgb(0.0f, 0.96f, 0.0f), DEFAULT_FILL_COLOR_ALPHA),
    CONSTANT(5, ColorUtil.rgb(0.96f, 0.96f, 0.96f), DEFAULT_FILL_COLOR_ALPHA),
    DOWN(20, ColorUtil.rgb(0.96f, 0.0f, 0.0f), DEFAULT_FILL_COLOR_ALPHA);

    public static class Constants {
        public static final int DEFAULT_FILL_COLOR_ALPHA = (int) (0.3f * 255.0f);
    }

    private final float threshold; // [m]
    private final int fillColor;
    private final int fillAlpha;

    TrendType(float threshold, int fillColor, int fillAlpha) {
        this.threshold = threshold;
        this.fillColor = fillColor;
        this.fillAlpha = fillAlpha;
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
}
