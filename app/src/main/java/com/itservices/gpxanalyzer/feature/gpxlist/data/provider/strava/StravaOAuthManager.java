package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse;
import com.itservices.gpxanalyzer.feature.gpxlist.ui.StravaOAuthActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Manages Strava OAuth 2.0 authentication flow.
 * 
 * This manager provides a high-level interface for:
 * - Checking authentication status
 * - Starting OAuth flow
 * - Managing token lifecycle
 * - Handling authentication state changes
 */
@Singleton
public class StravaOAuthManager {
    
    private static final String TAG = StravaOAuthManager.class.getSimpleName();
    
    private final StravaTokenManager tokenManager;
    private final StravaApiService stravaApiService;
    
    @Inject
    public StravaOAuthManager(StravaTokenManager tokenManager, StravaApiService stravaApiService) {
        this.tokenManager = tokenManager;
        this.stravaApiService = stravaApiService;
    }
    
    /**
     * Checks if user is currently authenticated with proper scopes.
     * 
     * @return Single emitting true if authenticated with required scopes
     */
    public Single<Boolean> isAuthenticated() {
        return tokenManager.getValidToken()
                .map(token -> token != null && token.getAccessToken() != null && !token.isExpired())
                .onErrorReturnItem(false)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Checks if OAuth is properly configured (client ID and secret available).
     * 
     * @return true if OAuth configuration is available
     */
    public boolean isOAuthConfigured() {
        return tokenManager.isOAuthConfigured();
    }
    
    /**
     * Gets current authentication status with detailed information.
     * 
     * @return Single emitting AuthenticationStatus
     */
    public Single<AuthenticationStatus> getAuthenticationStatus() {
        if (!isOAuthConfigured()) {
            return Single.just(AuthenticationStatus.NOT_CONFIGURED);
        }
        
        return tokenManager.getValidToken()
                .flatMap(token -> {
                    if (token == null || token.getAccessToken() == null) {
                        return Single.just(AuthenticationStatus.NOT_AUTHENTICATED);
                    } else if (token.isExpired()) {
                        return Single.just(AuthenticationStatus.TOKEN_EXPIRED);
                    } else {
                        // Test the token with a minimal API call to verify scopes
                        return testTokenScopes(token)
                                .map(scopesValid -> scopesValid ? 
                                    AuthenticationStatus.AUTHENTICATED : 
                                    AuthenticationStatus.SCOPE_MISSING);
                    }
                })
                .onErrorReturn(error -> {
                    Log.w(TAG, "Error checking authentication status", error);
                    return AuthenticationStatus.ERROR;
                })
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Tests if the token has the required activity:read scope by making a minimal API call.
     * 
     * @param token The token to test
     * @return Single emitting true if scopes are valid, false otherwise
     */
    private Single<Boolean> testTokenScopes(TokenResponse token) {
        // Try to fetch just 1 activity to verify the token has activity:read scope
        return stravaApiService.getAthleteActivities(
                token.getAuthorizationHeader(), 1, 1)
            .map(activities -> true) // If we get here, the token is valid with correct scopes
            .onErrorReturn(error -> {
                if (error instanceof retrofit2.adapter.rxjava2.HttpException) {
                    retrofit2.adapter.rxjava2.HttpException httpError = 
                        (retrofit2.adapter.rxjava2.HttpException) error;
                    
                    // Check if error is related to missing scope
                    if (httpError.code() == 401) {
                        try {
                            String errorBody = httpError.response().errorBody().string();
                            if (errorBody.contains("activity:read_permission") && 
                                errorBody.contains("missing")) {
                                Log.w(TAG, "Token is missing activity:read permission scope");
                                return false;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }
                }
                // For any other error, assume the token is invalid
                return false;
            })
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * Starts the OAuth authorization flow.
     * 
     * @param activity The activity to start OAuth from
     * @param requestCode Request code for activity result
     */
    public void startOAuthFlow(@NonNull Activity activity, int requestCode) {
        Log.i(TAG, "Starting OAuth flow");
        
        Intent intent = new Intent(activity, StravaOAuthActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
    
    /**
     * Handles OAuth activity result.
     * 
     * @param requestCode The request code from startActivityForResult
     * @param resultCode The result code from the OAuth activity
     * @param data The intent data from the OAuth activity
     * @return OAuthResult indicating the outcome
     */
    public OAuthResult handleOAuthResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "Handling OAuth result: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (resultCode == StravaOAuthActivity.RESULT_SUCCESS) {
            Log.i(TAG, "OAuth completed successfully");
            return OAuthResult.SUCCESS;
        } else if (resultCode == StravaOAuthActivity.RESULT_ERROR) {
            String errorMessage = data != null ? 
                data.getStringExtra(StravaOAuthActivity.EXTRA_ERROR_MESSAGE) : "Unknown error";
            Log.e(TAG, "OAuth failed: " + errorMessage);
            return OAuthResult.error(errorMessage);
        } else if (resultCode == StravaOAuthActivity.RESULT_CANCELLED) {
            Log.i(TAG, "OAuth cancelled by user");
            return OAuthResult.CANCELLED;
        } else {
            Log.w(TAG, "Unknown OAuth result code: " + resultCode);
            return OAuthResult.error("Unknown result");
        }
    }
    
    /**
     * Signs out the user by clearing all tokens.
     */
    public void signOut() {
        Log.i(TAG, "Signing out user");
        tokenManager.clearTokens();
    }
    
    /**
     * Forces a token refresh.
     * 
     * @return Single emitting true if refresh successful
     */
    public Single<Boolean> refreshToken() {
        return tokenManager.forceRefreshToken()
                .map(token -> true)
                .onErrorReturnItem(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    
    /**
     * Gets the current token for debugging purposes.
     * 
     * @return Single emitting current TokenResponse or null
     */
    public Single<TokenResponse> getCurrentToken() {
        return tokenManager.getValidToken()
                .onErrorReturnItem(new TokenResponse())
                .subscribeOn(Schedulers.io());
    }
    
    // Enums and Data Classes
    
    /**
     * Represents the current authentication status.
     */
    public enum AuthenticationStatus {
        /** OAuth not configured (missing client ID/secret) */
        NOT_CONFIGURED,
        /** User not authenticated */
        NOT_AUTHENTICATED,
        /** User authenticated with valid token */
        AUTHENTICATED,
        /** Token expired, needs refresh */
        TOKEN_EXPIRED,
        /** Error checking status */
        ERROR,
        /** Token is missing required scope (e.g., activity:read) */
        SCOPE_MISSING
    }
    
    /**
     * Represents the result of an OAuth flow.
     */
    public static class OAuthResult {
        public enum Type {
            SUCCESS,
            CANCELLED,
            ERROR
        }
        
        public final Type type;
        public final String errorMessage;
        
        private OAuthResult(Type type, String errorMessage) {
            this.type = type;
            this.errorMessage = errorMessage;
        }
        
        public static final OAuthResult SUCCESS = new OAuthResult(Type.SUCCESS, null);
        public static final OAuthResult CANCELLED = new OAuthResult(Type.CANCELLED, null);
        
        public static OAuthResult error(String message) {
            return new OAuthResult(Type.ERROR, message);
        }
        
        public boolean isSuccess() {
            return type == Type.SUCCESS;
        }
        
        public boolean isCancelled() {
            return type == Type.CANCELLED;
        }
        
        public boolean isError() {
            return type == Type.ERROR;
        }
    }
    
    /**
     * Interface for listening to authentication state changes.
     */
    public interface AuthenticationListener {
        void onAuthenticationSuccess();
        void onAuthenticationError(String error);
        void onAuthenticationCancelled();
    }
    
    /**
     * Convenience method to start OAuth with listener.
     * 
     * @param activity The activity to start OAuth from
     * @param requestCode Request code for activity result
     * @param listener Listener for authentication events
     */
    public void startOAuthFlow(@NonNull Activity activity, int requestCode, @NonNull AuthenticationListener listener) {
        // Note: The actual listener handling would need to be implemented in the calling activity
        // This is just a convenience method signature
        startOAuthFlow(activity, requestCode);
    }
    
    /**
     * Checks if the user needs to re-authenticate (for scope permission issues).
     * 
     * @return Single emitting true if re-authentication is recommended
     */
    public Single<Boolean> needsReAuthentication() {
        // This could be enhanced to check for specific scope errors
        return getAuthenticationStatus()
                .map(status -> status == AuthenticationStatus.NOT_AUTHENTICATED || 
                              status == AuthenticationStatus.TOKEN_EXPIRED ||
                              status == AuthenticationStatus.ERROR)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Gets a user-friendly description of the current authentication status.
     * 
     * @return Single emitting status description
     */
    public Single<String> getAuthenticationStatusDescription() {
        return getAuthenticationStatus()
                .map(status -> {
                    switch (status) {
                        case NOT_CONFIGURED:
                            return "OAuth not configured. Please check app setup.";
                        case NOT_AUTHENTICATED:
                            return "Not connected to Strava. Please authorize the app.";
                        case AUTHENTICATED:
                            return "Connected to Strava successfully.";
                        case TOKEN_EXPIRED:
                            return "Strava connection expired. Please re-authorize.";
                        case ERROR:
                            return "Error checking Strava connection.";
                        case SCOPE_MISSING:
                            return "Strava connection missing required scope. Please re-authorize.";
                        default:
                            return "Unknown status.";
                    }
                })
                .subscribeOn(Schedulers.io());
    }
} 