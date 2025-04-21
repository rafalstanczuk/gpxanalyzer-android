package com.itservices.gpxanalyzer.data.provider;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfoParser;
import com.itservices.gpxanalyzer.data.provider.db.AppDatabase;
import com.itservices.gpxanalyzer.data.provider.db.Converters;
import com.itservices.gpxanalyzer.data.provider.db.GpxFileInfoEntity;
import com.itservices.gpxanalyzer.data.provider.db.GpxFileInfoMapper;
import com.itservices.gpxanalyzer.data.provider.file.GpxFileValidator;
import com.itservices.gpxanalyzer.data.provider.file.DeviceStorageSearchedFileProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class GpxFileInfoProvider {
    public static final String GPX_FILE_EXTENSION = ".gpx";
    private static final String[] MEDIA_STORE_SELECTION_ARGS = new String[]{"application/gpx+xml", "text/xml", "%.gpx"};

    @Inject
    GpxFileInfoParser parser;

    @Inject
    DeviceStorageSearchedFileProvider deviceStorage;

    private final AppDatabase appDatabase;

    @Inject
    public GpxFileInfoProvider(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    public Single<List<GpxFileInfo>> getAllGpxFilesFromDb() {
        return appDatabase
                .gpxFileInfoDao()
                .getAll()
                .map(GpxFileInfoMapper::fromEntityList);
    }

    public Single<List<GpxFileInfo>> searchAndParseGpxFilesRecursively(Context context) {
        return deviceStorage.searchAndParseFilesRecursively(
                        context, file -> parser.parse(file),
                        GPX_FILE_EXTENSION, MEDIA_STORE_SELECTION_ARGS
                )
                .map(newParsedFileList -> {
                    List<GpxFileInfo> newGpxFileList = new ArrayList<>();
                    newParsedFileList.forEach(parsedFile -> newGpxFileList.add((GpxFileInfo) parsedFile));
                    //gpxFileInfoList = newGpxFileList;
                    return newGpxFileList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    private Completable insertIntoDb(List<GpxFileInfo> gpxFileInfoList) {
        Log.d(GpxFileInfoProvider.class.getSimpleName(), "insertIntoDb() called with: gpxFileInfoList = [" + gpxFileInfoList + "]");

        List<GpxFileInfoEntity> gpxFileInfoEntityList = GpxFileInfoMapper.toEntityList(gpxFileInfoList);
        return insertAllGpxFiles(gpxFileInfoEntityList);
    }

    public Completable insertAllGpxFiles(List<GpxFileInfoEntity> gpxFileInfos) {
        return appDatabase.gpxFileInfoDao().insertAll(gpxFileInfos);
    }

    public Single<List<GpxFileInfo>> getAndFilterGpxFiles() {
        return getAllGpxFilesFromDb()
                .map(GpxFileValidator::validateAndFilterFiles);
    }


    public Single<GpxFileInfoEntity> getGpxFileById(long id) {
        return appDatabase.gpxFileInfoDao().getById(id);
    }

    public Single<GpxFileInfoEntity> getGpxFileByPath(String absolutePath) {
        return appDatabase
                .gpxFileInfoDao()
                .getByBase64AbsolutePath(Converters.toBase64(absolutePath));
    }

    public Completable insertGpxFile(GpxFileInfoEntity gpxFileInfo) {
        return appDatabase.gpxFileInfoDao().insert(gpxFileInfo);
    }


    public Completable updateGpxFile(GpxFileInfo gpxFileInfo) {
        return appDatabase.gpxFileInfoDao()
                .update(GpxFileInfoMapper.toEntity(gpxFileInfo));
    }

    public Completable deleteGpxFile(GpxFileInfoEntity gpxFileInfo) {
        return appDatabase.gpxFileInfoDao().delete(gpxFileInfo);
    }

    public Completable deleteAllGpxFiles() {
        return appDatabase.gpxFileInfoDao().deleteAll()
                .subscribeOn(Schedulers.io());
    }

    public Completable replaceAll(List<GpxFileInfo> gpxFileInfoList) {
        return deleteAllGpxFiles()
                .andThen(insertIntoDb(gpxFileInfoList));
    }
}
