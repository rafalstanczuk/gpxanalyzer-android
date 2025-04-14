package com.itservices.gpxanalyzer.data.provider.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {GpxFileInfoEntity.class}, version = 1, exportSchema = true)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GpxFileInfoDao gpxFileInfoDao();
} 