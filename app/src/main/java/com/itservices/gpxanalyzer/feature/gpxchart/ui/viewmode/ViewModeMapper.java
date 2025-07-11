package com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode;

import java.util.List;

/**
 * Maps chart view modes to appropriate data indices for visualization.
 * 
 * This interface defines operations for mapping between enumerated view modes
 * (such as altitude, speed, etc.) and the corresponding indices in the data structure
 * that store those measurements. It allows the application to abstract the relationship
 * between UI view modes and the underlying data organization.
 * 
 * Implementations of this interface handle the translation between user-facing
 * visualization modes and the numeric indices used to access specific data measures
 * in the DataEntity objects.
 */
public interface ViewModeMapper {
    /**
     * Initializes the mapper with a list of name-unit pairs for data measures.
     * This method should be called before any mapping operations to configure
     * the mapper with information about available data measures.
     *
     * @param nameUnitList A list of strings representing name-unit pairs for each measure
     */
    void init(List<String> nameUnitList);

    /**
     * Maps a view mode enum value to the corresponding primary key index in the data structure.
     * This allows the application to determine which data index to use for displaying
     * a particular view mode.
     *
     * @param viewMode The view mode enum value to map
     * @return The index in the data structure corresponding to the specified view mode
     */
    int mapToPrimaryKeyIndexList(Enum<?> viewMode);
}
