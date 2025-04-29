package com.itservices.gpxanalyzer.data.provider.network.geocoding;

import android.util.Log;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.HttpException;

/**
 * Manages a queue of geocoding requests with rate limiting and retry logic.
 * Handles HTTP 429 (Too Many Requests), 503 (Service Unavailable), and 403 (Forbidden) responses.
 * Each instance of this queue has its own API key and can process requests independently.
 */
public class GeocodingRequestQueue {
    private static final String TAG = "GeocodingRequestQueue";
    private static final long MIN_REQUEST_INTERVAL_MS = 1500;
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final long MAX_RETRY_DELAY_MS = 30000;
    private static final double BACKOFF_MULTIPLIER = 2.0;


    private final PublishSubject<RequestWrapper<?>> requestQueue = PublishSubject.create();
    private final AtomicLong lastRequestTime = new AtomicLong(0);
    private final ReentrantLock requestLock = new ReentrantLock();
    private final Scheduler scheduler = Schedulers.newThread();
    private Disposable queueSubscription;

    @Inject
    public GeocodingRequestQueue() {
        setupQueueProcessing();
    }

    private void setupQueueProcessing() {
        queueSubscription = requestQueue
                .concatMap(this::processRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(
                        wrapper -> {
                        }, // onNext
                        error -> {
                            if (error instanceof GeocodingException) {
                                Log.e(TAG, "Geocoding error: " + error.getMessage(), error);
                            } else {
                                Log.e(TAG, "Unexpected error in request queue: " + error.getMessage(), error);
                                throw new RuntimeException(error);
                            }
                        }
                );
    }

    /**
     * Enqueues a geocoding request with rate limiting and retry logic.
     *
     * @param request The request to execute
     * @param <T>     The type of the response
     * @return A Single that will emit the response when the request is processed
     */
    public <T> Single<T> enqueue(Single<T> request) {
        return Single.create(emitter -> {
            RequestWrapper<T> wrapper = new RequestWrapper<>(request, emitter);
            requestQueue.onNext(wrapper);
        });
    }

    private <T> Observable<RequestWrapper<T>> processRequest(RequestWrapper<T> wrapper) {
        return Observable.just(wrapper)
                .flatMap(w -> {
                    requestLock.lock();
                    try {
                        long currentTime = System.currentTimeMillis();
                        long delay = 0;

                        if (currentTime - lastRequestTime.get() <= MIN_REQUEST_INTERVAL_MS) {
                            delay = MIN_REQUEST_INTERVAL_MS;
                        }

                        Log.i(TAG, "GeocodingRequestQueue: Rate limiting: Delaying request by " + delay + "ms");


                        return Observable.timer(delay, TimeUnit.MILLISECONDS, scheduler)
                                .doOnNext(t -> {
                                    Log.i(TAG, "GeocodingRequestQueue: Request is  being processed:");
                                    lastRequestTime.set(System.currentTimeMillis());
                                })
                                .flatMap(t -> executeRequest(w));
                    } finally {
                        requestLock.unlock();
                    }
                });
    }

    private <T> Observable<RequestWrapper<T>> executeRequest(RequestWrapper<T> wrapper) {
        return wrapper.request
                .toFlowable()
                .retryWhen(errors -> errors
                        .zipWith(Flowable.range(1, MAX_RETRIES), (throwable, retryCount) -> {
                            if (throwable instanceof HttpException httpException) {
                                int code = httpException.code();

                                switch (GeocodingApiError.fromCode(httpException.code())) {
                                    case ERROR_CODE_TOO_MANY_REQUESTS -> {
                                        long delay = calculateBackoffDelay(retryCount);
                                        Log.w(TAG, String.format(
                                                "Rate limited (429). Retry %d/%d after %dms",
                                                retryCount, MAX_RETRIES, delay
                                        ));
                                        return delay;
                                    }
                                    case ERROR_CODE_SERVICE_UNAVAILABLE -> {
                                        long delay = calculateBackoffDelay(retryCount);
                                        Log.w(TAG, String.format(
                                                "Service unavailable (503). Retry %d/%d after %dms",
                                                retryCount, MAX_RETRIES, delay
                                        ));
                                        return delay;
                                    }
                                    case ERROR_CODE_API_FORBIDDEN -> {
                                        throw new GeocodingException("API access forbidden. Please contact support.");
                                    }
                                    case UNKNOWN -> {
                                        throw new GeocodingException("Geocoding service error: " + code);
                                    }
                                }
                            }
                            throw new GeocodingException("Failed to geocode location", throwable);
                        })
                        .flatMap(delay -> Flowable.timer(delay, TimeUnit.MILLISECONDS)))
                .toObservable()
                .firstOrError()
                .doOnSuccess(wrapper.emitter::onSuccess)
                .doOnError(error -> {
                    if (error instanceof GeocodingException) {
                        wrapper.emitter.onError(error);
                    } else {
                        wrapper.emitter.onError(new GeocodingException("Failed to geocode location", error));
                    }
                })
                .map(response -> wrapper)
                .toObservable();
    }

    private long calculateBackoffDelay(int retryCount) {
        long delay = (long) (INITIAL_RETRY_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, retryCount - 1));
        return Math.min(delay, MAX_RETRY_DELAY_MS);
    }

    /**
     * Wrapper class for requests and their emitters.
     */
    private static class RequestWrapper<T> {
        final Single<T> request;
        final SingleEmitter<T> emitter;

        RequestWrapper(Single<T> request, SingleEmitter<T> emitter) {
            this.request = request;
            this.emitter = emitter;
        }
    }

    public static class GeocodingException extends RuntimeException {
        public GeocodingException(String message) {
            super(message);
        }

        public GeocodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 