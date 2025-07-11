package com.itservices.gpxanalyzer.core.data.provider.altitude.network;

import com.itservices.gpxanalyzer.core.data.model.opentopodata.ElevationResult;
import com.itservices.gpxanalyzer.core.data.model.opentopodata.OpenTopoDataResponse;
import com.itservices.gpxanalyzer.core.data.network.AltitudeService;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


@Singleton
public class AltitudeRepository {

    private static final int MAX_LOCATIONS_PER_REQUEST = 100;
    private static final long REQUEST_INTERVAL_MS = 1000; // 1 call per second
    private static final int URL_LENGTH_THRESHOLD = 2000; // Threshold to switch to POST method

    private final AltitudeService altitudeService;

    @Inject
    public AltitudeRepository(AltitudeService altitudeService) {
        this.altitudeService = altitudeService;
    }

    /**
     * Fetches altitude data for a list of GeoPoints from the Open Topo Data API.
     * Handles batching (max 100 locations per request) and throttling (1 request per second).
     *
     * @param points List of GeoPoint objects.
     * @return A Single emitting a combined list of all ElevationResults or an error.
     */
    public Single<ArrayList<ElevationResult>> getAltitudeForPoints(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        List<List<GeoPoint>> batches = partitionList(points, MAX_LOCATIONS_PER_REQUEST);

        // Create a Flowable that emits batch indices with a 1-second delay between each
        Flowable<Long> interval = Flowable.intervalRange(0, batches.size(), 0, REQUEST_INTERVAL_MS, TimeUnit.MILLISECONDS, Schedulers.io());

        return Flowable.fromIterable(batches)
                .zipWith(interval, (batch, timer) -> batch) // Combine batch with interval to enforce delay
                .flatMapSingle(this::fetchBatch) // Fetch data for each batch
                .subscribeOn(Schedulers.io()) // Perform network calls and batch processing on IO thread
                .reduce(new ArrayList<ElevationResult>(), (combinedList, batchResult) -> {
                    combinedList.addAll(batchResult);
                    return combinedList;
                }) // Combine results from all batches
                .observeOn(AndroidSchedulers.mainThread()); // Observe final result on Main thread (adjust if needed)
    }

    /**
     * Fetches elevation data for a single batch of points.
     * Uses GET for small batches and POST for larger ones to avoid URL length limitations.
     */
    private Single<List<ElevationResult>> fetchBatch(List<GeoPoint> batch) {
        String locationsParam = batch.stream()
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p.getLatitude(), p.getLongitude()))
                .collect(Collectors.joining("|"));
        
        // Estimate URL length (rough approximation)
        boolean useLargePayload = locationsParam.length() > URL_LENGTH_THRESHOLD;
        
        // Choose between GET and POST based on payload size
        Single<OpenTopoDataResponse> apiCall;
        
        if (useLargePayload) {
            // Use POST for large payloads
            Map<String, String> params = new HashMap<>();
            params.put("locations", locationsParam);
            params.put("interpolation", "bilinear"); // Optional: Add other parameters as needed
            
            apiCall = altitudeService.postElevation(params);
        } else {
            // Use GET for small payloads
            apiCall = altitudeService.getElevation(locationsParam);
        }

        return apiCall.flatMap(response -> {
                    if ("OK".equalsIgnoreCase(response.status) && response.results != null) {
                        return Single.just(response.results);
                    } else {
                        // Handle API errors (e.g., invalid location, rate limit message)
                        // Consider logging the error status
                        return Single.error(new ApiException("API Error: " + response.status + " for batch starting with " + batch.get(0)));
                    }
                })
                .onErrorResumeNext(throwable -> {
                    // Handle network errors or other exceptions during the API call for this batch
                    // Decide if you want to fail the whole operation or just return an empty list for this batch
                    // Log.e("AltitudeRepository", "Error fetching batch", throwable);
                    return Single.error(new ApiException("Network or processing error for batch", throwable));
                     // Or return Single.just(Collections.emptyList()); to ignore errors for a batch
                });
    }

    /**
     * Partitions a list into sublists of a specified size.
     */
    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(
                    list.subList(i, Math.min(i + size, list.size())))
            );
        }
        return partitions;
    }

    /** Custom exception for API or related errors. */
    public static class ApiException extends IOException {
        public ApiException(String message) {
            super(message);
        }
         public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 