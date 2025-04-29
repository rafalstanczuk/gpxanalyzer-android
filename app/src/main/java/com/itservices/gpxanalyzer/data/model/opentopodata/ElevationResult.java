package com.itservices.gpxanalyzer.data.model.opentopodata;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the elevation result for a single location from the Open Topo Data API.
 */
public class ElevationResult {
    @SerializedName("elevation")
    public Double elevation; // Altitude in meters

    @SerializedName("location")
    public Location location;

    @SerializedName("dataset")
    public String dataset;

    // Getters or public fields are fine for DTOs
} 