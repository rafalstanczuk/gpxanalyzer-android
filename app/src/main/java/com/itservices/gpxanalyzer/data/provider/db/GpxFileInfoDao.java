package com.itservices.gpxanalyzer.data.provider.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;

@Dao
public interface GpxFileInfoDao {
    @Query("SELECT * FROM gpx_files")
    Single<List<GpxFileInfoEntity>> getAll();

    @Query("SELECT * FROM gpx_files WHERE id = :id")
    Single<GpxFileInfoEntity> getById(long id);

    @Insert
    Completable insert(GpxFileInfoEntity gpxFileInfo);

    @Insert
    Completable insertAll(List<GpxFileInfoEntity> gpxFileInfos);

    @Update
    Completable update(GpxFileInfoEntity gpxFileInfo);

    @Delete
    Completable delete(GpxFileInfoEntity gpxFileInfo);

    @Query("DELETE FROM gpx_files")
    Completable deleteAll();
} 