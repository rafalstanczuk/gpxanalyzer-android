package com.itservices.gpxanalyzer.core.data.provider.geocoding;

import android.location.Location;
import com.itservices.gpxanalyzer.core.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.core.data.model.geocoding.GeocodingResult;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Single;

/**
 * Base interface for geocoding operations.
 */
public interface BaseGeocodingRepository {

    /**
     * Reverse geocodes a location to get its address.
     * @param location The location to reverse geocode.
     * @return A Single emitting the geocoding result.
     */
    Single<GeocodingResult> reverseGeocode(Location location);

    /**
     * Batch reverse geocodes multiple locations.
     * @param points List of locations to reverse geocode.
     * @return A Single emitting a list of geocoding results.
     */
    Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points);

    /**
     * Forward geocodes an address string to get its coordinates.
     * @param address The address string to geocode.
     * @return A Single emitting the forward geocoding response.
     */
    Single<ForwardGeocodingResponse> geocodeAddress(String address);

    /**
     * Batch forward geocodes multiple address strings.
     * @param addresses List of address strings to geocode.
     * @return A Single emitting a list of forward geocoding responses.
     */
    Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses);

    /**
     * Forward geocodes an address with structured components.
     * @param street Street address (with house number)
     * @param city City name
     * @param state State/province
     * @param country Country
     * @param postalCode Postal code
     * @return A Single emitting the forward geocoding response.
     */
    Single<ForwardGeocodingResponse> geocodeStructuredAddress(
            String street,
            String city,
            String state,
            String country,
            String postalCode
    );
} 