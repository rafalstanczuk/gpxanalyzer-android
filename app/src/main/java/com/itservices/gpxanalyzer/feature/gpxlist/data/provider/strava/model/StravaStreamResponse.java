package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Data model representing the Strava stream response from the API.
 * In the updated API format, streams are returned as a JSON object with stream types as keys,
 * rather than an array of stream objects.
 *
 * This class handles the updated response format introduced in 2025.
 */
public class StravaStreamResponse {

    @SerializedName("latlng")
    private StravaStream latLngStream;

    @SerializedName("altitude")
    private StravaStream altitudeStream;

    @SerializedName("time")
    private StravaStream timeStream;

    @SerializedName("distance")
    private StravaStream distanceStream;

    @SerializedName("heartrate")
    private StravaStream heartrateStream;

    @SerializedName("cadence")
    private StravaStream cadenceStream;

    @SerializedName("watts")
    private StravaStream wattsStream;

    @SerializedName("temp")
    private StravaStream tempStream;

    @SerializedName("moving")
    private StravaStream movingStream;

    @SerializedName("grade_smooth")
    private StravaStream gradeSmoothStream;

    @SerializedName("velocity_smooth")
    private StravaStream velocitySmoothStream;

    // Default constructor
    public StravaStreamResponse() {
    }

    /**
     * Converts this response object to a list of StravaStream objects
     * for compatibility with existing code.
     *
     * @return List of StravaStream objects
     */
    public List<StravaStream> toStreamList() {
        List<StravaStream> streams = new java.util.ArrayList<>();
        
        if (latLngStream != null) {
            latLngStream.setType(StravaStream.StreamType.LATLNG);
            streams.add(latLngStream);
        }
        
        if (altitudeStream != null) {
            altitudeStream.setType(StravaStream.StreamType.ELEVATION);
            streams.add(altitudeStream);
        }
        
        if (timeStream != null) {
            timeStream.setType(StravaStream.StreamType.TIME);
            streams.add(timeStream);
        }
        
        if (distanceStream != null) {
            distanceStream.setType(StravaStream.StreamType.DISTANCE);
            streams.add(distanceStream);
        }
        
        if (heartrateStream != null) {
            heartrateStream.setType(StravaStream.StreamType.HEARTRATE);
            streams.add(heartrateStream);
        }
        
        if (cadenceStream != null) {
            cadenceStream.setType(StravaStream.StreamType.CADENCE);
            streams.add(cadenceStream);
        }
        
        if (wattsStream != null) {
            wattsStream.setType(StravaStream.StreamType.WATTS);
            streams.add(wattsStream);
        }
        
        if (tempStream != null) {
            tempStream.setType(StravaStream.StreamType.TEMP);
            streams.add(tempStream);
        }
        
        if (movingStream != null) {
            movingStream.setType(StravaStream.StreamType.MOVING);
            streams.add(movingStream);
        }
        
        if (gradeSmoothStream != null) {
            gradeSmoothStream.setType(StravaStream.StreamType.GRADE_SMOOTH);
            streams.add(gradeSmoothStream);
        }
        
        if (velocitySmoothStream != null) {
            velocitySmoothStream.setType(StravaStream.StreamType.VELOCITY_SMOOTH);
            streams.add(velocitySmoothStream);
        }
        
        return streams;
    }

    // Getters
    public StravaStream getLatLngStream() {
        return latLngStream;
    }

    public StravaStream getAltitudeStream() {
        return altitudeStream;
    }

    public StravaStream getTimeStream() {
        return timeStream;
    }

    public StravaStream getDistanceStream() {
        return distanceStream;
    }

    public StravaStream getHeartrateStream() {
        return heartrateStream;
    }

    public StravaStream getCadenceStream() {
        return cadenceStream;
    }

    public StravaStream getWattsStream() {
        return wattsStream;
    }

    public StravaStream getTempStream() {
        return tempStream;
    }

    public StravaStream getMovingStream() {
        return movingStream;
    }

    public StravaStream getGradeSmoothStream() {
        return gradeSmoothStream;
    }

    public StravaStream getVelocitySmoothStream() {
        return velocitySmoothStream;
    }
} 