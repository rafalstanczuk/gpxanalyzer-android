package com.itservices.gpxanalyzer.data.provider.geocoding.network;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Qualifier annotation for injecting the Maps.co Geocoding API key.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface GeocodingApiKey {
} 