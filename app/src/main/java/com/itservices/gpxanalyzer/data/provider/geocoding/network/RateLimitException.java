package com.itservices.gpxanalyzer.data.provider.geocoding.network;

import java.io.IOException;

/**
 * Custom exception for rate limit errors.
 */
public class RateLimitException extends IOException {
    public RateLimitException(String message) {
        super(message);
    }
}
