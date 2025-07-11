package com.itservices.gpxanalyzer.core.di;

import android.content.Context;

import androidx.room.Room;

import com.itservices.gpxanalyzer.core.data.provider.db.gpxfileinfo.GpxFileInfoDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Dagger Hilt module responsible for providing database-related dependencies, specifically the GpxFileInfoDatabase instance.
 * This module is installed in the SingletonComponent, ensuring that the provided database instance is a singleton
 * throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent.class)
public class GpxFileInfoDatabaseModule {

    private static final String GPX_ANALYZER_DATABASE = "gpx-analyzer-database";

    /**
     * Provides a singleton instance of the {@link GpxFileInfoDatabase}.
     * This method configures and builds the Room database instance for the application.
     * It uses the application context to create the database and sets up fallback to destructive migration,
     * which means the database will be cleared and recreated if schema migrations are not provided.
     *
     * @param context The application context, injected by Hilt.
     * @return A singleton instance of {@link GpxFileInfoDatabase}.
     */
    @Provides
    @Singleton
    public GpxFileInfoDatabase provideGpxFileInfoDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                GpxFileInfoDatabase.class,
                        GPX_ANALYZER_DATABASE)
                .fallbackToDestructiveMigration()
                .build();
    }
} 