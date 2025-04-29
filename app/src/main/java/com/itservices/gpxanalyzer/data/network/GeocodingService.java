package com.itservices.gpxanalyzer.data.network;

import com.itservices.gpxanalyzer.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.data.model.geocoding.ReverseGeocodingResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit service interface for Maps.co Geocoding API.
 * 
 * API Limits:
 * - Free tier: 5,000 requests per day
 * - Rate limit: 1 request per second
 * 
 * See https://geocode.maps.co/ for more information.
 */
public interface GeocodingService {

    String BASE_URL = "https://geocode.maps.co/";

    /**
     * Forward geocoding - converts an address into coordinates.
     *
     * @param query   Address or place name to geocode
     * @param apiKey  API key for Maps.co
     * @return A Single emitting the geocoding response
     */
    @GET("search")
    Single<ForwardGeocodingResponse> forwardGeocode(
            @Query("q") String query,
            @Query("api_key") String apiKey
    );

    /**
     * Forward geocoding with structured address components.
     *
     * @param street     Street address with house number
     * @param city       City name
     * @param state      State/province
     * @param country    Country
     * @param postalCode Postal code
     * @param apiKey     API key for Maps.co
     * @return A Single emitting the geocoding response
     */
    @GET("search")
    Single<ForwardGeocodingResponse> forwardGeocodeStructured(
            @Query("street") String street,
            @Query("city") String city,
            @Query("state") String state,
            @Query("country") String country,
            @Query("postalcode") String postalCode,
            @Query("api_key") String apiKey
    );

    /**
     * Reverse geocoding - converts coordinates into an address.
     *
     * @param lat     Latitude
     * @param lon     Longitude
     * @param apiKey  API key for Maps.co
     * @return A Single emitting the reverse geocoding response
     */
    @GET("reverse")
    Single<ReverseGeocodingResponse> reverseGeocode(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("api_key") String apiKey
    );

    /**
     * Reverse geocoding with custom response format.
     *
     * @param lat     Latitude
     * @param lon     Longitude
     * @param format  Response format (json, xml, jsonv2, geojson, geocodejson)
     * @param apiKey  API key for Maps.co
     * @return A Single emitting the reverse geocoding response
     */
    @GET("reverse")
    Single<ReverseGeocodingResponse> reverseGeocodeWithFormat(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("format") String format,
            @Query("api_key") String apiKey
    );
} 