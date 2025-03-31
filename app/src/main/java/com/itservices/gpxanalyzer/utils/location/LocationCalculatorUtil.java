package com.itservices.gpxanalyzer.utils.location;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for performing calculations on Location objects.
 * This class provides methods for calculating centroids, speeds, and other
 * geographical measurements using the ECEF (Earth-Centered, Earth-Fixed)
 * coordinate system for accurate calculations.
 *
 * The class is designed to be used with the GPX analyzer application for
 * processing and analyzing geographical data from GPS tracks.
 */
public class LocationCalculatorUtil {

    /**
     * Calculates the centroid of a list of Location objects using manual ECEF conversion.
     * This method provides a more accurate calculation than simple averaging of coordinates
     * by converting to ECEF coordinates, calculating the centroid, and converting back to
     * geodetic coordinates.
     *
     * @param locations The list of Location objects to calculate the centroid for
     * @return A Location object representing the centroid
     * @throws IllegalArgumentException if the locations list is null or empty
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

    /**
     * Computes the mean time between two GPS points.
     *
     * @param gpxPointA The first GPS point
     * @param gpxPointB The second GPS point
     * @return The mean time in milliseconds
     */
    public static long computeMeanTime(Location gpxPointA, Location gpxPointB) {
        return (long) (0.5 * (double)(gpxPointA.getTime() + gpxPointB.getTime()));
    }

    /**
     * Calculates the speed between two geographical points in meters per second (m/s).
     * This method uses the Haversine formula to calculate the great-circle distance
     * between the points and divides it by the time difference.
     *
     * @param gpxPointA The first GPS point
     * @param gpxPointB The second GPS point
     * @return The speed in meters per second
     * @throws IllegalArgumentException if the time difference is non-positive
     */
    public static double calculateSpeed(Location gpxPointA, Location gpxPointB) {
        long timeDiff = gpxPointB.getTime() - gpxPointA.getTime();
        if (timeDiff <= 0) {
            throw new IllegalArgumentException("Time difference must be positive");
        }

        double distance = calculateDistance(gpxPointA, gpxPointB);
        return distance / (timeDiff / 1000.0); // Convert milliseconds to seconds
    }

    /**
     * Calculates the great-circle distance between two geographical points using
     * the Haversine formula.
     *
     * @param gpxPointA The first GPS point
     * @param gpxPointB The second GPS point
     * @return The distance in meters
     */
    public static double calculateDistance(Location gpxPointA, Location gpxPointB) {
        double lat1 = gpxPointA.getLatitude();
        double lon1 = gpxPointA.getLongitude();
        double lat2 = gpxPointB.getLatitude();
        double lon2 = gpxPointB.getLongitude();

        double R = 6371e3; // Earth's radius in meters
        double φ1 = Math.toRadians(lat1);
        double φ2 = Math.toRadians(lat2);
        double Δφ = Math.toRadians(lat2 - lat1);
        double Δλ = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
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

    @NonNull
    public static Location calculateCentroidLocation(Location gpxPointA, Location gpxPointB) {
        Location centroidLocation = calculateCentroid(Arrays.asList(gpxPointA, gpxPointB));

        float speed = calculateSpeed3D(gpxPointA, gpxPointB);
        centroidLocation.setSpeed(speed);
        centroidLocation.setTime(computeMeanTime(gpxPointA, gpxPointB));
        return centroidLocation;
    }
}
