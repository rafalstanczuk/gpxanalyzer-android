package com.itservices.gpxanalyzer.data.provider;

import android.location.Location;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.parser.gpxparser.domain.TrackPoint;

public class LocationMapper {
    @NonNull
    public static Location mapFrom(TrackPoint trackPoint) {
        Location location = new Location(TrackPoint.class.getSimpleName());

        location.setLatitude(trackPoint.getLatitude());
        location.setLongitude(trackPoint.getLongitude());
        location.setAltitude(trackPoint.getElevation());
        location.setTime(trackPoint.getTime().toDate().getTime());

        return location;
    }
}
