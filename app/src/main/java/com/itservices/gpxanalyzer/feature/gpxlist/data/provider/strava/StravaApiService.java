package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.AuthorizationCodeRequest;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaActivity;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaScope;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStream;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStreamResponse;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenRefreshRequest;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface for interacting with the Strava API.
 * Provides access to OAuth 2.0 authentication, athlete activities and GPS streams for GPX generation.
 * 
 * @see <a href="https://developers.strava.com/docs/reference/">Strava API Reference</a>
 * @see <a href="https://developers.strava.com/docs/authentication/">Strava OAuth 2.0 Documentation</a>
 */
public interface StravaApiService {

    String BASE_URL = "https://www.strava.com/api/v3/";
    String OAUTH_BASE_URL = "https://www.strava.com/oauth/";
    
    // OAuth 2.0 endpoints
    String TOKEN_ENDPOINT = "token";
    String DEAUTHORIZE_ENDPOINT = "deauthorize";
    
    // Default scopes required for GPX Analyzer functionality
    String DEFAULT_SCOPES = StravaScope.join(StravaScope.READ, StravaScope.ACTIVITY_READ);
    
    // Activity endpoints
    String ACTIVITIES_ENDPOINT = "athlete/activities";
    String ACTIVITY_STREAMS_ENDPOINT = "activities/{id}/streams";
    String ACTIVITY_GPX_EXPORT_ENDPOINT = "activities/{id}/export_gpx";

    // OAuth 2.0 Authentication Methods
    
    /**
     * Exchanges an authorization code for access and refresh tokens.
     * This is the second step in the OAuth 2.0 flow after user authorization.
     * 
     * @param request The authorization code exchange request
     * @return A Single emitting the token response with access and refresh tokens
     * @see <a href="https://developers.strava.com/docs/authentication/#token-exchange">Token Exchange Documentation</a>
     */
    @POST(TOKEN_ENDPOINT)
    Single<TokenResponse> exchangeAuthorizationCode(@Body AuthorizationCodeRequest request);

    /**
     * Refreshes an access token using a refresh token.
     * Access tokens expire every 6 hours and need to be refreshed.
     * 
     * @param request The token refresh request with refresh token
     * @return A Single emitting the new token response
     * @see <a href="https://developers.strava.com/docs/authentication/#token-refresh">Token Refresh Documentation</a>
     */
    @POST(TOKEN_ENDPOINT)
    Single<TokenResponse> refreshAccessToken(@Body TokenRefreshRequest request);

    /**
     * Deauthorizes the current token and revokes access.
     * 
     * @param authorization Authorization header with Bearer token
     * @return A Single emitting the deauthorization response
     */
    @POST(DEAUTHORIZE_ENDPOINT)
    Single<ResponseBody> deauthorize(@Header("Authorization") String authorization);

    // Activity Data Methods

    /**
     * Fetches the authenticated athlete's activities.
     * 
     * @param authorization Authorization header with Bearer token
     * @param perPage Number of activities per page (max 200)
     * @param page Page number
     * @return A Single emitting the list of activities
     */
    @GET(ACTIVITIES_ENDPOINT)
    Single<List<StravaActivity>> getAthleteActivities(
        @Header("Authorization") String authorization,
        @Query("per_page") Integer perPage,
        @Query("page") Integer page
    );

    /**
     * Fetches activities with additional filters.
     * 
     * @param authorization Authorization header with Bearer token
     * @param perPage Number of activities per page (max 200)
     * @param page Page number
     * @param before Unix timestamp for activities before this date
     * @param after Unix timestamp for activities after this date
     * @return A Single emitting the list of activities
     */
    @GET(ACTIVITIES_ENDPOINT)
    Single<List<StravaActivity>> getAthleteActivities(
        @Header("Authorization") String authorization,
        @Query("per_page") Integer perPage,
        @Query("page") Integer page,
        @Query("before") Long before,
        @Query("after") Long after
    );

    /**
     * Fetches streams for a specific activity.
     * Streams contain GPS data, elevation, time, etc.
     * 
     * @param authorization Authorization header with Bearer token
     * @param activityId The activity ID
     * @param keys Comma-separated list of stream types (latlng,elevation,time)
     * @param keyByType Whether to return streams keyed by type
     * @return A Single emitting the stream response object
     */
    @GET(ACTIVITY_STREAMS_ENDPOINT)
    Single<StravaStreamResponse> getActivityStreams(
        @Header("Authorization") String authorization,
        @Path("id") Long activityId,
        @Query("keys") String keys,
        @Query("key_by_type") Boolean keyByType
    );

    /**
     * Exports an activity as GPX format.
     * Note: This endpoint requires special permissions and may not be available for all activities.
     * 
     * @param authorization Authorization header with Bearer token
     * @param activityId The activity ID
     * @return A Single emitting the GPX file content as ResponseBody
     */
    @GET(ACTIVITY_GPX_EXPORT_ENDPOINT)
    Single<ResponseBody> exportActivityAsGpx(
        @Header("Authorization") String authorization,
        @Path("id") Long activityId
    );
} 