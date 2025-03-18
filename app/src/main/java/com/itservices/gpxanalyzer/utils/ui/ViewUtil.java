package com.itservices.gpxanalyzer.utils.ui;

import android.view.View;

public class ViewUtil {
    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
