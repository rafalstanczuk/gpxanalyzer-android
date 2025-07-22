package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.ArrayList;

/**
 * Data model representing a Strava stream response from the API.
 * Streams contain time-series data for activities including GPS coordinates, elevation, and time.
 * 
 * Updated for the 2025 API format where each stream is a separate object with its own type and data.
 */
public class StravaStream {

    @SerializedName("type")
    private String type;

    @SerializedName("data")
    private List<Object> data;

    @SerializedName("series_type")
    private String seriesType;

    @SerializedName("original_size")
    private Integer originalSize;

    @SerializedName("resolution")
    private String resolution;

    // Default constructor
    public StravaStream() {
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public Integer getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(Integer originalSize) {
        this.originalSize = originalSize;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * Stream types supported by Strava API
     */
    public static class StreamType {
        public static final String LATLNG = "latlng";
        public static final String ELEVATION = "altitude";
        public static final String TIME = "time";
        public static final String DISTANCE = "distance";
        public static final String HEARTRATE = "heartrate";
        public static final String CADENCE = "cadence";
        public static final String WATTS = "watts";
        public static final String TEMP = "temp";
        public static final String MOVING = "moving";
        public static final String GRADE_SMOOTH = "grade_smooth";
        public static final String VELOCITY_SMOOTH = "velocity_smooth";
    }

    /**
     * Checks if this stream contains GPS coordinate data.
     * 
     * @return true if the stream type is latlng
     */
    public boolean isLatLngStream() {
        return StreamType.LATLNG.equals(type);
    }

    /**
     * Checks if this stream contains elevation data.
     * 
     * @return true if the stream type is altitude/elevation
     */
    public boolean isElevationStream() {
        return StreamType.ELEVATION.equals(type);
    }

    /**
     * Checks if this stream contains time data.
     * 
     * @return true if the stream type is time
     */
    public boolean isTimeStream() {
        return StreamType.TIME.equals(type);
    }

    /**
     * Gets the GPS coordinates from a latlng stream.
     * 
     * @return list of [lat, lng] coordinate pairs or null if not a latlng stream
     */
    public List<List<Double>> getLatLngData() {
        if (!isLatLngStream() || data == null) {
            return null;
        }
        
        try {
            List<List<Double>> result = new ArrayList<>();
            for (Object item : data) {
                if (item instanceof List) {
                    List<?> coords = (List<?>) item;
                    if (coords.size() >= 2) {
                        List<Double> latLng = new ArrayList<>();
                        // Latitude
                        Object lat = coords.get(0);
                        if (lat instanceof Number) {
                            latLng.add(((Number) lat).doubleValue());
                        } else {
                            return null; // Invalid data
                        }
                        // Longitude
                        Object lng = coords.get(1);
                        if (lng instanceof Number) {
                            latLng.add(((Number) lng).doubleValue());
                        } else {
                            return null; // Invalid data
                        }
                        result.add(latLng);
                    }
                } else if (item instanceof Object[]) {
                    // Handle array format that might be used in newer API versions
                    Object[] coords = (Object[]) item;
                    if (coords.length >= 2) {
                        List<Double> latLng = new ArrayList<>();
                        // Latitude
                        if (coords[0] instanceof Number) {
                            latLng.add(((Number) coords[0]).doubleValue());
                        } else {
                            return null; // Invalid data
                        }
                        // Longitude
                        if (coords[1] instanceof Number) {
                            latLng.add(((Number) coords[1]).doubleValue());
                        } else {
                            return null; // Invalid data
                        }
                        result.add(latLng);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the elevation data from an elevation stream.
     * 
     * @return list of elevation values or null if not an elevation stream
     */
    public List<Double> getElevationData() {
        if (!isElevationStream() || data == null) {
            return null;
        }
        
        try {
            List<Double> result = new ArrayList<>();
            for (Object item : data) {
                if (item instanceof Number) {
                    result.add(((Number) item).doubleValue());
                } else {
                    return null; // Invalid data
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the time data from a time stream.
     * 
     * @return list of time values (seconds from start) or null if not a time stream
     */
    public List<Integer> getTimeData() {
        if (!isTimeStream() || data == null) {
            return null;
        }
        
        try {
            List<Integer> result = new ArrayList<>();
            for (Object item : data) {
                if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                } else {
                    return null; // Invalid data
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the number of data points in this stream.
     * 
     * @return number of data points
     */
    public int getDataPointCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public String toString() {
        return "StravaStream{" +
                "type='" + type + '\'' +
                ", dataPoints=" + getDataPointCount() +
                ", seriesType='" + seriesType + '\'' +
                ", originalSize=" + originalSize +
                ", resolution='" + resolution + '\'' +
                '}';
    }
} 