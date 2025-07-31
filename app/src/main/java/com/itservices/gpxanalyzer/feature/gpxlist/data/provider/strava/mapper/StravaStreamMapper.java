package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.mapper;

import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStream;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStreamResponse;

import java.util.List;
import java.util.ArrayList;

/**
 * Mapper class for converting between different Strava API response formats.
 * 
 * This class handles the transition from the older array-based stream format
 * to the newer object-based format introduced in 2025.
 */
public class StravaStreamMapper {

    /**
     * Converts a StravaStreamResponse (new format) to a list of StravaStream objects (old format).
     * This provides backward compatibility for existing code that expects the old format.
     *
     * @param response The new format stream response
     * @return List of stream objects in the old format
     */
    public static List<StravaStream> responseToStreamList(StravaStreamResponse response) {
        if (response == null) {
            return new ArrayList<>();
        }
        
        return response.toStreamList();
    }
    
    /**
     * Validates that the stream data contains sufficient information for GPX generation.
     * 
     * @param streams List of streams to validate
     * @return true if streams contain valid GPS data
     */
    public static boolean validateStreamsForGpx(List<StravaStream> streams) {
        if (streams == null || streams.isEmpty()) {
            return false;
        }
        
        // Check for required latlng stream
        for (StravaStream stream : streams) {
            if (stream.isLatLngStream() && stream.getLatLngData() != null && !stream.getLatLngData().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validates that the stream response contains sufficient information for GPX generation.
     * 
     * @param response Stream response to validate
     * @return true if response contains valid GPS data
     */
    public static boolean validateStreamResponseForGpx(StravaStreamResponse response) {
        if (response == null) {
            return false;
        }
        
        // Convert to list and validate
        List<StravaStream> streams = responseToStreamList(response);
        return validateStreamsForGpx(streams);
    }
} 