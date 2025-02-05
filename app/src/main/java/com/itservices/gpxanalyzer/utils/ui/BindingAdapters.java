package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    @BindingAdapter("layout_constraintHeight_percent")
    public static void setLayoutConstraintHeightPercent(View view, float percentHeight) {

        ConstraintLayout layout = (ConstraintLayout) view.getParent();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.constrainPercentHeight(view.getId(), percentHeight);
        constraintSet.applyTo(layout);
        layout.invalidate();
    }
}
