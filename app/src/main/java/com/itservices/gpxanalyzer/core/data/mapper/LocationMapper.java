package com.itservices.gpxanalyzer.core.data.mapper;

import android.location.Location;

import com.itservices.gpxanalyzer.core.data.parser.domain.TrackPoint;

import androidx.annotation.NonNull;

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
