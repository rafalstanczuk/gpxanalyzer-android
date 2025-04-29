package com.itservices.gpxanalyzer.data.provider.network.geocoding;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public enum GeocodingApiError {
    ERROR_CODE_OK(200),
    ERROR_CODE_TOO_MANY_REQUESTS(429),
    ERROR_CODE_SERVICE_UNAVAILABLE(503),
    ERROR_CODE_API_FORBIDDEN(403),
    UNKNOWN(-1);
    private final int code;

    GeocodingApiError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static GeocodingApiError fromCode(int code) {
        AtomicReference<GeocodingApiError> result = new AtomicReference<>(UNKNOWN);

        Arrays.stream(values())
                .filter(geocodingApiError -> geocodingApiError.getCode() == code)
                .findFirst()
                .ifPresent(result::set);

        return result.get();
    }
}
