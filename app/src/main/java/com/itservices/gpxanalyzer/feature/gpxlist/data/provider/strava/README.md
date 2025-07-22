# Strava API Integration - Proof of Concept

This module implements a Proof of Concept integration with the Strava API using the Proxy pattern. It demonstrates how to fetch athlete activities from Strava and convert them into GPX files that can be processed by the GPX Analyzer application.

## Architecture Overview

The implementation follows these key patterns:

### 🎯 Proxy Pattern
- `StravaApiFileProvider` acts as a proxy between the application and Strava API
- Provides the same interface as local file providers (`getFiles()` method)
- Handles API complexity, caching, and error recovery transparently

### 🔧 Components

#### Core Service Layer
- **`StravaApiService`**: Retrofit interface defining Strava API endpoints
- **`StravaApiFileProvider`**: Main proxy implementation with caching and error handling

#### Data Models
- **`StravaActivity`**: Represents a Strava activity with metadata
- **`StravaStream`**: Represents GPS, elevation, and time data streams
- **`StravaStreamResponse`**: Handles the updated 2025 API format for streams
- **`StravaStreamMapper`**: Converts between old and new API response formats

### 🔄 API Format Changes (2025)

In 2025, Strava updated their API response format for activity streams. The key changes are:

1. **Old Format (pre-2025)**: Streams were returned as an array of stream objects
   ```json
   [
     {
       "type": "latlng",
       "data": [[lat1, lng1], [lat2, lng2], ...],
       "series_type": "distance",
       "original_size": 1000,
       "resolution": "high"
     },
     {
       "type": "altitude",
       "data": [100, 101, 102, ...],
       "series_type": "distance",
       "original_size": 1000,
       "resolution": "high"
     }
   ]
   ```

2. **New Format (2025+)**: Streams are returned as a single object with stream types as keys
   ```json
   {
     "latlng": {
       "data": [[lat1, lng1], [lat2, lng2], ...],
       "series_type": "distance",
       "original_size": 1000,
       "resolution": "high"
     },
     "altitude": {
       "data": [100, 101, 102, ...],
       "series_type": "distance",
       "original_size": 1000,
       "resolution": "high"
     }
   }
   ```

To handle this change, we've implemented:
- `StravaStreamResponse` class to parse the new format
- `StravaStreamMapper` to convert between formats
- Backward compatibility for existing code

#### Dependency Injection
- **`StravaModule`**: Dagger Hilt module providing configured Retrofit service

## Setup Instructions

### 1. Strava API Application Setup

Following the [official Strava documentation](https://developers.strava.com/docs/getting-started/#account):

1. Go to [Strava API Settings](https://www.strava.com/settings/api)
2. Click "Create App" to create a new application
3. Fill in your application details:
   - **Application Name**: GPX Analyzer (or your app name)
   - **Category**: Data Import/Export
   - **Club**: Leave blank unless applicable
   - **Website**: Your app website or GitHub repository
   - **Authorization Callback Domain**: `localhost` (for development) or your domain
4. Note your **Client ID** and **Client Secret** from the application settings

### 2. Configuration File Setup

Create a file `app/secure.properties` in your project root:

```properties
# OAuth 2.0 Application Credentials (from Strava API settings)
STRAVA_CLIENT_ID=your_client_id_here
STRAVA_CLIENT_SECRET=your_client_secret_here

# OAuth 2.0 Access Tokens (for production)
STRAVA_ACCESS_TOKEN=your_access_token_here
STRAVA_REFRESH_TOKEN=your_refresh_token_here
STRAVA_REDIRECT_URI=gpxanalyzer://strava-auth

```

**Important**: Add `secure.properties` to your `.gitignore` to avoid committing API keys.

### 3. Token Configuration Options

#### Option A: Personal Access Token (PoC/Testing)
For quick testing, you can use your personal access token:
1. Go to [Strava API Settings](https://www.strava.com/settings/api)
2. Find "Your Access Token" in the "My API Application" section

**Note**: Personal access tokens don't expire but have limited scope and rate limits.

#### Option B: OAuth 2.0 Flow (Production)
For production apps, implement proper OAuth 2.0:
1. Configure `STRAVA_CLIENT_ID` and `STRAVA_CLIENT_SECRET`
2. Implement OAuth 2.0 authorization flow
3. Store `STRAVA_ACCESS_TOKEN` and `STRAVA_REFRESH_TOKEN`
4. Handle token refresh (access tokens expire every 6 hours)

**Rate Limits**: 200 requests per 15 minutes, 2,000 requests per day per application.

### 3. Usage

The provider integrates seamlessly with the existing architecture:

```java
// Injected automatically via Dagger Hilt
@Inject
StravaApiFileProvider stravaApiProvider;

// Use like any other provider
Single<List<GpxFileInfo>> gpxFiles = stravaApiProvider.getFiles(context, parserFunction);
```

## Features Demonstrated

### ✅ Implemented Features

- **API Integration**: Fetches athlete activities from Strava
- **Stream Processing**: Downloads GPS, elevation, and time data
- **GPX Generation**: Converts Strava data to standard GPX format
- **Caching**: Local file caching with expiry (1 hour default)
- **Error Handling**: Graceful degradation and retry logic
- **Rate Limiting**: Respects Strava API rate limits with delays
- **Proxy Pattern**: Transparent API abstraction

### 🔄 Reactive Implementation

Uses RxJava for asynchronous operations:
- Non-blocking API calls
- Stream-based data processing
- Error recovery and retry mechanisms
- Background thread execution

### 📦 Data Flow

```
Strava API → Activities → Streams → GPX Generation → File Cache → GpxFileInfo
```

## Configuration Options

### API Limits
```java
// Configurable in StravaApiFileProvider
private static final int DEFAULT_ACTIVITIES_PER_PAGE = 30;
private static final int MAX_ACTIVITIES_TO_FETCH = 100;
private static final long CACHE_EXPIRY_MINUTES = 60;
```

### Required Permissions

The implementation requires activities to have GPS data (`start_latlng` not null).

## Testing the Integration

### Mock Data
For testing without API calls, you can use the existing local GPX files in `res/raw/`:
- `skiing20250121t091423.gpx`
- `test20230719.gpx`
- `test20230729.gpx`

### API Testing
1. Configure your API key in `secure.properties`
2. Switch provider type to `ONLINE` in the app
3. Monitor logs for API responses and GPX generation

## Error Scenarios Handled

- **Invalid API Key**: Clear error message
- **No GPS Data**: Filters out activities without coordinates
- **API Rate Limits**: Automatic delays between requests
- **Network Issues**: Retry logic with exponential backoff
- **Malformed Data**: Validation and graceful skipping

## Future Enhancements

### OAuth2 Implementation
For production use, implement proper OAuth2 flow:
```java
// Redirect to Strava authorization
String authUrl = "https://www.strava.com/oauth/authorize" +
    "?client_id=" + CLIENT_ID +
    "&redirect_uri=" + REDIRECT_URI +
    "&response_type=code" +
    "&scope=read,activity:read";
```

### Advanced Features
- **Activity Filtering**: By sport type, date range, etc.
- **Batch Processing**: Parallel stream downloads
- **Progressive Sync**: Incremental updates
- **Offline Mode**: Enhanced caching strategies

## Architecture Benefits

### 🎯 Clean Architecture Compliance
- **Separation of Concerns**: API logic isolated in dedicated module
- **Dependency Inversion**: Abstractions don't depend on implementations
- **Single Responsibility**: Each class has a focused purpose

### 🔧 Maintainability
- **Testable**: Clear interfaces for mocking
- **Extensible**: Easy to add new data sources
- **Configurable**: Environment-specific settings

### ⚡ Performance
- **Caching**: Reduces redundant API calls
- **Async Processing**: Non-blocking operations
- **Efficient Parsing**: Stream-based GPX generation

## Technical Notes

### Strava API Limitations
- Rate limits: 600 requests per 15 minutes, 30,000 per day
- Activity data access requires proper scopes
- Some features require premium Strava subscription

### GPX Compatibility
Generated GPX files are compatible with:
- Standard GPX 1.1 specification
- Existing GPXAnalyzer parsing infrastructure
- Common GPS applications and tools

This implementation serves as a solid foundation for production Strava integration while demonstrating modern Android development patterns and clean architecture principles. 