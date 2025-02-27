package com.itservices.gpxanalyzer.utils.location;

import android.location.Location;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationCalculatorUtil {

    /**
     * Calculates the centroid of a list of Location objects using manual ECEF conversion.
     *
     * @param locations Location
     * @return Centroid Location object
     */
    public static Location calculateCentroid(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Location list is null or empty");
        }

        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;
        int count = locations.size();

        for (Location loc : locations) {
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            double alt = loc.hasAltitude() ? loc.getAltitude() : 0.0;

            double[] ecef = ECEFConverter.geodeticToECEF(lat, lon, alt);
            sumX += ecef[0];
            sumY += ecef[1];
            sumZ += ecef[2];
        }

        double centroidX = sumX / count;
        double centroidY = sumY / count;
        double centroidZ = sumZ / count;

        double[] centroidGeodetic = ECEFConverter.ecefToGeodetic(centroidX, centroidY, centroidZ);

        double centroidLat = centroidGeodetic[0];
        double centroidLon = centroidGeodetic[1];
        double centroidAlt = centroidGeodetic[2];

        Location centroid = new Location("centroid");
        centroid.setLatitude(centroidLat);
        centroid.setLongitude(centroidLon);
        centroid.setAltitude(centroidAlt);

        return centroid;
    }

    public static long computeMeanTime(Location gpxPointA, Location gpxPointB) {
        return (long) (0.5 * (double)(gpxPointA.getTime() + gpxPointB.getTime()));
    }

    /**
     * Calculates the speed between two geographical points in meters per second (m/s).
     *
     * @throws IllegalArgumentException If time difference is non-positive.
     */
    public static float calculateSpeed3D(
            Location startLocation,
            Location endLocation) {

        // Calculate 2D distance in meters
        float[] results = new float[1];
        Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude(), results);
        float distanceInMeters = results[0];

        // Include altitude difference for 3D distance
        double altitudeDifference = endLocation.getAltitude() - startLocation.getAltitude();
        double distance3D = Math.sqrt(distanceInMeters * distanceInMeters + altitudeDifference * altitudeDifference);

        // Calculate time difference in seconds
        double timeDifferenceSeconds = TimeUnit.MILLISECONDS.toSeconds((endLocation.getTime() - startLocation.getTime()));

        // Validate time difference
        if (timeDifferenceSeconds <= 0) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        // Calculate speed
        return (float) (distance3D / timeDifferenceSeconds);
    }
}
