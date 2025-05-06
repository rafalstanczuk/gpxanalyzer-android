package com.itservices.gpxanalyzer.examples;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.provider.geocoding.network.GeocodingNetworkRepository;
import com.itservices.gpxanalyzer.data.provider.geocoding.network.GeocodingNetworkRouterRepository;
import com.itservices.gpxanalyzer.data.provider.geocoding.network.GeocodingRequestQueue.GeocodingException;
import com.itservices.gpxanalyzer.utils.location.LocationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Example class demonstrating how to use the geocoding service to convert
 * coordinates to addresses and vice versa. This class is meant for educational purposes.
 */
public class GeocodingExample {

    private static final String TAG = "GeocodingExample";
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    private final GeocodingNetworkRepository geocodingRepository;

    private final GeocodingNetworkRouterRepository geocodingRouterRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public GeocodingExample(GeocodingNetworkRepository geocodingRepository, GeocodingNetworkRouterRepository geocodingRouterRepository) {
        this.geocodingRepository = geocodingRepository;
        this.geocodingRouterRepository = geocodingRouterRepository;
    }

    /**
     * Demonstrates how to perform forward geocoding (address to coordinates)
     * for a list of sample addresses.
     */
    public void performForwardGeocoding() {
        List<String> addresses = createSampleAddresses();

        for (String address : addresses) {
            Disposable disposable = geocodingRepository.geocodeAddress(address)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> handleGeocodingSuccess(address, response),
                            error -> handleGeocodingError(address, error)
                    );

            disposables.add(disposable);
        }
    }

    /**
     * Demonstrates how to perform reverse geocoding (coordinates to address)
     * for a list of sample coordinates.
     */
    public void performReverseGeocoding() {
        List<Location> coordinates = LocationUtils.createSampleCoordinates();
        int delayBetweenRequests = 1000; // 1 second between requests

        for (int i = 0; i < coordinates.size(); i++) {
            Location coordinate = coordinates.get(i);
            
            Disposable disposable = geocodingRepository.reverseGeocode(coordinate)
                    .subscribeOn(Schedulers.io())
                    .delay(i * delayBetweenRequests, TimeUnit.MILLISECONDS) // Stagger requests
                    .retry((retryCount, error) -> {
                        if (retryCount < MAX_RETRIES && error instanceof GeocodingException) {
                            Log.w(TAG, String.format(
                                "Retrying reverse geocoding for coordinates %f, %f (attempt %d)",
                                coordinate.getLatitude(),
                                coordinate.getLongitude(),
                                retryCount + 1
                            ));
                            return true;
                        }
                        return false;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> handleReverseGeocodingSuccess(coordinate, result),
                            error -> handleReverseGeocodingError(coordinate, error)
                    );

            disposables.add(disposable);
        }
    }

    /**
     * Demonstrates how to perform batch reverse geocoding for multiple coordinates at once.
     * This is more efficient than individual requests as it reduces the number of API calls.
     */
    public void performBatchReverseGeocoding() {
        List<Location> coordinates = LocationUtils.createSampleCoordinates();
        
        Disposable disposable = geocodingRepository.batchReverseGeocode(coordinates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> handleBatchReverseGeocodingSuccess(coordinates, results),
                        error -> handleBatchReverseGeocodingError(error)
                );

        disposables.add(disposable);
    }

    /**
     * Demonstrates how to perform batch forward geocoding for multiple addresses at once.
     * This is more efficient than individual requests as it reduces the number of API calls.
     */
    public void performBatchForwardGeocoding() {
        List<String> addresses = createSampleAddresses();
        
        Disposable disposable = geocodingRepository.batchGeocodeAddresses(addresses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> handleBatchForwardGeocodingSuccess(addresses, results),
                        error -> handleBatchForwardGeocodingError(error)
                );

        disposables.add(disposable);
    }

    public void performBatchReverseGeocodingUsingGeocodingRouterRepository() {
        Log.d(TAG, "performBatchReverseGeocodingUsingGeocodingRouterRepository() called");

        List<Location> coordinates = LocationUtils.createSampleCoordinates();

        Disposable disposable = geocodingRouterRepository.batchReverseGeocode(coordinates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> handleBatchReverseGeocodingSuccess(coordinates, results),
                        error -> handleBatchReverseGeocodingError(error)
                );

        disposables.add(disposable);
    }

    public void performBatchForwardGeocodingUsingGeocodingRouterRepository() {
        Log.d(TAG, "performBatchForwardGeocodingUsingGeocodingRouterRepository() called");

        List<String> addresses = createSampleAddresses();

        Disposable disposable = geocodingRouterRepository.batchGeocodeAddresses(addresses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        results -> handleBatchForwardGeocodingSuccess(addresses, results),
                        error -> handleBatchForwardGeocodingError(error)
                );

        disposables.add(disposable);
    }

    /**
     * Cleans up resources when the example is no longer needed.
     * This is important to prevent memory leaks from RxJava subscriptions.
     */
    public void cleanup() {
        disposables.clear();
    }

    private void handleGeocodingSuccess(String address, ForwardGeocodingResponse response) {
        if (!response.isEmpty()) {
            GeocodingResult firstResult = response.get(0);
            Log.d(TAG, String.format(
                    "Geocoding Address: %s\nFound at: %f, %f\nDisplay Name: %s",
                    address,
                    firstResult.latitude,
                    firstResult.longitude,
                    firstResult.displayName
            ));
        } else {
            Log.d(TAG, "No results found for address: " + address);
        }
    }

    private void handleGeocodingError(String address, Throwable error) {
        Log.e(TAG, "Error geocoding address: " + address, error);
    }

    private void handleReverseGeocodingSuccess(Location coordinate, GeocodingResult result) {
        Log.d(TAG, String.format(
                "Coordinates: %f, %f\nFound address: %s",
                coordinate.getLatitude(),
                coordinate.getLongitude(),
                result.getFormattedAddress()
        ));
    }

    private void handleReverseGeocodingError(Location coordinate, Throwable error) {
        Log.e(TAG, String.format(
                "Error reverse geocoding coordinates: %f, %f: %s",
                coordinate.getLatitude(),
                coordinate.getLongitude(),
                error.getMessage()
        ), error);
    }

    private void handleBatchReverseGeocodingSuccess(List<Location> coordinates, List<GeocodingResult> results) {
        // Sort coordinates by latitude and longitude
        coordinates.sort(Comparator
                .comparingDouble(Location::getLatitude)
                .thenComparingDouble(Location::getLongitude));

        // Sort results by latitude and longitude
        results.sort(Comparator
                .comparingDouble((GeocodingResult a) -> a.latitude)
                .thenComparingDouble(a -> a.longitude));

        if (results.size() != coordinates.size()) {
            Log.w(TAG, String.format(
                "Batch reverse geocoding returned %d results for %d coordinates",
                results.size(),
                coordinates.size()
            ));
        }

        for (int i = 0; i < Math.min(coordinates.size(), results.size()); i++) {
            Location coordinate = coordinates.get(i);
            GeocodingResult result = results.get(i);
            Log.d(TAG, String.format(
                    "Batch Result %d: Coordinates %f, %f\nFound address: %s",
                    i + 1,
                    coordinate.getLatitude(),
                    coordinate.getLongitude(),
                    result.getFormattedAddress()
            ));
        }
    }

    private void handleBatchReverseGeocodingError(Throwable error) {
        Log.e(TAG, "Error in batch reverse geocoding: " + error.getMessage(), error);
    }

    private void handleBatchForwardGeocodingSuccess(List<String> addresses, List<ForwardGeocodingResponse> results) {
        if (results.size() != addresses.size()) {
            Log.w(TAG, String.format(
                "Batch forward geocoding returned %d results for %d addresses",
                results.size(),
                addresses.size()
            ));
        }

        for (int i = 0; i < Math.min(addresses.size(), results.size()); i++) {
            String address = addresses.get(i);
            ForwardGeocodingResponse response = results.get(i);

            if (!response.isEmpty()) {
                GeocodingResult firstResult = response.get(0);
                Log.d(TAG, String.format(
                        "Batch Forward Result %d:\nAddress: %s\nFound at: %f, %f\nDisplay Name: %s",
                        i + 1,
                        address,
                        firstResult.latitude,
                        firstResult.longitude,
                        firstResult.displayName
                ));
            } else {
                Log.w(TAG, String.format(
                        "Batch Forward Result %d: No results found for address: %s",
                        i + 1,
                        address
                ));
            }
        }
    }

    private void handleBatchForwardGeocodingError(Throwable error) {
        Log.e(TAG, "Error in batch forward geocoding: " + error.getMessage(), error);
    }

    /**
     * Creates a list of sample addresses for demonstration purposes.
     */
    private List<String> createSampleAddresses() {
        List<String> addresses = new ArrayList<>();
        // Famous landmarks
        addresses.add("350 5th Ave, New York, NY"); // Empire State Building
        addresses.add("221B Baker Street, London, UK"); // Sherlock Holmes' address
        addresses.add("Tour Eiffel, Paris, France"); // Eiffel Tower
        addresses.add("1600 Amphitheatre Parkway, Mountain View, CA"); // Google HQ
        addresses.add("1 Infinite Loop, Cupertino, CA"); // Apple HQ
        // More landmarks
        addresses.add("Brandenburg Gate, Pariser Platz, Berlin, Germany");
        addresses.add("Colosseum, Piazza del Colosseo, Rome, Italy");
        addresses.add("Sydney Opera House, Australia");
        addresses.add("Christ the Redeemer, Brazil");
        addresses.add("Taj Mahal, India");

        addresses.addAll(addresses);

        addresses.addAll(addresses);

        addresses.addAll(addresses);

        return addresses;
    }

    /**
     * Example of how to use this class in an Activity or Fragment.
     * Note: This is just a code fragment, not meant to be executed directly.
     */
    /*
    // In an Activity or Fragment:
    private GeocodingExample geocodingExample;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the repository from Hilt
        GeocodingRepository repository = ...; // Injected by Hilt
        
        // Create the example
        geocodingExample = new GeocodingExample(repository);
        
        // Perform geocoding when needed
        geocodingExample.performForwardGeocoding();
        geocodingExample.performReverseGeocoding();
    }
    
    @Override
    protected void onDestroy() {
        // Clean up resources
        geocodingExample.cleanup();
        super.onDestroy();
    }
    */
} 