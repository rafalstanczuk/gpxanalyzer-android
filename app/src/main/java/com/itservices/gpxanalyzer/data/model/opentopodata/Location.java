package com.itservices.gpxanalyzer.data.model.opentopodata;

import com.google.gson.annotations.SerializedName;

/**
 * Represents latitude/longitude within the Open Topo Data API result.
 */
public class Location {
    @SerializedName("lat")
    public Double latitude;

    @SerializedName("lng")
    public Double longitude;

    // Getters or public fields are fine for DTOs
} 