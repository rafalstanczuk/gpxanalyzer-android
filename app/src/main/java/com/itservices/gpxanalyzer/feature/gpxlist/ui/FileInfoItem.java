package com.itservices.gpxanalyzer.feature.gpxlist.ui;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;

/**
 * Represents an item in the file list UI, combining a {@link GpxFileInfo} object
 * with its index within the displayed list.
 *
 * @param index    The zero-based index of this item in the list.
 * @param fileInfo The underlying {@link GpxFileInfo} data object containing metadata about the GPX file.
 */
public record FileInfoItem(int index,
                           GpxFileInfo fileInfo
                           ) {
}
