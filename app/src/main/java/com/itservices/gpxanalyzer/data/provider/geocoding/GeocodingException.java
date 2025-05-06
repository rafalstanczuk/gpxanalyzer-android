package com.itservices.gpxanalyzer.data.provider.geocoding;

import java.io.IOException;

/**
 * Custom exception for geocoding errors.
 */
public class GeocodingException extends IOException {
    public GeocodingException(String message) {
        super(message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
