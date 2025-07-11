package com.itservices.gpxanalyzer.domain.cumulative;

import static com.itservices.gpxanalyzer.domain.cumulative.TrendType.Constants.DEFAULT_FILL_COLOR_ALPHA;

import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.core.utils.ui.ColorUtil;

public enum TrendType {
    UP(ColorUtil.rgb(0.0f, 0.96f, 0.0f), 255, R.drawable.ic_trending_up_fill0),
    CONSTANT(ColorUtil.rgb(0.96f, 0.96f, 0.96f), DEFAULT_FILL_COLOR_ALPHA, R.drawable.ic_trending_flat_fill0),
    DOWN(ColorUtil.rgb(0.96f, 0.0f, 0.0f), 255, R.drawable.ic_trending_down_fill0);

    public static class Constants {
        public static final int DEFAULT_FILL_COLOR_ALPHA = (int) (0.3f * 255.0f);
    }

    private final int fillColor;
    private final int fillAlpha;

    @DrawableRes
    private final int drawableId;

    TrendType(int fillColor, int fillAlpha, @DrawableRes int drawableId) {
        this.fillColor = fillColor;
        this.fillAlpha = fillAlpha;
        this.drawableId = drawableId;
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
