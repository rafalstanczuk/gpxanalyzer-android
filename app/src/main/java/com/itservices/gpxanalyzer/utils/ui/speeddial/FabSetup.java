package com.itservices.gpxanalyzer.utils.ui.speeddial;

import androidx.annotation.DrawableRes;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public record FabSetup(FloatingActionButton floatingActionButton, @DrawableRes int iconResId,
                       int actionId) {
}