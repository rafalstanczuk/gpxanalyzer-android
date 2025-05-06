package com.itservices.gpxanalyzer.data.provider.geocoding.network;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.provider.geocoding.BaseGeocodingRepository;
import com.itservices.gpxanalyzer.data.provider.geocoding.GeocodingException;
import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.PercentageUpdateEventSourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Repository that distributes geocoding requests across multiple geocoding providers.
 * It also implements the BaseGeocodingRepository itself to provide a unified interface.
 */
public class GeocodingNetworkRouterRepository implements BaseGeocodingRepository {
    private static final String TAG = "GeocodingRouterRepository";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final GlobalEventWrapper events;
    private final List<BaseGeocodingRepository> repositories;
    private final Scheduler scheduler;
    private final AtomicInteger currentRepoIndex = new AtomicInteger(0);
    private AtomicInteger progress;
    private AtomicReference<EventProgress> lastEventAtomic = new AtomicReference<>();

    public GeocodingNetworkRouterRepository(GlobalEventWrapper events, List<BaseGeocodingRepository> repositories, Scheduler scheduler) {
        this.events = events;
        this.repositories = repositories;
        this.scheduler = scheduler;
    }

    private BaseGeocodingRepository getNextRepository() {
        if (repositories == null || repositories.isEmpty()) {
            Log.e(TAG, "No geocoding repositories configured for GeocodingNetworkRouterRepository.");
            // Return a repository that will immediately error out.
            return new BaseGeocodingRepository() {
                private Single error() { return Single.error(new IllegalStateException("No geocoding repositories configured.")); }
                @Override public Single<GeocodingResult> reverseGeocode(Location location) { return error(); }
                @Override public Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points) { return error(); }
                @Override public Single<ForwardGeocodingResponse> geocodeAddress(String address) { return error(); }
                @Override public Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses) { return error(); }
                @Override public Single<ForwardGeocodingResponse> geocodeStructuredAddress(String street, String city, String state, String country, String postalCode) { return error(); }
            };
        }
        return repositories.get(currentRepoIndex.getAndIncrement() % repositories.size());
    }

    // Implementation for single reverseGeocode
    @Override
    public Single<GeocodingResult> reverseGeocode(Location location) {
        if (location == null) {
            return Single.error(new IllegalArgumentException("Location cannot be null for reverseGeocode."));
        }
        // Delegate to one of the repositories. Simpler error handling/retry than batch.
        // The underlying repository is expected to handle its own retries if applicable.
        // For simplicity, not adding the router-level retry logic here as in batch methods.
        BaseGeocodingRepository repository = getNextRepository();
        return repository.reverseGeocode(location)
            .doOnError(error -> Log.e(TAG, "Error in single reverseGeocode for location: " + location, error));
    }

    // Implementation for single geocodeAddress
    @Override
    public Single<ForwardGeocodingResponse> geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("Address cannot be null or empty for geocodeAddress."));
        }
        BaseGeocodingRepository repository = getNextRepository();
        return repository.geocodeAddress(address)
            .doOnError(error -> Log.e(TAG, "Error in single geocodeAddress for address: " + address, error));
    }

    // Implementation for single geocodeStructuredAddress
    @Override
    public Single<ForwardGeocodingResponse> geocodeStructuredAddress(String street, String city, String state, String country, String postalCode) {
        // Basic validation, can be enhanced
        if ((street == null || street.trim().isEmpty()) && 
            (city == null || city.trim().isEmpty()) && 
            (country == null || country.trim().isEmpty())) {
            return Single.error(new IllegalArgumentException("At least one key address component (street, city, country) must be provided for structured geocoding."));
        }
        BaseGeocodingRepository repository = getNextRepository();
        return repository.geocodeStructuredAddress(street, city, state, country, postalCode)
            .doOnError(error -> Log.e(TAG, "Error in single geocodeStructuredAddress", error));
    }

    @Override
    public Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) { // Changed from addresses.isEmpty() to allow null
            return Single.just(new ArrayList<>());
        }

        currentRepoIndex.set(0);
        startEventProgress(addresses);
        List<Observable<ForwardGeocodingResponse>> tasks = new ArrayList<>();

        for (String address : addresses) {
            BaseGeocodingRepository repository = getNextRepository();
            tasks.add(Observable.just(address)
                    .subscribeOn(scheduler)
                    .flatMapSingle(addr -> repository.geocodeAddress(addr) // Use flatMapSingle for Single source
                        .onErrorResumeNext(error -> {
                             Log.e(TAG, "Error processing address: " + addr + " within batch, returning empty ForwardGeocodingResponse", error);
                             ForwardGeocodingResponse emptyResponse = new ForwardGeocodingResponse();
                             // Optionally, populate with error indicator or original address
                             return Single.just(emptyResponse); // Allow batch to continue
                        })
                    )
                    .doOnNext(geocodingResults -> updateEventProgress(addresses))
                    // Router-level retry for the task of getting a response from a sub-repository
                    .retryWhen(errors -> errors
                            .zipWith(Observable.range(1, MAX_RETRIES), (throwable, retryCount) -> {
                                // Only retry for specific, potentially transient errors if desired.
                                // Here, GeocodingException is from GeocodingNetworkRepository, might need broader checks
                                if (throwable instanceof GeocodingException || throwable instanceof java.io.IOException) {
                                    Log.w(TAG, String.format(
                                            "Retry %d/%d for address %s in batch after error: %s",
                                            retryCount, MAX_RETRIES, address, throwable.getMessage()
                                    ));
                                    return retryCount;
                                }
                                throw new RuntimeException("Non-retryable error in batchGeocodeAddresses: " + throwable.getMessage(), throwable);
                            })
                            .flatMap(retryCount -> Observable.timer(
                                    RETRY_DELAY_MS * retryCount,
                                    TimeUnit.MILLISECONDS,
                                    scheduler
                            )))
            );
        }

        return Observable.merge(tasks)
                .collect(ArrayList<ForwardGeocodingResponse>::new, ArrayList::add)
                .doOnSubscribe(disposable -> { /*Log.d(TAG, "Starting batch geocoding with " + addresses.size() + " addresses")*/ })
                .doOnSuccess(results -> {
                    if (results.size() != addresses.size()) {
                        Log.w(TAG, "Warning: Number of results (" + results.size() +
                                ") does not match number of input addresses (" + addresses.size() + ") due to errors in individual items.");
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "Error in final merge of batch geocoding: " + error.getMessage());
                });
                // Removed previous onErrorResumeNext that would hide all errors completely.
                // Individual errors are handled with onErrorResumeNext within the loop.
    }

    @Override
    public Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points) {
        Log.d(TAG, "batchReverseGeocode() called with: points = [" + (points == null ? "null" : String.valueOf(points.size())) + "]");

        if (points == null || points.isEmpty()) { // Changed from points.isEmpty() to allow null
            return Single.just(new ArrayList<>());
        }

        currentRepoIndex.set(0);
        startEventProgress(points);
        List<Observable<GeocodingResult>> tasks = new ArrayList<>();

        for (Location point : points) {
            BaseGeocodingRepository repository = getNextRepository();
            tasks.add(Observable.just(point)
                    .subscribeOn(scheduler)
                    .flatMapSingle(p -> repository.reverseGeocode(p) // Use flatMapSingle for Single source
                        .onErrorResumeNext(error -> {
                            Log.e(TAG, "Error processing point: " + p + " within batch, returning empty GeocodingResult", error);
                            GeocodingResult emptyResult = new GeocodingResult();
                            if(p != null) { // Guard against null point if it could happen
                                emptyResult.latitude = p.getLatitude();
                                emptyResult.longitude = p.getLongitude();
                            }
                            emptyResult.displayName = "Error: Geocoding failed for this point";
                            return Single.just(emptyResult); // Allow batch to continue
                        })
                    )
                    .doOnNext(geocodingResults -> updateEventProgress(points))
                    // Router-level retry for the task of getting a response from a sub-repository
                    .retryWhen(errors -> errors
                            .zipWith(Observable.range(1, MAX_RETRIES), (throwable, retryCount) -> {
                                if (throwable instanceof GeocodingException || throwable instanceof java.io.IOException) {
                                    Log.w(TAG, String.format(
                                            "Retry %d/%d for point %s in batch after error: %s",
                                            retryCount, MAX_RETRIES, point, throwable.getMessage()
                                    ));
                                    return retryCount;
                                }
                                throw new RuntimeException("Non-retryable error in batchReverseGeocode: " + throwable.getMessage(), throwable);
                            })
                            .flatMap(retryCount -> Observable.timer(
                                    RETRY_DELAY_MS * retryCount,
                                    TimeUnit.MILLISECONDS,
                                    scheduler
                            )))
            );
        }

        return Observable.merge(tasks)
                .collect(ArrayList<GeocodingResult>::new, ArrayList::add)
                .doOnSubscribe(disposable -> {/*Log.d(TAG, "Starting batch reverse geocoding with " + points.size() + " points")*/})
                .doOnSuccess(results -> {/*Log.d(TAG, "Completed batch reverse geocoding with " + results.size() + " results")*/})
                .doOnError(error -> Log.e(TAG, "Error in final merge of batch reverse geocoding: " + error.getMessage()));
                 // Removed previous onErrorResumeNext that would hide all errors completely.
    }

    private void updateEventProgress(Collection<?> listToProcess) {
        if (progress == null) return; // Guard against null progress, e.g. if startEventProgress wasn't called
        EventProgress currentEvent = EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.incrementAndGet(), listToProcess.size()
        );
        if (events != null) { // Guard against null events
           events.onNextChanged(lastEventAtomic.getAndSet(currentEvent), currentEvent);
        }
    }

    private void startEventProgress(Collection<?> listToProcess) {
        progress = new AtomicInteger(0);
        EventProgress initialEvent = EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.get(), listToProcess.size()
        );
        lastEventAtomic.set(initialEvent);
        if (events != null) { // Guard against null events
            events.onNext(initialEvent);
        }
    }
} 