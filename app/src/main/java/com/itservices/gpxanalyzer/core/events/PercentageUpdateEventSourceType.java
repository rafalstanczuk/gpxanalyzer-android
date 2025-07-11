package com.itservices.gpxanalyzer.core.events;

import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.GpxFileInfoProvider;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.file.DeviceStorageSearchedFileProvider;
import com.itservices.gpxanalyzer.feature.gpxchart.data.provider.file.GpxFileDataEntityProvider;
import com.itservices.gpxanalyzer.core.data.provider.geocoding.BaseGeocodingRepository;
import com.itservices.gpxanalyzer.core.ui.components.miniature.GpxFileInfoMiniatureProvider;

/**
 * Enumerates the possible sources of a percentage progress update event ({@link EventProgress}).
 * Each enum constant is associated with a specific class that typically originates the progress updates.
 * This allows filtering progress events based on the operation they represent.
 */
public enum PercentageUpdateEventSourceType {
    /**
     * Indicates progress related to processing GPX file data entities, typically handled by {@link GpxFileDataEntityProvider}.
     */
    GPX_FILE_DATA_ENTITY_PROVIDER(GpxFileDataEntityProvider.class),
    /**
     * Indicates progress related to searching for files on device storage, typically handled by {@link DeviceStorageSearchedFileProvider}.
     */
    STORAGE_SEARCH_PROGRESS(DeviceStorageSearchedFileProvider.class),
    /**
     * Indicates progress related to generating GPX file info miniatures, typically handled by {@link GpxFileInfoMiniatureProvider}.
     */
    MINIATURE_GENERATION_PROGRESS(GpxFileInfoMiniatureProvider.class),

    GEOCODING_PROCESSING(BaseGeocodingRepository.class),
    UPDATING_RESOURCES_PROCESSING(GpxFileInfoProvider.class),
    /**
     * Represents an unknown or unspecified source for the progress update.
     */
    UNKNOWN_SOURCE(null);

    private final Class<?> clazz;

    /**
     * Constructor for the enum constants.
     *
     * @param clazz The class associated with this source type, or null for {@link #UNKNOWN_SOURCE}.
     */
    PercentageUpdateEventSourceType(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Gets the class associated with this source type.
     *
     * @return The associated class, or null if none (for {@link #UNKNOWN_SOURCE}).
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Finds the {@link PercentageUpdateEventSourceType} corresponding to a given class.
     * Iterates through the enum values and compares their associated class with the provided class.
     *
     * @param clazz The class to find the corresponding source type for.
     * @return The matching {@link PercentageUpdateEventSourceType}, or {@link #UNKNOWN_SOURCE} if no match is found.
     */
    public static PercentageUpdateEventSourceType create(Class<?> clazz) {
        for (PercentageUpdateEventSourceType type : values()) {
            if (type.getClazz().equals(clazz)) {
                return type;
            }
        }

        return UNKNOWN_SOURCE;
    }

}
