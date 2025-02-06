package com.itservices.gpxanalyzer.ui.main;

public class MenuItem {
    private final String title;
    private int destinationFragment;

    public MenuItem(String title, int destinationFragment) {
        this.title = title;
        this.destinationFragment = destinationFragment;
    }

    public String getTitle() {
        return title;
    }

    public int getDestinationFragment() {
        return destinationFragment;
    }
}