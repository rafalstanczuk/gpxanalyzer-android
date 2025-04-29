package com.itservices.gpxanalyzer.di;

import android.content.Context;

import com.itservices.gpxanalyzer.data.provider.db.geocoding.GeocodingDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class GeocodingDatabaseModule {
    private static final String DATABASE_NAME = "geocoding_database";

    @Provides
    @Singleton
    public GeocodingDatabase provideGeocodingDatabase(@ApplicationContext Context context) {
        return androidx.room.Room.databaseBuilder(
                context,
                GeocodingDatabase.class,
                DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build();
    }
} 