package com.itservices.gpxanalyzer.core.data.provider.db.gpxfileinfo;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {GpxFileInfoEntity.class}, version = 1, exportSchema = true)
@TypeConverters(Converters.class)
public abstract class GpxFileInfoDatabase extends RoomDatabase {
    public abstract GpxFileInfoDao gpxFileInfoDao();
} 