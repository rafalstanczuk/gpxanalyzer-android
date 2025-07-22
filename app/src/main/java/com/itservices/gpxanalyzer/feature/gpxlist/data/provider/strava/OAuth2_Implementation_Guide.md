# Strava OAuth 2.0 Implementation Guide

This document explains the new OAuth 2.0 implementation for Strava API integration in GPX Analyzer.

## Overview

The Strava integration has been completely rewritten to use proper OAuth 2.0 authentication according to the [official Strava API documentation](https://developers.strava.com/docs/authentication/). This replaces the previous static token approach with a robust, production-ready system.

## Key Components

### 1. OAuth 2.0 Models
- **`TokenResponse`**: Handles access and refresh tokens from Strava
- **`TokenRefreshRequest`**: Request model for token refresh
- **`AuthorizationCodeRequest`**: Request model for initial token exchange
- **`ErrorResponse`**: Handles API error responses

### 2. Enhanced API Service
- **`StravaApiService`**: Updated with OAuth 2.0 token endpoints
  - `exchangeAuthorizationCode()`: Exchange authorization code for tokens
  - `refreshAccessToken()`: Refresh expired access tokens
  - `deauthorize()`: Revoke access

### 3. Token Management
- **`StravaTokenManager`**: Centralized OAuth 2.0 token management
  - Automatic token refresh
  - Persistent token storage
  - In-memory caching
  - Token validation

### 4. Updated File Provider
- **`StravaApiFileProvider`**: Updated to use OAuth 2.0 flow
  - Automatic authentication handling
  - Enhanced error handling
  - Improved GPX generation

## Configuration

### 1. Strava App Registration

1. Visit [Strava API Settings](https://www.strava.com/settings/api)
2. Create a new application
3. Set Authorization Callback Domain to: `localhost` (for development)
4. Note your Client ID and Client Secret

### 2. Configure Properties File

Update `app/stravaapi_secure.properties`:

```properties
# OAuth 2.0 Application Credentials
STRAVA_CLIENT_ID=your_client_id_here
STRAVA_CLIENT_SECRET=your_client_secret_here

# OAuth 2.0 Access Tokens (optional for development)
STRAVA_ACCESS_TOKEN=your_access_token_here
STRAVA_REFRESH_TOKEN=your_refresh_token_here

# Redirect URI for OAuth 2.0 flow
STRAVA_REDIRECT_URI=gpxanalyzer://strava-auth
```

### 3. Required Scopes

For GPX file generation, you need these Strava scopes:
- `read`: Read public profile information
- `activity:read`: Read activity data

## Usage

### 1. Check OAuth Configuration

```java
StravaApiFileProvider provider = // injected instance
if (provider.isOAuthConfigured()) {
    // OAuth is properly configured
} else {
    // Show configuration error to user
}
```

### 2. Get Authorization URL

```java
String authUrl = provider.getAuthorizationUrl("read,activity:read");
// Redirect user to authUrl for authorization
```

### 3. Handle Authorization Response

When user completes authorization, they'll be redirected to your app with an authorization code. Use this to get tokens:

```java
// In your activity/service handling the redirect
String authorizationCode = // extract from redirect URI
AuthorizationCodeRequest request = new AuthorizationCodeRequest(
    BuildConfig.STRAVA_CLIENT_ID,
    BuildConfig.STRAVA_CLIENT_SECRET,
    authorizationCode
);

stravaApiService.exchangeAuthorizationCode(request)
    .subscribe(tokenResponse -> {
        tokenManager.saveToken(tokenResponse);
        // User is now authenticated
    });
```

### 4. Use the File Provider

```java
StravaApiFileProvider provider = // injected instance
provider.getFiles(context, gpxFileParser)
    .subscribe(
        gpxFiles -> {
            // Handle successful GPX file retrieval
        },
        error -> {
            // Handle errors (automatic token refresh if needed)
        }
    );
```

## Token Management

### Automatic Features

1. **Token Validation**: Checks expiry before each API call
2. **Automatic Refresh**: Refreshes tokens when they expire (every 6 hours)
3. **Persistent Storage**: Saves tokens securely in SharedPreferences
4. **In-Memory Caching**: Reduces storage access for frequently used tokens

### Manual Operations

```java
// Force token refresh
tokenManager.forceRefreshToken()
    .subscribe(newToken -> {
        // Token refreshed successfully
    });

// Clear all tokens (logout)
tokenManager.clearTokens();

// Get current valid token
tokenManager.getValidToken()
    .subscribe(token -> {
        // Use token for API calls
    });
```

## Error Handling

The system handles common OAuth 2.0 errors:

1. **401 Unauthorized**: Automatically triggers token refresh
2. **Token Expired**: Handled transparently by TokenManager
3. **Invalid Refresh Token**: Requires user re-authentication
4. **Network Errors**: Retries with exponential backoff

## Security Considerations

1. **Client Secret**: Stored in BuildConfig (not ideal for production)
2. **Token Storage**: Encrypted SharedPreferences in production apps
3. **Deep Links**: Validate redirect URIs to prevent attacks
4. **Rate Limiting**: Built-in delays between API calls

## Migration from Previous Implementation

The new implementation is backward compatible with existing configurations:

1. Existing `STRAVA_ACCESS_TOKEN` and `STRAVA_REFRESH_TOKEN` are automatically used
2. If refresh token is available, the system will refresh to get proper expiry times
3. Old static tokens are migrated to the new token management system

## Testing

### Development Mode

For development, you can use personal access tokens:

1. Generate a personal access token from [Strava API Settings](https://www.strava.com/settings/api)
2. Add to `stravaapi_secure.properties` as `STRAVA_ACCESS_TOKEN`
3. The system will work without OAuth flow for testing

### Production Mode

For production apps:

1. Implement full OAuth 2.0 flow with proper redirect handling
2. Store client secret securely (consider using NDK or server-side proxy)
3. Implement proper deep link handling for authorization callbacks

## Troubleshooting

### Common Issues

1. **401 Errors**: Check if tokens are properly configured and not expired
2. **No GPS Data**: Ensure activities have GPS tracks (indoor activities may not have location data)
3. **Rate Limiting**: Respect Strava's rate limits (200 requests per 15 minutes, 2000 per day)

### Debug Logs

Enable debug logging to see OAuth flow:

```
adb shell setprop log.tag.StravaTokenManager DEBUG
adb shell setprop log.tag.StravaApiFileProvider DEBUG
```

### Force Token Refresh

If experiencing authentication issues:

```java
provider.refreshToken()
    .subscribe(
        () -> Log.d(TAG, "Token refreshed successfully"),
        error -> Log.e(TAG, "Token refresh failed", error)
    );
```

## Next Steps

1. **UI Integration**: Add OAuth flow to app's authentication UI
2. **Error UI**: Show appropriate error messages for authentication failures
3. **Settings**: Allow users to manage Strava connection in app settings
4. **Offline Mode**: Handle cases when network is unavailable

## References

- [Strava API Documentation](https://developers.strava.com/docs/)
- [OAuth 2.0 Specification](https://tools.ietf.org/html/rfc6749)
- [Android OAuth Best Practices](https://developer.android.com/training/id-auth/authenticate) 