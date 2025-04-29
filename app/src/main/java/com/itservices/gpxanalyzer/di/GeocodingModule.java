package com.itservices.gpxanalyzer.di;

import com.itservices.gpxanalyzer.BuildConfig;
import com.itservices.gpxanalyzer.data.network.GeocodingService;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingApiKey;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingApiKeys;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingNetworkRepository;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingNetworkRouterRepository;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingRequestQueue;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Dagger module for providing geocoding-related dependencies.
 */
@Module
@InstallIn(SingletonComponent.class)
public class GeocodingModule {

    private static final boolean ENABLE_LOGGING = true; // Disable logging for production

    /**
     * Provides a list of Maps.co API keys.
     * In a real application, these should come from a secure source
     * such as BuildConfig or encrypted storage.
     */
    @Provides
    @Singleton
    @GeocodingApiKeys
    public List<String> provideGeocodingApiKeys() {
        List<String> apiKeys = Arrays.asList(
            BuildConfig.MAPS_CO_API_KEY_1,
            BuildConfig.MAPS_CO_API_KEY_2,
            BuildConfig.MAPS_CO_API_KEY_3,
            BuildConfig.MAPS_CO_API_KEY_4,
            BuildConfig.MAPS_CO_API_KEY_5,
            BuildConfig.MAPS_CO_API_KEY_6,
            BuildConfig.MAPS_CO_API_KEY_7,
            BuildConfig.MAPS_CO_API_KEY_8,
            BuildConfig.MAPS_CO_API_KEY_9,
            BuildConfig.MAPS_CO_API_KEY_10
        );

        // Filter out empty keys
        List<String> validKeys = apiKeys.stream()
                .filter(key -> key != null && !key.isEmpty())
                .collect(Collectors.toList());

        return validKeys;
    }

    /**
     * Provides the first API key for backward compatibility.
     */
    @Provides
    @Singleton
    @GeocodingApiKey
    public String provideGeocodingApiKey(@GeocodingApiKeys List<String> apiKeys) {
        return apiKeys.get(0);
    }

    /**
     * Provides the Retrofit service for geocoding.
     */
    @Provides
    public GeocodingService provideGeocodingService(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(GeocodingService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(GeocodingService.class);
    }

    /**
     * Provides the original repository for backward compatibility.
     */
    @Provides
    public GeocodingNetworkRepository provideGeocodingRepository(
            GeocodingService geocodingService,
            @GeocodingApiKey String apiKey,
            GeocodingRequestQueue requestQueue) {
        return new GeocodingNetworkRepository(geocodingService, apiKey, requestQueue);
    }

    /**
     * Provides the router repository for distributed geocoding.
     */
    @Provides
    @Singleton
    public GeocodingNetworkRouterRepository provideGeocodingRouterRepository(
            GlobalEventWrapper events,
            @GeocodingApiKeys List<String> apiKeys) {
        List<GeocodingNetworkRepository> repositories = new ArrayList<>();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(apiKeys.size());
        Scheduler scheduler = Schedulers.from(threadPoolExecutor);
        
        for (String apiKey : apiKeys) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

/*            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);*/

            // Create a new GeocodingService instance for each API key
            GeocodingService geocodingService = new Retrofit.Builder()
                    .baseUrl(GeocodingService.BASE_URL)
                    .client(clientBuilder
                            .build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(GeocodingService.class);
            
            // Create a new repository with its own service instance and request queue
            repositories.add(new GeocodingNetworkRepository(
                geocodingService, 
                apiKey,
                new GeocodingRequestQueue()
            ));
        }
        
        return new GeocodingNetworkRouterRepository(events, repositories, scheduler);
    }
}