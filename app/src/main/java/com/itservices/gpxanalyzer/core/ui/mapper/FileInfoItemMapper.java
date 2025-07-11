package com.itservices.gpxanalyzer.core.ui.mapper;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.feature.gpxlist.ui.FileInfoItem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mapper class responsible for converting data models ({@link GpxFileInfo})
 * into UI-specific models ({@link FileInfoItem}).
 */
public class FileInfoItemMapper {


    /**
     * Maps a single {@link GpxFileInfo} object to a {@link FileInfoItem}, associating it with a given index.
     *
     * @param index       The index to associate with the item (e.g., its position in a list).
     * @param gpxFileInfo The source {@link GpxFileInfo} object.
     * @return A new {@link FileInfoItem} containing the index and the original {@code gpxFileInfo}.
     */
    public static FileInfoItem mapFrom(int index, GpxFileInfo gpxFileInfo) {
        return new FileInfoItem(index, gpxFileInfo);
    }


    /**
     * Maps a list of {@link GpxFileInfo} objects to a list of {@link FileInfoItem} objects.
     * Each item in the resulting list is assigned an index corresponding to its position in the input list.
     *
     * @param gpxFileInfoList The list of {@link GpxFileInfo} objects to map.
     * @return A new {@link List} of {@link FileInfoItem} objects.
     */
    public static List<FileInfoItem> mapFrom(List<GpxFileInfo> gpxFileInfoList) {
        return IntStream.range(0, gpxFileInfoList.size())
                .mapToObj(index -> mapFrom(index, gpxFileInfoList.get(index)))
                .collect(Collectors.toList());
    }
}
