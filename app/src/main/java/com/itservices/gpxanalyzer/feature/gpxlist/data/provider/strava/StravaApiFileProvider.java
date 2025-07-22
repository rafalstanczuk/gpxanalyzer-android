package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.mapper.StravaStreamMapper;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaActivity;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStream;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStreamResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * Strava API File Provider implementing Proxy pattern with OAuth 2.0 authentication.
 * Acts as a proxy between the application and Strava API, providing GPX files
 * by fetching activities and converting them to GPX format.
 * 
 * This implementation demonstrates:
 * - Proxy pattern for API abstraction
 * - OAuth 2.0 integration with automatic token refresh
 * - Strava API integration
 * - GPX generation from activity streams
 * - Caching and error handling
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/">Strava OAuth 2.0 Documentation</a>
 */
public class StravaApiFileProvider {

    private static final String TAG = StravaApiFileProvider.class.getSimpleName();
    
    // API Configuration
    private static final int DEFAULT_ACTIVITIES_PER_PAGE = 30;
    private static final int MAX_ACTIVITIES_TO_FETCH = 10;
    private static final String REQUIRED_STREAM_KEYS = "latlng,altitude,time";
    
    // Cache configuration
    private static final String CACHE_DIR_NAME = "strava_gpx_cache";
    private static final long CACHE_EXPIRY_MINUTES = 60; // 1 hour cache

    private final StravaApiService stravaApiService;
    private final StravaTokenManager tokenManager;
    
    // Cache for storing temporary GPX files
    private File cacheDirectory;
    private long lastCacheTime = 0;

    @Inject
    public StravaApiFileProvider(StravaApiService stravaApiService, StravaTokenManager tokenManager) {
        this.stravaApiService = stravaApiService;
        this.tokenManager = tokenManager;
    }

    /**
     * Proxy method implementing the expected interface.
     * Fetches activities from Strava, converts them to GPX files, and returns parsed GpxFileInfo objects.
     * 
     * @param context Android context for file operations
     * @param parserFunction Function to parse GPX files to GpxFileInfo
     * @return Single emitting list of GpxFileInfo objects
     */
    public Single<List<GpxFileInfo>> getFiles(Context context, Function<File, GpxFileInfo> parserFunction) {
        return initializeCacheDirectory(context)
                .flatMap(cacheDir -> {
                    // Check if we should use cached data
                    if (isCacheValid()) {
                        Log.d(TAG, "Using cached GPX files");
                        return getCachedGpxFiles(parserFunction);
                    } else {
                        Log.d(TAG, "Fetching fresh data from Strava API");
                        return fetchAndCacheActivities(context, parserFunction);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSuccess(files -> Log.i(TAG, "Successfully retrieved " + files.size() + " GPX files from Strava"))
                .doOnError(error -> Log.e(TAG, "Error fetching files from Strava", error));
    }

    /**
     * Fetches and caches activities from Strava API.
     * 
     * @param context Context for cache directory access
     * @param parserFunction Function to parse GPX files into file info objects
     * @return Single emitting parsed GPX file info list
     */
    private Single<List<GpxFileInfo>> fetchAndCacheActivities(Context context, Function<File, GpxFileInfo> parserFunction) {
        Log.d(TAG, "Fetching fresh data from Strava API");
        
        return initializeCacheDirectory(context)
                .flatMap(cacheDir -> {
                    // Get authorization header from token manager
                    return tokenManager.getAuthorizationHeader()
                            .flatMap(this::fetchActivitiesFromStrava)
                            .flatMap(activities -> convertActivitiesToGpxFiles(activities, context))
                            .flatMap(gpxFiles -> parseGpxFiles(gpxFiles, parserFunction))
                            .doOnSuccess(fileInfos -> {
                                Log.i(TAG, "Successfully fetched " + fileInfos.size() + " activities from Strava");
                                lastCacheTime = System.currentTimeMillis();
                            });
                })
                .retry(3, error -> {
                    // Retry logic for authentication errors
                    if (isAuthenticationError(error)) {
                        Log.w(TAG, "Authentication error, attempting token refresh");
                        // Check for missing scopes
                        if (error instanceof retrofit2.adapter.rxjava2.HttpException) {
                            retrofit2.adapter.rxjava2.HttpException httpError = 
                                (retrofit2.adapter.rxjava2.HttpException) error;
                            
                            try {
                                String errorBody = httpError.response().errorBody().string();
                                if (errorBody.contains("activity:read_permission") && 
                                    errorBody.contains("missing")) {
                                    
                                    Log.e(TAG, "SCOPE PERMISSION ERROR: Access token missing 'activity:read' scope!");
                                    Log.e(TAG, "The current token was authorized without the required permissions.");
                                    Log.e(TAG, "To fix this:");
                                    StravaOAuthHelper.debugPrintAuthorizationUrl();
                                    Log.e(TAG, "1. Go to: " + getAuthorizationUrl(StravaApiService.DEFAULT_SCOPES + ",activity:read"));
                                    Log.e(TAG, "2. Authorize the app with 'activity:read' permission");
                                    Log.e(TAG, "3. Update stravaapi_secure.properties with the new tokens");
                                    
                                    // Don't retry in this case - requires user re-auth
                                    return false;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                        }
                        
                        // Try to refresh token for other auth errors
                        try {
                            tokenManager.forceRefreshToken().blockingGet();
                            return true; // Retry after token refresh
                        } catch (Exception e) {
                            Log.e(TAG, "Token refresh failed during retry", e);
                            return false; // Don't retry further
                        }
                    }
                    return false; // Don't retry other errors
                })
                .doOnError(e -> Log.e(TAG, "Error fetching files from Strava", e));
    }

    /**
     * Fetches the athlete's activities from Strava API.
     */
    private Single<List<StravaActivity>> fetchActivitiesFromStrava(String authHeader) {
        return stravaApiService.getAthleteActivities(authHeader, DEFAULT_ACTIVITIES_PER_PAGE, 1)
                .map(activities -> {
                    // Filter activities that have GPS data
                    List<StravaActivity> gpsActivities = new ArrayList<>();
                    for (StravaActivity activity : activities) {
                        if (activity.hasGpsData()) {
                            gpsActivities.add(activity);
                            if (gpsActivities.size() >= MAX_ACTIVITIES_TO_FETCH) {
                                break;
                            }
                        }
                    }
                    Log.d(TAG, "Found " + gpsActivities.size() + " activities with GPS data");
                    return gpsActivities;
                })
                .timeout(30, TimeUnit.SECONDS)
                .retry(2);
    }

    /**
     * Converts Strava activities to GPX files by fetching streams and generating GPX content.
     */
    private Single<List<File>> convertActivitiesToGpxFiles(List<StravaActivity> activities, Context context) {
        return tokenManager.getAuthorizationHeader()
                .flatMap(authHeader -> Single.fromCallable(() -> {
                    List<File> gpxFiles = new ArrayList<>();
                    
                    for (StravaActivity activity : activities) {
                        try {
                            // Add delay between requests to respect rate limits
                            if (!gpxFiles.isEmpty()) {
                                Thread.sleep(1000); // 1 second delay between API calls
                            }
                            
                            File gpxFile = convertActivityToGpxFile(activity, authHeader);
                            if (gpxFile != null) {
                                gpxFiles.add(gpxFile);
                                Log.d(TAG, "Converted activity " + activity.getId() + " to GPX: " + gpxFile.getName());
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to convert activity " + activity.getId() + " to GPX", e);
                            // Continue with other activities
                        }
                    }
                    
                    return gpxFiles;
                }).subscribeOn(Schedulers.io()));
    }

    /**
     * Converts a single Strava activity to a GPX file.
     */
    private File convertActivityToGpxFile(StravaActivity activity, String authHeader) throws Exception {
        try {
            // Fetch activity streams
            StravaStreamResponse streamResponse = stravaApiService.getActivityStreams(
                    authHeader, 
                    activity.getId(), 
                    REQUIRED_STREAM_KEYS, 
                    false
            ).blockingGet();

            // Convert to list for compatibility with existing code
            List<StravaStream> streams = StravaStreamMapper.responseToStreamList(streamResponse);

            if (streams == null || streams.isEmpty()) {
                Log.w(TAG, "No streams found for activity " + activity.getId());
                return null;
            }

            // Validate streams for GPX generation
            if (!StravaStreamMapper.validateStreamsForGpx(streams)) {
                Log.w(TAG, "Insufficient GPS data in streams for activity " + activity.getId());
                return null;
            }

            // Generate GPX content
            String gpxContent = StravaGpxGenerator.generateGpx(activity, streams);
            
            // Save to cache file
            File gpxFile = new File(cacheDirectory, activity.getSafeFilename());
            try (FileOutputStream fos = new FileOutputStream(gpxFile)) {
                fos.write(gpxContent.getBytes());
            }

            return gpxFile;
        } catch (Exception e) {
            Log.e(TAG, "Error converting activity " + activity.getId() + " to GPX", e);
            
            // Check for specific API format error
            if (e instanceof IllegalStateException && e.getMessage() != null && 
                e.getMessage().contains("Expected BEGIN_ARRAY but was BEGIN_OBJECT")) {
                Log.e(TAG, "API format error detected. Please update the app to support the latest Strava API format.");
            }
            
            throw e;
        }
    }

    /**
     * Parses GPX files using the provided parser function.
     */
    private Single<List<GpxFileInfo>> parseGpxFiles(List<File> gpxFiles, Function<File, GpxFileInfo> parserFunction) {
        return Single.fromCallable(() -> {
            List<GpxFileInfo> gpxFileInfos = new ArrayList<>();
            
            for (File gpxFile : gpxFiles) {
                try {
                    GpxFileInfo gpxFileInfo = parserFunction.apply(gpxFile);
                    if (gpxFileInfo != null) {
                        gpxFileInfos.add(gpxFileInfo);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to parse GPX file: " + gpxFile.getName(), e);
                }
            }
            
            return gpxFileInfos;
        }).subscribeOn(Schedulers.computation());
    }

    /**
     * Gets cached GPX files if they exist and are valid.
     */
    private Single<List<GpxFileInfo>> getCachedGpxFiles(Function<File, GpxFileInfo> parserFunction) {
        return Single.fromCallable(() -> {
            List<File> cachedFiles = new ArrayList<>();
            
            if (cacheDirectory != null && cacheDirectory.exists()) {
                File[] files = cacheDirectory.listFiles((dir, name) -> name.endsWith(".gpx"));
                if (files != null) {
                    for (File file : files) {
                        cachedFiles.add(file);
                    }
                }
            }
            
            return cachedFiles;
        })
        .flatMap(files -> parseGpxFiles(files, parserFunction))
        .subscribeOn(Schedulers.io());
    }

    /**
     * Initializes the cache directory for storing temporary GPX files.
     */
    private Single<File> initializeCacheDirectory(Context context) {
        return Single.fromCallable(() -> {
            if (cacheDirectory == null) {
                cacheDirectory = new File(context.getCacheDir(), CACHE_DIR_NAME);
                if (!cacheDirectory.exists()) {
                    boolean created = cacheDirectory.mkdirs();
                    if (!created) {
                        throw new IOException("Failed to create cache directory: " + cacheDirectory.getAbsolutePath());
                    }
                }
                Log.d(TAG, "Cache directory initialized: " + cacheDirectory.getAbsolutePath());
            }
            return cacheDirectory;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Checks if the cache is still valid based on the expiry time.
     */
    private boolean isCacheValid() {
        long currentTime = System.currentTimeMillis();
        long cacheAge = currentTime - lastCacheTime;
        long maxCacheAge = CACHE_EXPIRY_MINUTES * 60 * 1000; // Convert to milliseconds
        
        return cacheAge < maxCacheAge && cacheDirectory != null && cacheDirectory.exists();
    }

    /**
     * Checks if an error is related to authentication.
     */
    private boolean isAuthenticationError(Throwable error) {
        if (error instanceof HttpException) {
            HttpException httpError = (HttpException) error;
            return httpError.code() == 401; // Unauthorized
        }
        return false;
    }

    /**
     * Gets the OAuth 2.0 authorization URL for user authentication using default scopes.
     * Uses the required scopes for GPX Analyzer: "read,activity:read"
     * 
     * @return Authorization URL for redirecting users
     */
    public String getAuthorizationUrl() {
        return tokenManager.getAuthorizationUrl();
    }

    /**
     * Gets the OAuth 2.0 authorization URL for user authentication.
     * 
     * @param scopes Required scopes (e.g., "read,activity:read")
     * @return Authorization URL for redirecting users
     */
    public String getAuthorizationUrl(String scopes) {
        return tokenManager.getAuthorizationUrl(scopes);
    }

    /**
     * Checks if OAuth 2.0 credentials are properly configured.
     * 
     * @return true if client ID and client secret are configured
     */
    public boolean isOAuthConfigured() {
        return tokenManager.isOAuthConfigured();
    }

    /**
     * Manually refreshes the access token.
     * 
     * @return Single emitting success or error
     */
    public Single<Void> refreshToken() {
        return tokenManager.forceRefreshToken()
                .map(token -> {
                    Log.i(TAG, "Token refreshed successfully");
                    return (Void) null;
                });
    }

    /**
     * Clears all cached data including tokens and GPX files.
     */
    public void clearCache() {
        // Clear token cache
        tokenManager.clearTokens();
        
        // Clear GPX file cache
        if (cacheDirectory != null && cacheDirectory.exists()) {
            File[] files = cacheDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete cached file: " + file.getName());
                    }
                }
            }
            lastCacheTime = 0;
        }
        
        Log.d(TAG, "All cache cleared");
    }

    /**
     * Utility class for GPX generation from Strava activity streams.
     */
    private static class StravaGpxGenerator {
        
        /**
         * Generates GPX content from Strava activity and streams.
         */
        public static String generateGpx(StravaActivity activity, List<StravaStream> streams) {
            StringBuilder gpx = new StringBuilder();
            gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            gpx.append("<gpx version=\"1.1\" creator=\"GPXAnalyzer-StravaProxy\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
            gpx.append("  <metadata>\n");
            gpx.append("    <name>").append(escapeXml(activity.getName())).append("</name>\n");
            gpx.append("    <desc>Imported from Strava Activity ID: ").append(activity.getId()).append("</desc>\n");
            if (activity.getStartDateLocal() != null) {
                gpx.append("    <time>").append(activity.getStartDateLocal()).append("</time>\n");
            }
            gpx.append("  </metadata>\n");
            gpx.append("  <trk>\n");
            gpx.append("    <name>").append(escapeXml(activity.getName())).append("</name>\n");
            gpx.append("    <type>").append(escapeXml(activity.getType())).append("</type>\n");
            gpx.append("    <trkseg>\n");
            
            // Find streams
            StravaStream latlngStream = null;
            StravaStream altitudeStream = null;
            StravaStream timeStream = null;
            
            for (StravaStream stream : streams) {
                if (stream.isLatLngStream()) {
                    latlngStream = stream;
                } else if ("altitude".equals(stream.getType())) {
                    altitudeStream = stream;
                } else if ("time".equals(stream.getType())) {
                    timeStream = stream;
                }
            }
            
            // Generate track points
            if (latlngStream != null) {
                List<List<Double>> coordinates = latlngStream.getLatLngData();
                List<Double> altitudes = altitudeStream != null ? altitudeStream.getElevationData() : null;
                List<Integer> times = timeStream != null ? timeStream.getTimeData() : null;
                
                if (coordinates != null) {
                    for (int i = 0; i < coordinates.size(); i++) {
                        List<Double> coord = coordinates.get(i);
                        if (coord != null && coord.size() >= 2) {
                            gpx.append("      <trkpt lat=\"").append(coord.get(0)).append("\" lon=\"").append(coord.get(1)).append("\">\n");
                            
                            // Add elevation if available
                            if (altitudes != null && i < altitudes.size() && altitudes.get(i) != null) {
                                gpx.append("        <ele>").append(altitudes.get(i)).append("</ele>\n");
                            }
                            
                            // Add time if available
                            if (times != null && i < times.size() && times.get(i) != null && activity.getStartDate() != null) {
                                try {
                                    // Get start date and add seconds offset
                                    long startTimeMs = activity.getStartDate().getTime();
                                    long pointTimeMs = startTimeMs + (times.get(i) * 1000L);
                                    String timeStr = java.time.Instant.ofEpochMilli(pointTimeMs).toString();
                                    gpx.append("        <time>").append(timeStr).append("</time>\n");
                                } catch (Exception e) {
                                    // Skip time if parsing fails
                                }
                            }
                            
                            gpx.append("      </trkpt>\n");
                        }
                    }
                }
            }
            
            gpx.append("    </trkseg>\n");
            gpx.append("  </trk>\n");
            gpx.append("</gpx>\n");
            
            return gpx.toString();
        }

        /**
         * Validates that streams contain sufficient data for GPX generation.
         * @deprecated Use {@link com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.mapper.StravaStreamMapper#validateStreamsForGpx} instead.
         */
        @Deprecated
        public static boolean validateStreamsForGpx(List<StravaStream> streams) {
            return StravaStreamMapper.validateStreamsForGpx(streams);
        }

        /**
         * Escapes XML special characters.
         */
        private static String escapeXml(String text) {
            if (text == null) return "";
            return text.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;")
                      .replace("'", "&apos;");
        }
    }
}


