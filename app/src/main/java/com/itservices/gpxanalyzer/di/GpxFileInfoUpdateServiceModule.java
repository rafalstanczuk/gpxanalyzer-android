package com.itservices.gpxanalyzer.di;

import com.itservices.gpxanalyzer.domain.service.GpxFileInfoUpdateService;
import com.itservices.gpxanalyzer.domain.service.GpxFileInfoUpdateServiceImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class GpxFileInfoUpdateServiceModule {
    
    @Provides
    @Singleton
    public GpxFileInfoUpdateService provideGpxFileUpdateService(
            GpxFileInfoUpdateServiceImpl impl) {
        return impl;
    }
} 