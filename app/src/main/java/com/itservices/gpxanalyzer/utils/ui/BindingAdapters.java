package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    @BindingAdapter("height_percent")
    public static void setHeightPercent(final View view, final Float percent) {
        if (percent == null) return;
        view.post(() -> {
            int baseHeight = 0;
            // Try the immediate parent's height
            View parent = (View) view.getParent();
            if (parent != null && parent.getHeight() > 0) {
                baseHeight = parent.getHeight();
            } else {
                // Fallback: try the root view's height
                View root = view.getRootView();
                if (root != null && root.getHeight() > 0) {
                    baseHeight = root.getHeight();
                } else {
                    // Final fallback: use the device screen height
                    baseHeight = view.getContext().getResources().getDisplayMetrics().heightPixels;
                }
            }
            int newHeight = (int) (baseHeight * percent);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = newHeight;
            view.setLayoutParams(params);
        });
    }

}
