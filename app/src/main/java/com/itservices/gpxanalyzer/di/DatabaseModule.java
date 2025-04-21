package com.itservices.gpxanalyzer.di;

import android.content.Context;

import androidx.room.Room;

import com.itservices.gpxanalyzer.data.provider.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Dagger Hilt module responsible for providing database-related dependencies, specifically the AppDatabase instance.
 * This module is installed in the SingletonComponent, ensuring that the provided database instance is a singleton
 * throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provides a singleton instance of the {@link AppDatabase}.
     * This method configures and builds the Room database instance for the application.
     * It uses the application context to create the database and sets up fallback to destructive migration,
     * which means the database will be cleared and recreated if schema migrations are not provided.
     *
     * @param context The application context, injected by Hilt.
     * @return A singleton instance of {@link AppDatabase}.
     */
    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class, 
                "gpx-analyzer-database")
                .fallbackToDestructiveMigration()
                .build();
    }
} 