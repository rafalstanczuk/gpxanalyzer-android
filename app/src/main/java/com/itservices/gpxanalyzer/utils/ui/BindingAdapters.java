package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    public static final float CHART_PERCENTAGE_HEIGHT_LANDSCAPE = 90f;
    public static final float CHART_PERCENTAGE_HEIGHT_PORTRAIT = 80f;
    public static final float DEFAULT_MAX_100_PERCENT = 100f;
    public static final float DEFAULT_FLOAT_RELATIVE_PERCENT_VALUE = 1.0f;

    @BindingAdapter("layout_constraintHeight_percent")
    public static void setLayoutConstraintHeightPercent(View view, float percentHeight) {

        ConstraintLayout layout = (ConstraintLayout) view.getParent();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.constrainPercentHeight(view.getId(), percentHeight);
        constraintSet.applyTo(layout);
    }
}
