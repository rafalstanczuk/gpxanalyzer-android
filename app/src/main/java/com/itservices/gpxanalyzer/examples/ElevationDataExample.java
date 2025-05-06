package com.itservices.gpxanalyzer.examples;

import android.util.Log;

import com.itservices.gpxanalyzer.data.model.opentopodata.ElevationResult;
import com.itservices.gpxanalyzer.data.provider.altitude.network.AltitudeRepository;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Example class demonstrating how to fetch elevation data for 10 coordinates
 * using the AltitudeRepository. This is meant for educational purposes.
 */
public class ElevationDataExample {

    private static final String TAG = "ElevationDataExample";

    private final AltitudeRepository altitudeRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public ElevationDataExample(AltitudeRepository altitudeRepository) {
        this.altitudeRepository = altitudeRepository;
    }

    /**
     * Fetches elevation data for 10 major cities around the world.
     * This demonstrates how to use the AltitudeRepository with RxJava.
     */
    public void fetchElevationForTenCities() {
        // 1. Create a list of 10 coordinates (major cities)
        List<GeoPoint> coordinates = createSampleCityCoordinates();
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);
        coordinates.addAll(coordinates);

        // 2. Fetch elevations using AltitudeRepository
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

    private void handleSuccess(List<ElevationResult> results) {
        Log.d(TAG, "Successfully retrieved " + results.size() + " elevation results");
int i=0;
        // Process and display each elevation result
        for (ElevationResult result : results) {
            Log.d(TAG, String.format(
                    i++ + "Location: %f, %f - Elevation: %.2f meters",
                    result.location.latitude,
                    result.location.longitude,
                    result.elevation));
        }
    }

    private void handleError(Throwable error) {
        Log.e(TAG, "Error fetching elevation data: " + error.getMessage(), error);
    }

    /**
     * Creates a list of sample coordinates for major cities around the world.
     */
    private List<GeoPoint> createSampleCityCoordinates() {
        List<GeoPoint> coordinates = new ArrayList<>();

        // Add coordinates for major cities
        coordinates.add(new GeoPoint(37.7749, -122.4194)); // San Francisco
        coordinates.add(new GeoPoint(40.7128, -74.0060));  // New York
        coordinates.add(new GeoPoint(51.5074, -0.1278));   // London
        coordinates.add(new GeoPoint(48.8566, 2.3522));    // Paris
        coordinates.add(new GeoPoint(35.6762, 139.6503));  // Tokyo
        coordinates.add(new GeoPoint(-33.8688, 151.2093)); // Sydney
        coordinates.add(new GeoPoint(-22.9068, -43.1729)); // Rio de Janeiro
        coordinates.add(new GeoPoint(55.7558, 37.6173));   // Moscow
        coordinates.add(new GeoPoint(28.6139, 77.2090));   // New Delhi
        coordinates.add(new GeoPoint(39.9042, 116.4074));  // Beijing

        return coordinates;
    }

    /**
     * Example of how to use this class in an Activity or Fragment.
     * Note: This is just a code fragment, not meant to be executed directly.
     */
    /*
    // In an Activity or Fragment:
    private ElevationDataExample elevationExample;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the repository from Hilt
        AltitudeRepository repository = ...; // Injected by Hilt
        
        // Create the example
        elevationExample = new ElevationDataExample(repository);
        
        // Fetch elevations when needed
        elevationExample.fetchElevationForTenCities();
    }
    
    @Override
    protected void onDestroy() {
        // Clean up resources
        elevationExample.cleanup();
        super.onDestroy();
    }
    */
} 