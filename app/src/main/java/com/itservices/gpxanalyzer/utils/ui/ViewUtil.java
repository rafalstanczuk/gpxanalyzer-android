package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;

/**
 * Utility class providing helper methods for common {@link View} operations.
 */
public class ViewUtil {
    /**
     * Sets the visibility of a {@link View} based on a boolean flag.
     * If {@code isVisible} is true, the view's visibility is set to {@link View#VISIBLE}.
     * If {@code isVisible} is false, the view's visibility is set to {@link View#GONE}.
     *
     * @param view      The {@link View} whose visibility needs to be changed.
     * @param isVisible {@code true} to make the view visible, {@code false} to make it gone.
     */
    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
