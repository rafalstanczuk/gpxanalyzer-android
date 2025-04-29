package com.itservices.gpxanalyzer.data.provider.network.geocoding;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
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
 * Repository that distributes geocoding requests across multiple API keys
 * to increase throughput while respecting rate limits.
 */
public class GeocodingNetworkRouterRepository {
    private static final String TAG = "GeocodingRouterRepository";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final GlobalEventWrapper events;
    private final List<GeocodingNetworkRepository> repositories;
    private final Scheduler scheduler;
    private final AtomicInteger currentRepoIndex = new AtomicInteger(0);
    private AtomicInteger progress;
    private AtomicReference<EventProgress> lastEventAtomic = new AtomicReference<>();

    public GeocodingNetworkRouterRepository(GlobalEventWrapper events, List<GeocodingNetworkRepository> repositories, Scheduler scheduler) {
        this.events = events;
        this.repositories = repositories;
        this.scheduler = scheduler;
        //Log.d(TAG, "Created thread pool with " + scheduler + " threads");
    }

    private GeocodingNetworkRepository getNextRepository() {
        return repositories.get(currentRepoIndex.getAndIncrement() % repositories.size());
    }

    public Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses) {
        if (addresses.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        currentRepoIndex.set(0);

        startEventProgress(addresses);

        List<Observable<ForwardGeocodingResponse>> tasks = new ArrayList<>();

        for (String address : addresses) {
            GeocodingNetworkRepository repository = getNextRepository();
            tasks.add(Observable.just(address)
                    .subscribeOn(scheduler)
                    .map(addr -> {
                        try {
                            //Log.d(TAG, "Processing address: " + addr + " with API key: " + repository.getApiKey());
                            return repository.geocodeAddress(addr).blockingGet();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing address: " + addr, e);
                            throw e;
                        }
                    })
                    .doOnNext(geocodingResults -> updateEventProgress(addresses))
                    .retryWhen(errors -> errors
                            .zipWith(Observable.range(1, MAX_RETRIES), (throwable, retryCount) -> {
                                if (throwable instanceof GeocodingRequestQueue.GeocodingException) {
                                    Log.w(TAG, String.format(
                                            "Retry %d/%d for address %s after error: %s",
                                            retryCount, MAX_RETRIES, address, throwable.getMessage()
                                    ));
                                    return retryCount;
                                }
                                throw new RuntimeException(throwable);
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
                    //Log.d(TAG, "Completed batch geocoding with " + results.size() + " results");
                    if (results.size() != addresses.size()) {
                        Log.w(TAG, "Warning: Number of results (" + results.size() +
                                ") does not match number of input addresses (" + addresses.size() + ")");
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "Error in batch geocoding: " + error.getMessage());
                    if (error instanceof GeocodingRequestQueue.GeocodingException) {
                        Log.e(TAG, "Geocoding error details: " + error.getMessage());
                    }
                })
                .onErrorResumeNext(error -> {
                    Log.e(TAG, "Resuming with empty results after error: " + error.getMessage());
                    return Single.just(new ArrayList<>());
                });
    }

    public Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points) {
        Log.d(TAG, "batchReverseGeocode() called with: points = [" + points + "]");

        if (points.isEmpty()) {
            return Single.just(new ArrayList<>());
        }

        currentRepoIndex.set(0);

        startEventProgress(points);

        List<Observable<GeocodingResult>> tasks = new ArrayList<>();

        for (Location point : points) {
            GeocodingNetworkRepository repository = getNextRepository();
            tasks.add(Observable.just(point)
                    .subscribeOn(scheduler)
                    .map(p -> {
                        try {
                            //Log.d(TAG, "Processing point: " + p + " with API key: " + repository.getApiKey());
                            return repository.reverseGeocode(p).blockingGet();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing point: " + p, e);
                            throw e;
                        }
                    })
                    .doOnNext(geocodingResults -> updateEventProgress(points))
                    .retryWhen(errors -> errors
                            .zipWith(Observable.range(1, MAX_RETRIES), (throwable, retryCount) -> {
                                if (throwable instanceof GeocodingRequestQueue.GeocodingException) {
                                    Log.w(TAG, String.format(
                                            "Retry %d/%d for point %s after error: %s",
                                            retryCount, MAX_RETRIES, point, throwable.getMessage()
                                    ));
                                    return retryCount;
                                }
                                throw new RuntimeException(throwable);
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
                .doOnError(error -> Log.e(TAG, "Error in batch reverse geocoding: " + error.getMessage()))
                .onErrorResumeNext(error -> {
                    Log.e(TAG, "Resuming with empty results after error: " + error.getMessage());
                    return Single.just(new ArrayList<>());
                });
    }

    private void updateEventProgress(Collection<?> listToProcess) {
        EventProgress currentEvent = EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.incrementAndGet(), listToProcess.size()
        );
        events.onNextChanged(lastEventAtomic.get(), currentEvent);

        lastEventAtomic.set(currentEvent);
    }

    private void startEventProgress(Collection<?> listToProcess) {
        progress = new AtomicInteger(0);
        lastEventAtomic.set(EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.get(), listToProcess.size()
        ));
        events.onNext(lastEventAtomic.get());
    }
} 