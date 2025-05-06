package com.itservices.gpxanalyzer.examples;

import android.util.Log;

import com.itservices.gpxanalyzer.data.model.opentopodata.ElevationResult;
import com.itservices.gpxanalyzer.data.provider.altitude.network.AltitudeRepository;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Example demonstrating how to fetch elevation data for many coordinates
 * using the AltitudeRepository with POST requests for large batches.
 */
public class PostElevationExample {
    private static final String TAG = "PostElevationExample";
    
    private final AltitudeRepository altitudeRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public PostElevationExample(AltitudeRepository altitudeRepository) {
        this.altitudeRepository = altitudeRepository;
    }

    /**
     * Fetches elevation data for many coordinates.
     * The AltitudeRepository will automatically use POST when the request
     * payload exceeds the URL length threshold.
     */
    public void fetchElevationForManyCoordinates() {
        // Create a large list of coordinates (e.g., 200 random points)
        List<GeoPoint> coordinates = generateRandomCoordinates(200);
        
        Log.d(TAG, "Fetching elevation data for " + coordinates.size() + " coordinates");
        
        // Fetch elevations using AltitudeRepository
        Disposable disposable = altitudeRepository.getAltitudeForPoints(coordinates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError);
        
        // Add the disposable to the CompositeDisposable for lifecycle management
        disposables.add(disposable);
    }

    /**
     * Cleans up resources when the example is no longer needed.
     * This is important to prevent memory leaks from RxJava subscriptions.
     */
    public void cleanup() {
        disposables.clear();
    }

    /**
     * Handle successful API response
     */
    private void handleSuccess(List<ElevationResult> results) {
        Log.d(TAG, "Successfully retrieved " + results.size() + " elevation results");
        
        // Display sample results (first 5)
        for (int i = 0; i < Math.min(5, results.size()); i++) {
            ElevationResult result = results.get(i);
            Log.d(TAG, String.format(Locale.US, 
                    "Sample result %d: Location (%.6f, %.6f) has elevation: %.2f meters",
                    i + 1, 
                    result.location.latitude,
                    result.location.longitude,
                    result.elevation));
        }
        
        if (results.size() > 5) {
            Log.d(TAG, String.format(Locale.US, "... and %d more results", 
                    results.size() - 5));
        }
    }

    /**
     * Handle API errors
     */
    private void handleError(Throwable error) {
        Log.e(TAG, "Error fetching elevation data: " + error.getMessage(), error);
    }
    
    /**
     * Generate random coordinates for testing
     */
    private List<GeoPoint> generateRandomCoordinates(int count) {
        List<GeoPoint> points = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            // Generate random lat/lon within reasonable range
            double lat = (random.nextDouble() * 170) - 85; // -85 to +85
            double lon = (random.nextDouble() * 360) - 180; // -180 to +180
            
            points.add(new GeoPoint(lat, lon));
        }
        
        return points;
    }
    
    /**
     * Example of how to use this class in an Activity or Fragment.
     * Note: This is just a code fragment, not meant to be executed directly.
     */
    /*
    // In an Activity or Fragment:
    private PostElevationExample elevationExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // PostElevationExample will be injected by Hilt
        @Inject PostElevationExample elevationExample;
        
        // Fetch elevations when needed
        elevationExample.fetchElevationForManyCoordinates();
    }
    
    @Override
    protected void onDestroy() {
        // Clean up resources
        elevationExample.cleanup();
        super.onDestroy();
    }
    */
} 