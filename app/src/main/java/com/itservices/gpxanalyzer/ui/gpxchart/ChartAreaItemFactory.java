package com.itservices.gpxanalyzer.ui.gpxchart;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface ChartAreaItemFactory {
    ChartAreaItem create(
            ViewMode viewMode,
            @Assisted("drawX") boolean drawX,
            @Assisted("drawIconsEnabled") boolean drawIconsEnabled
    );
}