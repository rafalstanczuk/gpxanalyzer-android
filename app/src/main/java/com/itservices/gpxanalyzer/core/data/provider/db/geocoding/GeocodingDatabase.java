package com.itservices.gpxanalyzer.core.data.provider.db.geocoding;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database class for geocoding results.
 * Provides access to the database and its DAOs.
 */
@Database(
    entities = {GeocodingResultEntity.class},
    version = 1,
    exportSchema = true
)
public abstract class GeocodingDatabase extends RoomDatabase {
    public abstract GeocodingResultDao geocodingResultDao();
} 