package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;

/**
 * Defines custom Binding Adapters for use in XML layouts with Android Data Binding.
 */
public class BindingAdapters {

    /**
     * A custom Binding Adapter that allows setting a View's height as a percentage of a base height.
     * The base height is determined by trying the following in order:
     * <ol>
     *     <li>The height of the immediate parent View.</li>
     *     <li>The height of the root View of the layout.</li>
     *     <li>The height of the device screen.</li>
     * </ol>
     * The calculation and setting of the height are posted to the View's message queue
     * to ensure that the parent/root view heights are likely measured before execution.
     *
     * Usage in XML:
     * <pre>{@code
     * <View
     *     ...
     *     app:height_percent="@{0.5f}"
     *     ...
     *     />
     * }</pre>
     *
     * @param view    The View whose height needs to be set.
     * @param percent The desired height as a fraction of the base height (e.g., 0.5f for 50%).
     *                If null, the adapter does nothing.
     */
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
