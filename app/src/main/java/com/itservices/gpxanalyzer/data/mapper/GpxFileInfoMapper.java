package com.itservices.gpxanalyzer.data.mapper;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.ui.storage.FileInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GpxFileInfoMapper {
    public static GpxFileInfo mapFrom(FileInfoItem fileInfoItem) {
        return fileInfoItem.fileInfo();
    }

    public static List<GpxFileInfo> mapFrom(List<FileInfoItem> fileInfoItemList) {
        return fileInfoItemList.stream()
                .map(FileInfoItem::fileInfo)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
