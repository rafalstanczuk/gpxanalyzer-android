package com.itservices.gpxanalyzer.data.model.opentopodata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Root object for the JSON response from the Open Topo Data API.
 */
public class OpenTopoDataResponse {
    @SerializedName("results")
    public List<ElevationResult> results;

    @SerializedName("status")
    public String status; // e.g., "OK"

    // Getters or public fields are fine for DTOs
} 