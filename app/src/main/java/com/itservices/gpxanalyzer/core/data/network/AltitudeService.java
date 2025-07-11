package com.itservices.gpxanalyzer.core.data.network;

import com.itservices.gpxanalyzer.core.data.model.opentopodata.OpenTopoDataResponse;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit service interface for interacting with the Open Topo Data API.
 */
public interface AltitudeService {

    String BASE_URL = "https://api.opentopodata.org/v1/";
    String DATASET_SRTM90M = "srtm90m";
    String DATASET_MAPZEN = "mapzen";

    /**
     * Fetches elevation data for a list of locations using GET.
     * Uses the SRTM90m dataset by default.
     *
     * @param locations Pipe (|) separated string of "latitude,longitude" pairs.
     * @return A Single emitting the API response.
     */
    @GET(DATASET_SRTM90M)
    Single<OpenTopoDataResponse> getElevation(@Query("locations") String locations);

    /**
     * Fetches elevation data for a list of locations using POST.
     * This method is recommended when the number of locations is large,
     * as it avoids URL length limitations.
     * 
     * @param params Map containing parameters: "locations" (required), "interpolation" (optional),
     *               "nodata_value" (optional), and "format" (optional).
     * @return A Single emitting the API response.
     */
    @FormUrlEncoded
    @POST(DATASET_SRTM90M)
    Single<OpenTopoDataResponse> postElevation(@FieldMap Map<String, String> params);

    // Add other endpoints or datasets (e.g., "mapzen") here if needed
} 