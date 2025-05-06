package com.itservices.gpxanalyzer.data.provider.geocoding.network;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingRateLimitInterceptor implements Interceptor {
    private static final String TAG = "GeocodingRateLimit";
    private static final int MAX_RETRIES = 5;
    private static final long MIN_REQUEST_INTERVAL_MS = 2000; // 2 second between requests
    private final AtomicLong lastRequestTime = new AtomicLong(0);
    private final ReentrantLock lock = new ReentrantLock();

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Response response = chain.proceed(request);
        int retryCount = 0;

        while (needToRetry(response, retryCount)) {
            response = handleErrorAndRetry(chain, response, request);
            retryCount++;
        }

        return response;
    }

    private static boolean needToRetry(Response response, int retryCount) {
        return (response.code() != GeocodingApiError.ERROR_CODE_OK.getCode()) && retryCount < MAX_RETRIES;
    }

    @NonNull
    private Response handleErrorAndRetry(Chain chain, Response response, Request request) throws IOException {
        GeocodingApiError geocodingApiError = GeocodingApiError.fromCode(response.code());
        switch (geocodingApiError) {
            case ERROR_CODE_TOO_MANY_REQUESTS -> {
                Log.i(TAG, "intercept " + geocodingApiError.name() + ": " + response);

                response = retryingAfterNDelays(chain, 1, response, request);
            }
            case ERROR_CODE_SERVICE_UNAVAILABLE -> {
                Log.i(TAG, "intercept " + geocodingApiError.name() + ": " + response);

                response = retryingAfterNDelays(chain, 2, response, request);
            }
            default -> {
                Log.i(TAG, "intercept " + geocodingApiError.name() + ": " + response);
            }
        }
        return response;
    }

    @NonNull
    private Response retryingAfterNDelays(Chain chain, int n, Response response, Request request) throws IOException {
        Log.i(TAG, "Retrying after ms: " + n *MIN_REQUEST_INTERVAL_MS);
        response.close();
        waitMinInterval(n);
        response = chain.proceed(request);
        return response;
    }

    private void waitMinInterval(int n) throws IOException {
        lock.lock();
        try {
            long delay = n * MIN_REQUEST_INTERVAL_MS;

            Log.i(TAG, "Interceptor: Rate limiting: Delaying request by " + delay + "ms");

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
            lastRequestTime.set(System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }
} 