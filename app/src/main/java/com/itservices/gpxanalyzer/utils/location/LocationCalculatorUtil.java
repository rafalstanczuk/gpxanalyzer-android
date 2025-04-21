package com.itservices.gpxanalyzer.utils.location;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for performing calculations involving {@link Location} objects.
 * Provides methods for calculating centroids, distances (2D and 3D), speeds, and mean time between points.
 * Utilizes the {@link ECEFConverter} for accurate centroid calculations based on the Earth-Centered, Earth-Fixed (ECEF)
 * coordinate system and the Haversine formula for great-circle distances.
 *
 * The class is designed to be used with the GPX analyzer application for
 * processing and analyzing geographical data from GPS tracks.
 */
public class LocationCalculatorUtil {

    /**
     * Calculates the geometric centroid of a list of {@link Location} objects.
     * This method converts each geodetic location (latitude, longitude, altitude) to ECEF coordinates,
     * averages these X, Y, Z coordinates, and then converts the resulting average ECEF coordinate back
     * to geodetic coordinates (latitude, longitude, altitude).
     * If a location lacks altitude, 0.0 is assumed.
     *
     * @param locations The list of {@link Location} objects.
     * @return A new {@link Location} object representing the calculated centroid.
     *         The provider for the returned Location is set to "centroid".
     * @throws IllegalArgumentException if the {@code locations} list is null or empty.
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
     * Computes the mean timestamp between two {@link Location} points.
     *
     * @param gpxPointA The first {@link Location} object.
     * @param gpxPointB The second {@link Location} object.
     * @return The average of the two location timestamps, in milliseconds since the epoch.
     */
    public static long computeMeanTime(Location gpxPointA, Location gpxPointB) {
        return (long) (0.5 * (double)(gpxPointA.getTime() + gpxPointB.getTime()));
    }

    /**
     * Calculates the 2D speed between two {@link Location} points in meters per second (m/s).
     * This method uses the Haversine formula (via {@link #calculateDistance(Location, Location)}) to calculate the
     * great-circle (2D) distance between the points and divides it by the time difference in seconds.
     *
     * @param gpxPointA The starting {@link Location} object.
     * @param gpxPointB The ending {@link Location} object.
     * @return The calculated speed in meters per second (m/s).
     * @throws IllegalArgumentException if the time difference between the points is zero or negative.
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
     * Calculates the great-circle (2D) distance between two geographical points using the Haversine formula.
     * This formula accounts for the curvature of the Earth but ignores altitude differences.
     *
     * @param gpxPointA The first {@link Location} object.
     * @param gpxPointB The second {@link Location} object.
     * @return The distance in meters.
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
     * Calculates the 3D speed between two {@link Location} points in meters per second (m/s).
     * This method considers both the horizontal (2D) distance calculated using {@link Location#distanceBetween(double, double, double, double, float[])}
     * and the vertical distance (difference in altitude).
     * The 3D distance (hypotenuse) is calculated using the Pythagorean theorem.
     * The speed is then computed by dividing the 3D distance by the time difference in seconds.
     *
     * @param startLocation The starting {@link Location} object.
     * @param endLocation   The ending {@link Location} object.
     * @return The calculated 3D speed in meters per second (m/s).
     * @throws IllegalArgumentException if the time difference between the points is zero or negative.
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

    /**
     * Calculates a new {@link Location} object representing the midpoint (centroid) between two given locations.
     * The resulting location will have:
     * - Latitude, Longitude, Altitude calculated via {@link #calculateCentroid(List)}.
     * - Time set to the mean time via {@link #computeMeanTime(Location, Location)}.
     * - Speed set based on the 3D speed between the two points via {@link #calculateSpeed3D(Location, Location)}.
     *
     * @param gpxPointA The first {@link Location} object.
     * @param gpxPointB The second {@link Location} object.
     * @return A new {@link Location} representing the calculated centroid with mean time and 3D speed.
     * @throws IllegalArgumentException if the time difference between the points is zero or negative (from `calculateSpeed3D`).
     */
    @NonNull
    public static Location calculateCentroidLocation(Location gpxPointA, Location gpxPointB) {
        Location centroidLocation = calculateCentroid(Arrays.asList(gpxPointA, gpxPointB));

        float speed = calculateSpeed3D(gpxPointA, gpxPointB);
        centroidLocation.setSpeed(speed);
        centroidLocation.setTime(computeMeanTime(gpxPointA, gpxPointB));
        return centroidLocation;
    }
}
