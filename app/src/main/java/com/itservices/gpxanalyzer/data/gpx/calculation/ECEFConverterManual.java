package com.itservices.gpxanalyzer.data.gpx.calculation;

public class ECEFConverterManual {
    // WGS84 ellipsoid constants
    private static final double a = 6378137.0;            // Semi-major axis [meters]
    private static final double b = 6356752.314245;       // Semi-minor axis [meters]
    private static final double eSquared = 1 - (b * b) / (a * a);
    // Second eccentricity squared, e'^2 = (a^2 - b^2) / b^2
    private static final double e2Prime = eSquared / (1 - eSquared);

    /**
     * Converts geodetic coordinates to ECEF.
     *
     * @param latitude  Latitude in degrees
     * @param longitude Longitude in degrees
     * @param altitude  Altitude in meters
     * @return Array containing ECEF coordinates [x, y, z] in meters
     */
    public static double[] geodeticToECEF(double latitude, double longitude, double altitude) {
        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);

        double sinLat = Math.sin(radLat);
        double cosLat = Math.cos(radLat);
        double sinLon = Math.sin(radLon);
        double cosLon = Math.cos(radLon);

        // Radius of curvature in the prime vertical
        double N = a / Math.sqrt(1 - eSquared * sinLat * sinLat);

        double x = (N + altitude) * cosLat * cosLon;
        double y = (N + altitude) * cosLat * sinLon;
        // (b*b)/(a*a) = (1 - e^2). So the expression is (N(1-e^2) + altitude) * sinLat
        double z = (N * (1 - eSquared) + altitude) * sinLat;

        return new double[]{x, y, z};
    }

    /**
     * Converts ECEF coordinates to geodetic.
     *
     * @param x ECEF X in meters
     * @param y ECEF Y in meters
     * @param z ECEF Z in meters
     * @return Array containing geodetic coordinates [latitude, longitude, altitude]
     */
    public static double[] ecefToGeodetic(double x, double y, double z) {
        // Longitude is straightforward
        double lon = Math.atan2(y, x);
        double p   = Math.sqrt(x * x + y * y);

        // Bowring’s formula for initial latitude
        // theta = atan2(z * a, p * b)
        double theta = Math.atan2(z * a, p * b);
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        // Bowring formula for latitude:
        // lat = atan2(z + e'^2 * b * sin^3(theta), p - e^2 * a * cos^3(theta))
        double lat = Math.atan2(
                z + e2Prime * b * sinTheta * sinTheta * sinTheta,
                p - eSquared * a * cosTheta * cosTheta * cosTheta
        );

        // Radius of curvature in the prime vertical, N
        double sinLat = Math.sin(lat);
        double N = a / Math.sqrt(1 - eSquared * sinLat * sinLat);

        // Altitude above the ellipsoid
        double alt = p / Math.cos(lat) - N;

        // Convert back to degrees for latitude/longitude
        return new double[]{
                Math.toDegrees(lat),
                Math.toDegrees(lon),
                alt
        };
    }
}
