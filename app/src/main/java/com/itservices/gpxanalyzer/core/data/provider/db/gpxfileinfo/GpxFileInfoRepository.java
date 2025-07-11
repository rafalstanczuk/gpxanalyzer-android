package com.itservices.gpxanalyzer.core.data.provider.db.gpxfileinfo;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository class for handling GpxFileInfo data operations.
 * Provides a clean API for accessing and managing GpxFileInfo data.
 */
@Singleton
public class GpxFileInfoRepository {
    private final GpxFileInfoDao gpxFileInfoDao;

    @Inject
    public GpxFileInfoRepository(GpxFileInfoDatabase database) {
        this.gpxFileInfoDao = database.gpxFileInfoDao();
    }

    /**
     * Inserts a GpxFileInfo into the database.
     *
     * @param gpxFileInfo The GpxFileInfo to insert
     * @return Completable that completes when the operation is done
     */
    public Completable insert(GpxFileInfo gpxFileInfo) {
        return Single.fromCallable(() -> GpxFileInfoMapper.toEntity(gpxFileInfo))
                .flatMapCompletable(gpxFileInfoDao::insert)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Inserts multiple GpxFileInfo objects into the database.
     *
     * @param gpxFileInfos The list of GpxFileInfo objects to insert
     * @return Completable that completes when the operation is done
     */
    public Completable insertAll(List<GpxFileInfo> gpxFileInfos) {
        return Single.fromCallable(() -> gpxFileInfos.stream()
                .map(GpxFileInfoMapper::toEntity)
                .collect(Collectors.toCollection(ArrayList::new)))
                .flatMapCompletable(gpxFileInfoDao::insertAll)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gets all GpxFileInfo objects from the database.
     *
     * @return Single that emits lists of GpxFileInfo objects
     */
    public Single<List<GpxFileInfo>> getAll() {
        return gpxFileInfoDao.getAll()
                .map(entities -> entities.stream()
                        .map(GpxFileInfoMapper::fromEntity)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Updates a GpxFileInfo in the database.
     *
     * @param gpxFileInfo The GpxFileInfo to update
     * @return Completable that completes when the operation is done
     */
    public Completable update(GpxFileInfo gpxFileInfo) {
        return Single.fromCallable(() -> GpxFileInfoMapper.toEntity(gpxFileInfo))
                .flatMapCompletable(gpxFileInfoDao::update)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Deletes all GpxFileInfo objects from the database.
     *
     * @return Completable that completes when the operation is done
     */
    public Completable deleteAll() {
        return gpxFileInfoDao.deleteAll()
                .subscribeOn(Schedulers.io());
    }
} 