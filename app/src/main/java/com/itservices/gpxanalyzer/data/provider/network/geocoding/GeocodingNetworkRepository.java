package com.itservices.gpxanalyzer.data.provider.network.geocoding;

import com.itservices.gpxanalyzer.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.model.geocoding.ReverseGeocodingResponse;
import com.itservices.gpxanalyzer.data.network.GeocodingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import android.location.Location;
import android.util.Log;

/**
 * Repository class for handling geocoding operations through Maps.co API.
 * This class manages API rate limits and provides methods for both
 * forward and reverse geocoding.
 */
public class GeocodingNetworkRepository {

    private static final int MAX_DAILY_REQUESTS = 5000;   // Free tier daily limit
    private static final long RETRY_DELAY_MS = 2000; // Initial delay between retries in milliseconds
    private static final String TAG = "GeocodingRepository";

    private final GeocodingService geocodingService;
    private final String apiKey;
    private final GeocodingRequestQueue requestQueue;
    private int requestCount = 0;

    @Inject
    public GeocodingNetworkRepository(GeocodingService geocodingService,
                                      @GeocodingApiKey String apiKey,
                                      GeocodingRequestQueue requestQueue) {
        this.geocodingService = geocodingService;
        this.apiKey = apiKey;
        this.requestQueue = requestQueue;
    }

    /**
     * Forward geocodes an address to get its coordinates.
     * 
     * @param address Address to geocode
     * @return Single emitting the geocoding result
     */
    public Single<ForwardGeocodingResponse> geocodeAddress(String address) {
        Log.d(TAG, GeocodingNetworkRepository.this.hashCode() + " geocodeAddress() called with: address = [" + address + "]");

        if (address == null || address.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Address cannot be empty"));
        }

        if (isRateLimitExceeded()) {
            return Single.error(new RateLimitException("Daily request limit exceeded"));
        }

        return requestQueue.enqueue(
            geocodingService.forwardGeocode(address, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> incrementRequestCount())
                .retryWhen(errors -> errors.flatMap(error -> {
                    if (error instanceof HttpException) {
                        HttpException httpError = (HttpException) error;
                        switch (httpError.code()) {
                            case 429: // Too Many Requests
                                return Flowable.timer(RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                            case 503: // Service Unavailable
                                return Flowable.timer(RETRY_DELAY_MS * 2, TimeUnit.MILLISECONDS);
                            case 403: // Forbidden
                                return Flowable.error(new GeocodingException("API access blocked. Please contact support.", error));
                            default:
                                return Flowable.error(new GeocodingException("HTTP error: " + httpError.code(), error));
                        }
                    }
                    return Flowable.error(new GeocodingException("Failed to geocode address", error));
                }))
                .onErrorResumeNext(throwable -> {
                    return Single.error(new GeocodingException("Failed to geocode address", throwable));
                })
        );
    }

    /**
     * Forward geocodes an address with structured components.
     * 
     * @param street     Street address (with house number)
     * @param city       City name
     * @param state      State/province
     * @param country    Country
     * @param postalCode Postal code
     * @return Single emitting the geocoding result
     */
    public Single<ForwardGeocodingResponse> geocodeStructuredAddress(
            String street,
            String city,
            String state,
            String country,
            String postalCode) {

        if ((street == null || street.trim().isEmpty()) &&
            (city == null || city.trim().isEmpty()) &&
            (country == null || country.trim().isEmpty())) {
            return Single.error(new IllegalArgumentException("At least one address component must be provided"));
        }

        if (isRateLimitExceeded()) {
            return Single.error(new RateLimitException("Daily request limit exceeded"));
        }

        // Ensure non-null values for the API
        street = street != null ? street : "";
        city = city != null ? city : "";
        state = state != null ? state : "";
        country = country != null ? country : "";
        postalCode = postalCode != null ? postalCode : "";

        return requestQueue.enqueue(
            geocodingService.forwardGeocodeStructured(street, city, state, country, postalCode, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(response -> incrementRequestCount())
                .retryWhen(errors -> errors.flatMap(error -> {
                    if (error instanceof HttpException) {
                        HttpException httpError = (HttpException) error;
                        switch (httpError.code()) {
                            case 429: // Too Many Requests
                                return Flowable.timer(RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                            case 503: // Service Unavailable
                                return Flowable.timer(RETRY_DELAY_MS * 2, TimeUnit.MILLISECONDS);
                            case 403: // Forbidden
                                return Flowable.error(new GeocodingException("API access blocked. Please contact support.", error));
                            default:
                                return Flowable.error(new GeocodingException("HTTP error: " + httpError.code(), error));
                        }
                    }
                    return Flowable.error(new GeocodingException("Failed to geocode structured address", error));
                }))
                .onErrorResumeNext(throwable -> {
                    return Single.error(new GeocodingException("Failed to geocode structured address", throwable));
                })
        );
    }

    /**
     * Reverse geocodes a location to get its address.
     * 
     * @return Single emitting the reverse geocoding result
     */
    public Single<GeocodingResult> reverseGeocode(Location location) {
        if (isRateLimitExceeded()) {
            return Single.error(new RateLimitException("Daily request limit exceeded"));
        }

        return requestQueue.enqueue(
            geocodingService.reverseGeocode(location.getLatitude(), location.getLongitude(), apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ReverseGeocodingResponse::toGeocodingResult)
                .doOnSuccess(response -> incrementRequestCount())
                .retryWhen(errors -> errors.flatMap(error -> {
                    if (error instanceof HttpException) {
                        HttpException httpError = (HttpException) error;
                        switch (httpError.code()) {
                            case 429: // Too Many Requests
                                return Flowable.timer(RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                            case 503: // Service Unavailable
                                return Flowable.timer(RETRY_DELAY_MS * 2, TimeUnit.MILLISECONDS);
                            case 403: // Forbidden
                                return Flowable.error(new GeocodingException("API access blocked. Please contact support.", error));
                            default:
                                return Flowable.error(new GeocodingException("HTTP error: " + httpError.code(), error));
                        }
                    }
                    return Flowable.error(new GeocodingException("Failed to reverse geocode location", error));
                }))
                .onErrorResumeNext(throwable -> {
                    return Single.error(new GeocodingException("Failed to reverse geocode location", throwable));
                })
        );
    }

    /**
     * Batch geocodes multiple addresses respecting the rate limits.
     * 
     * @param addresses List of addresses to geocode
     * @return Single emitting a list of geocoding results
     */
    public Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        if (addresses.size() > MAX_DAILY_REQUESTS) {
            return Single.error(new RateLimitException("Batch size exceeds daily limit"));
        }

        if (isRateLimitExceeded()) {
            return Single.error(new RateLimitException("Daily request limit exceeded"));
        }

        // Process all addresses in parallel using different API keys
        return Flowable.fromIterable(addresses)
                .flatMapSingle(address -> {
                    // Get next API key in a round-robin fashion
                    String apiKey = getNextApiKey();
                    return geocodingService.forwardGeocode(address, apiKey)
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess(response -> incrementRequestCount())
                            .retryWhen(errors -> errors.flatMap(error -> {
                                if (error instanceof HttpException) {
                                    HttpException httpError = (HttpException) error;
                                    switch (httpError.code()) {
                                        case 429: // Too Many Requests
                                            return Flowable.timer(RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                                        case 503: // Service Unavailable
                                            return Flowable.timer(RETRY_DELAY_MS * 2, TimeUnit.MILLISECONDS);
                                        case 403: // Forbidden
                                            return Flowable.error(new GeocodingException("API access blocked. Please contact support.", error));
                                        default:
                                            return Flowable.error(new GeocodingException("HTTP error: " + httpError.code(), error));
                                    }
                                }
                                return Flowable.error(new GeocodingException("Failed to geocode address", error));
                            }))
                            .onErrorResumeNext(throwable -> {
                                return Single.error(new GeocodingException("Failed to geocode address", throwable));
                            });
                })
                .toList()
                .map(ArrayList::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "Starting batch geocoding with " + addresses.size() + " addresses");
                })
                .doOnSuccess(results -> {
                    Log.d(TAG, "Completed batch geocoding with " + results.size() + " results");
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error during batch geocoding: " + throwable.getMessage());
                });
    }

    /**
     * Batch reverse geocodes multiple locations respecting the rate limits.
     *
     * @param points List of GeoPoints to reverse geocode
     * @return Single emitting a list of reverse geocoding results
     */
    public Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points) {
        if (points == null || points.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        if (points.size() > MAX_DAILY_REQUESTS) {
            return Single.error(new RateLimitException("Batch size exceeds daily limit"));
        }

        if (isRateLimitExceeded()) {
            return Single.error(new RateLimitException("Daily request limit exceeded"));
        }

        // Process all points in parallel using different API keys
        return Flowable.fromIterable(points)
                .flatMapSingle(point -> {
                    // Get next API key in a round-robin fashion
                    String apiKey = getNextApiKey();
                    return geocodingService.reverseGeocode(point.getLatitude(), point.getLongitude(), apiKey)
                            .subscribeOn(Schedulers.io())
                            .map(ReverseGeocodingResponse::toGeocodingResult)
                            .doOnSuccess(response -> incrementRequestCount())
                            .retryWhen(errors -> errors.flatMap(error -> {
                                if (error instanceof HttpException) {
                                    HttpException httpError = (HttpException) error;
                                    switch (httpError.code()) {
                                        case 429: // Too Many Requests
                                            return Flowable.timer(RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
                                        case 503: // Service Unavailable
                                            return Flowable.timer(RETRY_DELAY_MS * 2, TimeUnit.MILLISECONDS);
                                        case 403: // Forbidden
                                            return Flowable.error(new GeocodingException("API access blocked. Please contact support.", error));
                                        default:
                                            return Flowable.error(new GeocodingException("HTTP error: " + httpError.code(), error));
                                    }
                                }
                                return Flowable.error(new GeocodingException("Failed to reverse geocode location", error));
                            }))
                            .onErrorResumeNext(throwable -> {
                                return Single.error(new GeocodingException("Failed to reverse geocode location", throwable));
                            });
                })
                .toList()
                .map(ArrayList::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "Starting batch reverse geocoding with " + points.size() + " points");
                })
                .doOnSuccess(results -> {
                    Log.d(TAG, "Completed batch reverse geocoding with " + results.size() + " results");
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error during batch reverse geocoding: " + throwable.getMessage());
                });
    }

    private String getNextApiKey() {
        // TODO: Implement round-robin API key selection
        // For now, just return the default API key
        return apiKey;
    }

    private synchronized void incrementRequestCount() {
        requestCount++;
    }

    private synchronized boolean isRateLimitExceeded() {
        return requestCount >= MAX_DAILY_REQUESTS;
    }

    /**
     * Resets the request counter.
     * Should be called once per day, ideally at midnight.
     */
    public synchronized void resetDailyCounter() {
        requestCount = 0;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Custom exception for geocoding errors.
     */
    public static class GeocodingException extends IOException {
        public GeocodingException(String message) {
            super(message);
        }

        public GeocodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom exception for rate limit errors.
     */
    public static class RateLimitException extends IOException {
        public RateLimitException(String message) {
            super(message);
        }
    }
} 