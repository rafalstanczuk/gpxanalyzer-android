package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;

import java.util.List;

public interface ViewModeMapper {
    void init(List<String> nameUnitList);

    int mapToPrimaryKeyIndexList(Enum<?> viewMode);
}
