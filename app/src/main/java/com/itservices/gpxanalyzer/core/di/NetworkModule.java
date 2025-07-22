package com.itservices.gpxanalyzer.core.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // Can be toggled manually or controlled via a flavor/build type variable
    private static final boolean ENABLE_LOGGING = false; // Disable logging for production

    @Provides
    public OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        // Add logging interceptor for development builds
        if (ENABLE_LOGGING) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);
        }
        // Add other interceptors, timeouts etc. here if needed
        return clientBuilder.build();
    }

    @Provides
    public Gson provideGson() {
        return new GsonBuilder().create(); // Configure Gson here if needed
    }
} 