package com.itservices.gpxanalyzer.examples;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itservices.gpxanalyzer.data.model.opentopodata.ElevationResult;
import com.itservices.gpxanalyzer.data.model.opentopodata.OpenTopoDataResponse;
import com.itservices.gpxanalyzer.data.network.AltitudeService;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A standalone example that demonstrates how to fetch elevation data from Open Topo Data API
 * for 10 coordinates. This example can be run without Android dependencies.
 * 
 * Note: For simplicity, this standalone example doesn't use Hilt dependency injection
 * and creates all dependencies manually.
 */
public class StandaloneElevationExample {

    // Central location for results tracking
    private static final List<ElevationResult> allResults = new ArrayList<>();
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static Exception caughtException = null;

    /**
     * Main method to run the example.
     * Note: This can only be run in a JVM environment, not on Android directly.
     */
    public static void main(String[] args) {
        System.out.println("Starting Elevation Data Example...");
        
        try {
            // 1. Set up dependencies manually 
            OkHttpClient client = createOkHttpClient();
            AltitudeService altitudeService = createAltitudeService(client);
            
            // 2. Create the list of 10 coordinates
            List<GeoPoint> coordinates = createSampleCoordinates();
            System.out.println("Fetching elevation data for " + coordinates.size() + " coordinates...");
            
            // 3. Fetch elevation data
            fetchElevationData(altitudeService, coordinates);
            
            // 4. Wait for the async call to complete (max 30 seconds)
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            
            // 5. Process results
            if (completed) {
                if (caughtException != null) {
                    System.err.println("Error: " + caughtException.getMessage());
                    caughtException.printStackTrace();
                } else {
                    System.out.println("\nResults:");
                    System.out.println("----------");
                    for (ElevationResult result : allResults) {
                        System.out.printf(
                                "Location: %.6f, %.6f - Elevation: %.2f meters (Dataset: %s)%n",
                                result.location.latitude,
                                result.location.longitude,
                                result.elevation,
                                result.dataset
                        );
                    }
                }
            } else {
                System.err.println("Operation timed out after 30 seconds!");
            }
            
        } catch (Exception e) {
            System.err.println("Unhandled exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\nExample completed.");
    }
    
    /**
     * Creates a properly configured OkHttpClient.
     */
    private static OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
                System.out.println("HTTP: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Creates the AltitudeService retrofit interface.
     */
    private static AltitudeService createAltitudeService(OkHttpClient client) {
        Gson gson = new GsonBuilder().create();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AltitudeService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        
        return retrofit.create(AltitudeService.class);
    }
    
    /**
     * Fetches elevation data for the given coordinates.
     * This implementation manually handles the batching and rate limiting.
     */
    private static void fetchElevationData(AltitudeService service, List<GeoPoint> coordinates) {
        // For simplicity in this example, we'll assume the list is small (≤100 points)
        // For larger lists, implement batching as in AltitudeRepository
        
        if (coordinates.isEmpty()) {
            latch.countDown(); // Signal completion
            return;
        }
        
        // Format coordinates for API
        String locationsParam = coordinates.stream()
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p.getLatitude(), p.getLongitude()))
                .collect(Collectors.joining("|"));
        
        // Execute API call
        service.getElevation(locationsParam)
               .subscribeOn(Schedulers.io())
               .subscribe(
                   // Success handler
                   response -> {
                       if ("OK".equalsIgnoreCase(response.status) && response.results != null) {
                           allResults.addAll(response.results);
                           latch.countDown(); // Signal completion
                       } else {
                           caughtException = new IOException("API Error: " + response.status);
                           latch.countDown(); // Signal completion
                       }
                   },
                   // Error handler
                   error -> {
                       caughtException = new IOException("Error fetching elevation data", error);
                       latch.countDown(); // Signal completion
                   }
               );
    }
    
    /**
     * Creates a list of sample coordinates (major cities).
     */
    private static List<GeoPoint> createSampleCoordinates() {
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
} 