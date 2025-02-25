package com.itservices.gpxanalyzer.ui.main.item;

import android.graphics.drawable.Drawable;

public class MenuItem {

    private final Drawable iconId;
    private final String title;
    private final int destinationFragment;

    public MenuItem(Drawable iconId, String title, int destinationFragment) {
        this.iconId = iconId;
        this.title = title;
        this.destinationFragment = destinationFragment;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return iconId;
    }

    public int getDestinationFragment() {
        return destinationFragment;
    }
}