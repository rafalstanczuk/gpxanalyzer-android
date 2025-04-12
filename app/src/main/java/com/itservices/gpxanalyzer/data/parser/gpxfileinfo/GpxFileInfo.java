package com.itservices.gpxanalyzer.data.parser.gpxfileinfo;

import java.io.File;
import java.util.Date;

public record GpxFileInfo(
        File file,
        String creator,
        String authorName,
        String firstPointLat,
        String firstPointLon,
        String firstPointEle,
        long firstPointTimeMillis,
        long fileSize,
        Date lastFileModified
) {
    public GpxFileInfo(File file, String creator, String authorName,
                       String firstPointLat, String firstPointLon,
                       String firstPointEle, long firstPointTimeMillis) {
        this(file, creator, authorName, firstPointLat, firstPointLon,
                firstPointEle, firstPointTimeMillis, file.length(), new Date(file.lastModified()));
    }
}
