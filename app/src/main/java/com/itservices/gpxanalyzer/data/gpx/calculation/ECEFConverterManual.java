package com.itservices.gpxanalyzer.data.gpx.calculation;

public class ECEFConverterManual {
    // WGS84 ellipsoid constants
    private static final double a = 6378137.0; // Semi-major axis in meters
    private static final double b = 6356752.314245; // Semi-minor axis in meters
    private static final double eSquared = 1 - (b * b) / (a * a);

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

        double N = a / Math.sqrt(1 - eSquared * sinLat * sinLat);

        double x = (N + altitude) * cosLat * cosLon;
        double y = (N + altitude) * cosLat * sinLon;
        double z = ((b * b) / (a * a) * N + altitude) * sinLat;

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
        double lon = Math.toDegrees(Math.atan2(y, x));

        double p = Math.sqrt(x * x + y * y);
        double theta = Math.atan2(z * a, p * b);

        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        double lat = Math.toDegrees(Math.atan2(z + eSquared * (b) * sinTheta * sinTheta * sinTheta,
                p - eSquared * a * cosTheta * cosTheta * cosTheta));

        double sinLat = Math.sin(Math.toRadians(lat));
        double N = a / Math.sqrt(1 - eSquared * sinLat * sinLat);
        double alt = p / Math.cos(Math.toRadians(lat)) - N;

        return new double[]{lat, lon, alt};
    }
}
