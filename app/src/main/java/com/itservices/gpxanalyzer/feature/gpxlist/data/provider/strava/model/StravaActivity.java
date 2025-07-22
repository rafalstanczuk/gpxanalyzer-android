package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Data model representing a Strava activity response from the API.
 * Contains essential information about an activity that can be converted to GPX format.
 */
public class StravaActivity {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("sport_type")
    private String sportType;

    @SerializedName("start_date")
    private Date startDate;

    @SerializedName("start_date_local")
    private Date startDateLocal;

    @SerializedName("distance")
    private Float distance;

    @SerializedName("moving_time")
    private Integer movingTime;

    @SerializedName("elapsed_time")
    private Integer elapsedTime;

    @SerializedName("total_elevation_gain")
    private Float totalElevationGain;

    @SerializedName("start_latlng")
    private Float[] startLatLng;

    @SerializedName("end_latlng")
    private Float[] endLatLng;

    @SerializedName("map")
    private StravaMap map;

    @SerializedName("has_heartrate")
    private Boolean hasHeartrate;

    @SerializedName("gear_id")
    private String gearId;

    @SerializedName("external_id")
    private String externalId;

    // Default constructor
    public StravaActivity() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDateLocal() {
        return startDateLocal;
    }

    public void setStartDateLocal(Date startDateLocal) {
        this.startDateLocal = startDateLocal;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Integer getMovingTime() {
        return movingTime;
    }

    public void setMovingTime(Integer movingTime) {
        this.movingTime = movingTime;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Float getTotalElevationGain() {
        return totalElevationGain;
    }

    public void setTotalElevationGain(Float totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }

    public Float[] getStartLatLng() {
        return startLatLng;
    }

    public void setStartLatLng(Float[] startLatLng) {
        this.startLatLng = startLatLng;
    }

    public Float[] getEndLatLng() {
        return endLatLng;
    }

    public void setEndLatLng(Float[] endLatLng) {
        this.endLatLng = endLatLng;
    }

    public StravaMap getMap() {
        return map;
    }

    public void setMap(StravaMap map) {
        this.map = map;
    }

    public Boolean getHasHeartrate() {
        return hasHeartrate;
    }

    public void setHasHeartrate(Boolean hasHeartrate) {
        this.hasHeartrate = hasHeartrate;
    }

    public String getGearId() {
        return gearId;
    }

    public void setGearId(String gearId) {
        this.gearId = gearId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Checks if this activity has GPS data available.
     * 
     * @return true if the activity has GPS coordinates
     */
    public boolean hasGpsData() {
        return startLatLng != null && startLatLng.length >= 2 
               && startLatLng[0] != null && startLatLng[1] != null;
    }

    /**
     * Gets a safe filename for this activity.
     * 
     * @return a filename-safe string based on activity name and date
     */
    public String getSafeFilename() {
        String safeName = (name != null ? name : "activity_" + id)
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        return safeName + "_" + id + ".gpx";
    }

    @Override
    public String toString() {
        return "StravaActivity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", startDate=" + startDate +
                ", distance=" + distance +
                ", hasGpsData=" + hasGpsData() +
                '}';
    }

    /**
     * Nested class representing the map data from Strava activity.
     */
    public static class StravaMap {
        @SerializedName("id")
        private String id;

        @SerializedName("polyline")
        private String polyline;

        @SerializedName("summary_polyline")
        private String summaryPolyline;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(String polyline) {
            this.polyline = polyline;
        }

        public String getSummaryPolyline() {
            return summaryPolyline;
        }

        public void setSummaryPolyline(String summaryPolyline) {
            this.summaryPolyline = summaryPolyline;
        }
    }
} 