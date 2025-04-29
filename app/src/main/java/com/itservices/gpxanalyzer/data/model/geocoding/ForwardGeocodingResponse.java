package com.itservices.gpxanalyzer.data.model.geocoding;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing the response from Maps.co Forward Geocoding API.
 * The API returns an array of location results.
 */
public class ForwardGeocodingResponse extends ArrayList<GeocodingResult> {
    // This class extends ArrayList<GeocodingResult> since the Maps.co API
    // returns the results as a JSON array of geocoding results
} 