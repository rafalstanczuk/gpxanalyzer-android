package com.itservices.gpxanalyzer.data.provider.network.geocoding;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Qualifier for multiple Maps.co API keys.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface GeocodingApiKeys {
} 