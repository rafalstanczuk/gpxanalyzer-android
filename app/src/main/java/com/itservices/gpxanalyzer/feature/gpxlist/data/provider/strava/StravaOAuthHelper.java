package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import android.util.Log;
import com.itservices.gpxanalyzer.BuildConfig;

/**
 * Utility class for Strava OAuth 2.0 operations.
 * Provides helper methods for generating authorization URLs and debugging OAuth issues.
 */
public class StravaOAuthHelper {
    
    private static final String TAG = StravaOAuthHelper.class.getSimpleName();
    
    /**
     * Generates an authorization URL with default scopes for manual testing.
     * Use this method when you need to manually regenerate tokens with correct scopes.
     * 
     * @return Authorization URL string for manual browser navigation
     */
    public static String generateAuthorizationUrlForTesting() {
        if (BuildConfig.STRAVA_CLIENT_ID.isEmpty()) {
            Log.e(TAG, "STRAVA_CLIENT_ID not configured in BuildConfig");
            return null;
        }
        
        // Ensure scopes include both read and activity:read
        String scopes = StravaApiService.DEFAULT_SCOPES;
        if (!scopes.contains(StravaApiService.SCOPE_ACTIVITY_READ)) {
            scopes = scopes + "," + StravaApiService.SCOPE_ACTIVITY_READ;
        }
        
        String authUrl = "https://www.strava.com/oauth/authorize" +
                "?client_id=" + BuildConfig.STRAVA_CLIENT_ID +
                "&redirect_uri=" + BuildConfig.STRAVA_REDIRECT_URI +
                "&response_type=code" +
                "&scope=" + scopes +
                "&approval_prompt=auto";
        
        Log.i(TAG, "Generated authorization URL for testing:");
        Log.i(TAG, authUrl);
        Log.i(TAG, "Required scopes: " + scopes);
        Log.i(TAG, "Steps:");
        Log.i(TAG, "1. FIRST: Update Strava app settings at https://www.strava.com/settings/api");
        Log.i(TAG, "   - Set 'Authorization Callback Domain' to: localhost");
        Log.i(TAG, "2. Navigate to the URL above in a browser");
        Log.i(TAG, "3. Authorize the app with required scopes (read + activity:read)");
        Log.i(TAG, "4. Copy the authorization code from the redirect URL");
        Log.i(TAG, "5. Exchange the code for access/refresh tokens");
        Log.i(TAG, "6. Update stravaapi_secure.properties with new tokens");
        Log.i(TAG, "");
        Log.i(TAG, "ALTERNATIVE: Generate tokens directly at https://www.strava.com/settings/api");
        Log.i(TAG, "- Ensure 'Read activities' scope is checked before generating");
        
        return authUrl;
    }
    
    /**
     * Logs the current OAuth configuration for debugging.
     * Helps verify that BuildConfig contains the necessary OAuth credentials.
     */
    public static void logOAuthConfiguration() {
        Log.d(TAG, "=== Strava OAuth Configuration ===");
        Log.d(TAG, "Client ID configured: " + !BuildConfig.STRAVA_CLIENT_ID.isEmpty());
        Log.d(TAG, "Client Secret configured: " + !BuildConfig.STRAVA_CLIENT_SECRET.isEmpty());
        Log.d(TAG, "Access Token configured: " + !BuildConfig.STRAVA_ACCESS_TOKEN.isEmpty());
        Log.d(TAG, "Refresh Token configured: " + !BuildConfig.STRAVA_REFRESH_TOKEN.isEmpty());
        Log.d(TAG, "Redirect URI: " + BuildConfig.STRAVA_REDIRECT_URI);
        Log.d(TAG, "Required scopes: " + StravaApiService.DEFAULT_SCOPES);
        Log.d(TAG, "=== End Configuration ===");
    }
    
    /**
     * Call this method during app startup to automatically log the authorization URL.
     * Useful during development to quickly get the URL for re-authorization.
     */
    public static void debugPrintAuthorizationUrl() {
        String separator = "================================================================================";
        Log.w(TAG, "");
        Log.w(TAG, separator);
        Log.w(TAG, "STRAVA OAUTH DEBUG: SCOPE PERMISSION ISSUE DETECTED");
        Log.w(TAG, "");
        Log.w(TAG, "Current tokens lack 'activity:read' scope. You need to re-authorize.");
        Log.w(TAG, "");
        Log.w(TAG, "STEP 1: Update redirect URI in Strava app settings:");
        Log.w(TAG, "- Go to: https://www.strava.com/settings/api");
        Log.w(TAG, "- Edit your app and set 'Authorization Callback Domain' to: localhost");
        Log.w(TAG, "");
        generateAuthorizationUrlForTesting();
        Log.w(TAG, "");
        Log.w(TAG, "STEP 2: COPY THE URL ABOVE AND PASTE IT IN A BROWSER TO RE-AUTHORIZE");
        Log.w(TAG, separator);
        Log.w(TAG, "");
    }
    
    /**
     * Validates the authorization code format.
     * 
     * @param authorizationCode The authorization code from Strava redirect
     * @return true if the code appears valid, false otherwise
     */
    public static boolean isValidAuthorizationCode(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
            Log.w(TAG, "Authorization code is null or empty");
            return false;
        }
        
        // Strava authorization codes are typically 40 characters long
        if (authorizationCode.length() < 20) {
            Log.w(TAG, "Authorization code seems too short: " + authorizationCode.length() + " characters");
            return false;
        }
        
        Log.d(TAG, "Authorization code format appears valid");
        return true;
    }
} 