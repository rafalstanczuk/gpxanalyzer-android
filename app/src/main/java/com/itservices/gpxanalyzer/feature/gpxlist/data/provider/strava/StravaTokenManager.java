package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.itservices.gpxanalyzer.BuildConfig;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaScope;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenRefreshRequest;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * Manages OAuth 2.0 tokens for Strava API integration.
 * Handles token storage, validation, and automatic refresh.
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/">Strava OAuth 2.0 Documentation</a>
 */
@Singleton
public class StravaTokenManager {
    
    private static final String TAG = StravaTokenManager.class.getSimpleName();
    
    // SharedPreferences configuration
    private static final String PREFS_NAME = "strava_oauth_tokens";
    private static final String KEY_TOKEN_RESPONSE = "token_response";
    private static final String KEY_LAST_REFRESH = "last_refresh_time";
    
    private final Context context;
    private final StravaApiService stravaApiService;
    private final Gson gson;
    
    // In-memory cache
    private TokenResponse cachedTokenResponse;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    @Inject
    public StravaTokenManager(@ApplicationContext Context context, StravaApiService stravaApiService) {
        this.context = context;
        this.stravaApiService = stravaApiService;
        this.gson = new Gson();
    }

    /**
     * Gets a valid access token, refreshing if necessary.
     * 
     * @return Single emitting a valid TokenResponse, or error if unable to obtain one
     */
    public Single<TokenResponse> getValidToken() {
        return Single.fromCallable(() -> {
                    TokenResponse token = getCachedOrStoredToken();
                    return token != null ? token : new TokenResponse(); // Return empty token instead of null
                })
                .flatMap(token -> {
                    if (token.getAccessToken() == null) {
                        return initializeFromBuildConfig();
                    } else if (token.isExpired()) {
                        Log.d(TAG, "Token expired, refreshing...");
                        return refreshToken(token);
                    } else {
                        Log.d(TAG, "Using valid cached token");
                        return Single.just(token);
                    }
                })
                .doOnSuccess(this::cacheToken)
                .doOnSuccess(this::saveTokenToStorage)
                .doOnError(error -> {
                    if (error instanceof HttpException) {
                        HttpException httpError = (HttpException) error;
                        if (httpError.code() == 401) {
                            Log.e(TAG, "Authentication failed (401). This likely means:");
                            Log.e(TAG, "1. Token expired and refresh failed, OR");
                            Log.e(TAG, "2. Token missing required 'activity:read' scope");
                            Log.e(TAG, "Solution: Re-authorize with correct scopes using:");
                            Log.e(TAG, StravaOAuthHelper.generateAuthorizationUrlForTesting());
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gets the authorization header for API calls.
     * 
     * @return Single emitting authorization header value or error
     */
    public Single<String> getAuthorizationHeader() {
        return getValidToken()
                .map(TokenResponse::getAuthorizationHeader)
                .doOnSuccess(header -> Log.d(TAG, "Generated authorization header"));
    }

    /**
     * Manually refreshes the current token.
     * 
     * @return Single emitting the new TokenResponse
     */
    public Single<TokenResponse> forceRefreshToken() {
        return Single.fromCallable(() -> {
                    TokenResponse token = getCachedOrStoredToken();
                    return token != null ? token : new TokenResponse(); // Return empty token instead of null
                })
                .flatMap(token -> {
                    if (token.getRefreshToken() == null || token.getRefreshToken().isEmpty()) {
                        return Single.error(new IllegalStateException("No refresh token available"));
                    }
                    return refreshToken(token);
                })
                .doOnSuccess(this::cacheToken)
                .doOnSuccess(this::saveTokenToStorage)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Saves a new token (typically from OAuth flow completion).
     * 
     * @param tokenResponse The token response to save
     */
    public void saveToken(TokenResponse tokenResponse) {
        cacheToken(tokenResponse);
        saveTokenToStorage(tokenResponse);
        Log.i(TAG, "Token saved successfully");
    }

    /**
     * Clears all stored tokens.
     */
    public void clearTokens() {
        cachedTokenResponse = null;
        lastCacheTime = 0;
        
        SharedPreferences prefs = getSharedPreferences();
        prefs.edit().clear().apply();
        
        Log.i(TAG, "All tokens cleared");
    }

    /**
     * Checks if OAuth 2.0 credentials are properly configured.
     * 
     * @return true if client ID and secret are available
     */
    public boolean isOAuthConfigured() {
        return !BuildConfig.STRAVA_CLIENT_ID.isEmpty() && 
               !BuildConfig.STRAVA_CLIENT_SECRET.isEmpty();
    }

    /**
     * Gets the OAuth 2.0 authorization URL for user authentication using default scopes.
     * Uses the required scopes for GPX Analyzer: "read,activity:read"
     * 
     * @return Authorization URL with default scopes
     */
    public String getAuthorizationUrl() {
        return getAuthorizationUrl(StravaApiService.DEFAULT_SCOPES, false);
    }

    /**
     * Gets the OAuth 2.0 authorization URL for user authentication.
     * 
     * @param scopes Required scopes (e.g., "read,activity:read")
     * @return Authorization URL
     */
    public String getAuthorizationUrl(String scopes) {
        return getAuthorizationUrl(scopes, false);
    }

    /**
     * Gets the OAuth 2.0 authorization URL for user authentication.
     *
     * @param scopes Required scopes (e.g., "read,activity:read")
     * @param forcePrompt If true, will force the approval prompt on Strava's side
     * @return Authorization URL
     */
    public String getAuthorizationUrl(String scopes, boolean forcePrompt) {
        if (!isOAuthConfigured()) {
            throw new IllegalStateException("OAuth 2.0 not configured");
        }

        // Make sure activity:read scope is included to fix permission issues
        if (!scopes.contains(StravaScope.ACTIVITY_READ.getValue())) {
            scopes = scopes + "," + StravaScope.ACTIVITY_READ.getValue();
        }

        String approvalPrompt = forcePrompt ? "force" : "auto";

        return "https://www.strava.com/oauth/authorize" +
               "?client_id=" + BuildConfig.STRAVA_CLIENT_ID +
               "&redirect_uri=" + BuildConfig.STRAVA_REDIRECT_URI +
               "&response_type=code" +
               "&scope=" + scopes +
               "&approval_prompt=" + approvalPrompt;
    }

    // Private helper methods

    /**
     * Gets token from cache or storage.
     */
    private TokenResponse getCachedOrStoredToken() {
        // Check in-memory cache first
        if (cachedTokenResponse != null && isCacheValid()) {
            Log.d(TAG, "Using in-memory cached token");
            return cachedTokenResponse;
        }
        
        // Load from storage
        SharedPreferences prefs = getSharedPreferences();
        String tokenJson = prefs.getString(KEY_TOKEN_RESPONSE, null);
        
        if (tokenJson != null) {
            try {
                TokenResponse token = gson.fromJson(tokenJson, TokenResponse.class);
                cacheToken(token);
                Log.d(TAG, "Loaded token from storage");
                return token;
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse stored token", e);
                prefs.edit().remove(KEY_TOKEN_RESPONSE).apply();
            }
        }
        
        return null;
    }

    /**
     * Initializes token from BuildConfig values (fallback for development).
     */
    private Single<TokenResponse> initializeFromBuildConfig() {
        String accessToken = BuildConfig.STRAVA_ACCESS_TOKEN;
        String refreshToken = BuildConfig.STRAVA_REFRESH_TOKEN;
        
        if (accessToken.isEmpty() && refreshToken.isEmpty()) {
            return Single.error(new IllegalStateException(
                "No tokens available. Please complete OAuth 2.0 flow or configure tokens in secure.properties"));
        }
        
        if (!accessToken.isEmpty()) {
            Log.d(TAG, "Initializing with BuildConfig access token");
            TokenResponse token = new TokenResponse(
                accessToken,
                refreshToken.isEmpty() ? null : refreshToken,
                null, // Unknown expiry, will be treated as expired
                null,
                "Bearer"
            );
            
            // If we have a refresh token, try to refresh immediately to get proper expiry
            if (!refreshToken.isEmpty()) {
                return refreshToken(token);
            } else {
                return Single.just(token);
            }
        }
        
        return Single.error(new IllegalStateException("No valid token configuration found"));
    }

    /**
     * Refreshes an access token using the refresh token.
     */
    private Single<TokenResponse> refreshToken(TokenResponse currentToken) {
        if (currentToken.getRefreshToken() == null || currentToken.getRefreshToken().isEmpty()) {
            return Single.error(new IllegalStateException("No refresh token available"));
        }
        
        if (!isOAuthConfigured()) {
            return Single.error(new IllegalStateException("OAuth 2.0 credentials not configured"));
        }
        
        Log.d(TAG, "Refreshing access token...");
        
        TokenRefreshRequest request = new TokenRefreshRequest(
            BuildConfig.STRAVA_CLIENT_ID,
            BuildConfig.STRAVA_CLIENT_SECRET,
            currentToken.getRefreshToken()
        );
        
        return stravaApiService.refreshAccessToken(request)
                .doOnSuccess(response -> Log.i(TAG, "Token refreshed successfully"))
                .doOnError(error -> Log.e(TAG, "Token refresh failed", error));
    }

    /**
     * Caches token in memory.
     */
    private void cacheToken(TokenResponse token) {
        this.cachedTokenResponse = token;
        this.lastCacheTime = System.currentTimeMillis();
    }

    /**
     * Saves token to persistent storage.
     */
    private void saveTokenToStorage(TokenResponse token) {
        SharedPreferences prefs = getSharedPreferences();
        String tokenJson = gson.toJson(token);
        
        prefs.edit()
                .putString(KEY_TOKEN_RESPONSE, tokenJson)
                .putLong(KEY_LAST_REFRESH, System.currentTimeMillis())
                .apply();
    }

    /**
     * Checks if in-memory cache is still valid.
     */
    private boolean isCacheValid() {
        return (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION_MS;
    }

    /**
     * Gets SharedPreferences instance for token storage.
     */
    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
} 