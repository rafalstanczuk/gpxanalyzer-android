package com.itservices.gpxanalyzer.data.provider;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfoParser;
import com.itservices.gpxanalyzer.data.provider.db.gpxfileinfo.GpxFileInfoRepository;
import com.itservices.gpxanalyzer.data.provider.file.GpxFileValidator;
import com.itservices.gpxanalyzer.data.provider.file.DeviceStorageSearchedFileProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class GpxFileInfoProvider {
    public static final String GPX_FILE_EXTENSION = ".gpx";
    private static final String[] MEDIA_STORE_SELECTION_ARGS = new String[]{"application/gpx+xml", "text/xml", "%.gpx"};

    @Inject
    GpxFileInfoParser parser;

    @Inject
    DeviceStorageSearchedFileProvider deviceStorage;

    private final GpxFileInfoRepository repository;

    @Inject
    public GpxFileInfoProvider(GpxFileInfoRepository repository) {
        this.repository = repository;
    }

    public Single<List<GpxFileInfo>> getAllGpxFilesFromDb() {
        return repository.getAll();
    }

    public Single<List<GpxFileInfo>> searchAndParseGpxFilesRecursively(Context context) {
        return deviceStorage.searchAndParseFilesRecursively(
                        context, file -> parser.parse(file),
                        GPX_FILE_EXTENSION, MEDIA_STORE_SELECTION_ARGS
                )
                .map(newParsedFileList -> {
                    List<GpxFileInfo> newGpxFileList = new ArrayList<>();
                    newParsedFileList.forEach(parsedFile -> newGpxFileList.add((GpxFileInfo) parsedFile));
                    return newGpxFileList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    private Completable insertIntoDb(List<GpxFileInfo> gpxFileInfoList) {
        //Log.d(GpxFileInfoProvider.class.getSimpleName(), "insertIntoDb() called with: gpxFileInfoList = [" + gpxFileInfoList + "]");
        return repository.insertAll(gpxFileInfoList);
    }

    public Single<List<GpxFileInfo>> getAndFilterGpxFiles() {
        return getAllGpxFilesFromDb()
                .map(GpxFileValidator::validateAndFilterFiles);
    }

    public Completable replaceAll(List<GpxFileInfo> gpxFileInfoList) {
        return repository.deleteAll()
                .andThen(insertIntoDb(gpxFileInfoList));
    }
}
