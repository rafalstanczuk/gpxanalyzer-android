package com.itservices.gpxanalyzer.core.di;

import com.google.gson.Gson;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaApiService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Dagger Hilt module for Strava API dependencies.
 * Provides Retrofit service configuration for Strava API integration.
 * 
 * Note: Strava has different base URLs for OAuth and API endpoints:
 * - OAuth endpoints: https://www.strava.com/oauth/
 * - API endpoints: https://www.strava.com/api/v3/
 */
@Module
@InstallIn(SingletonComponent.class)
public class StravaModule {

    /**
     * Provides StravaApiService configured with OAuth base URL for token operations.
     * OAuth endpoints (token, authorize) use different base URL than regular API calls.
     * 
     * @param okHttpClient Shared HTTP client with logging and other interceptors
     * @param gson Shared Gson instance for JSON serialization
     * @return Configured StravaApiService instance
     */
    @Provides
    @Singleton
    public StravaApiService provideStravaApiService(OkHttpClient okHttpClient, Gson gson) {
        // Create a custom Retrofit instance that can handle both OAuth and API endpoints
        return new StravaApiServiceImpl(okHttpClient, gson);
    }

    /**
     * Custom implementation that uses different Retrofit instances for OAuth and API calls.
     */
    private static class StravaApiServiceImpl implements StravaApiService {
        private final StravaApiService oauthService;
        private final StravaApiService apiService;

        public StravaApiServiceImpl(OkHttpClient okHttpClient, Gson gson) {
            // OAuth service for token operations
            this.oauthService = new Retrofit.Builder()
                    .baseUrl("https://www.strava.com/oauth/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(StravaApiService.class);

            // API service for data operations
            this.apiService = new Retrofit.Builder()
                    .baseUrl(StravaApiService.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(StravaApiService.class);
        }

        // OAuth endpoints - delegate to OAuth service
        @Override
        public io.reactivex.Single<com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse> exchangeAuthorizationCode(
                com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.AuthorizationCodeRequest request) {
            return oauthService.exchangeAuthorizationCode(request);
        }

        @Override
        public io.reactivex.Single<com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse> refreshAccessToken(
                com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenRefreshRequest request) {
            return oauthService.refreshAccessToken(request);
        }

        @Override
        public io.reactivex.Single<okhttp3.ResponseBody> deauthorize(String authorization) {
            return oauthService.deauthorize(authorization);
        }

        // API endpoints - delegate to API service
        @Override
        public io.reactivex.Single<java.util.List<com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaActivity>> getAthleteActivities(
                String authorization, Integer perPage, Integer page) {
            return apiService.getAthleteActivities(authorization, perPage, page);
        }

        @Override
        public io.reactivex.Single<java.util.List<com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaActivity>> getAthleteActivities(
                String authorization, Integer perPage, Integer page, Long before, Long after) {
            return apiService.getAthleteActivities(authorization, perPage, page, before, after);
        }

        @Override
        public io.reactivex.Single<com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStreamResponse> getActivityStreams(
                String authorization, Long activityId, String keys, Boolean keyByType) {
            return apiService.getActivityStreams(authorization, activityId, keys, keyByType);
        }

        @Override
        public io.reactivex.Single<okhttp3.ResponseBody> exportActivityAsGpx(String authorization, Long activityId) {
            return apiService.exportActivityAsGpx(authorization, activityId);
        }
    }
} 