package com.itservices.gpxanalyzer.di;

import com.google.gson.Gson;
import com.itservices.gpxanalyzer.data.network.AltitudeService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AltitudeModule {
    @Provides
    public AltitudeService provideAltitudeService(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(AltitudeService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava2 adapter
                .build()
                .create(AltitudeService.class);
    }
}