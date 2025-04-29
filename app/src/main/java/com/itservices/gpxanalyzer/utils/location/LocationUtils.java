package com.itservices.gpxanalyzer.utils.location;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {
    private static final float MIN_DISTANCE_KM = 1.0f; // Minimum distance between locations in kilometers

    /**
     * Creates a new Location object with the given coordinates.
     * 
     * @param latitude The latitude in degrees
     * @param longitude The longitude in degrees
     * @return A new Location object with the specified coordinates
     */
    public static Location createLocation(double latitude, double longitude) {
        Location location = new Location("gpx");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(0.0); // Default altitude
        location.setTime(System.currentTimeMillis()); // Current time
        return location;
    }

    /**
     * Calculates the distance between two locations in kilometers.
     */
    private static float calculateDistance(Location loc1, Location loc2) {
        return loc1.distanceTo(loc2) / 1000.0f; // Convert meters to kilometers
    }

    /**
     * Verifies that all locations are at least MIN_DISTANCE_KM apart.
     * Throws an exception if any pair is too close.
     */
    private static void verifyDistances(List<Location> coordinates) {
        for (int i = 0; i < coordinates.size(); i++) {
            for (int j = i + 1; j < coordinates.size(); j++) {
                float distance = calculateDistance(coordinates.get(i), coordinates.get(j));
                if (distance < MIN_DISTANCE_KM) {
                    throw new IllegalStateException(
                        String.format("Locations %d (%s) and %d (%s) are too close: %.2f km", 
                            i, getLocationName(i), j, getLocationName(j), distance));
                }
            }
        }
    }

    /**
     * Returns the name of the location at the given index.
     */
    private static String getLocationName(int index) {
        if (index < 20) return "North America location " + (index + 1);
        if (index < 35) return "South America location " + (index - 19);
        if (index < 55) return "Europe location " + (index - 34);
        if (index < 70) return "Africa location " + (index - 54);
        if (index < 90) return "Asia location " + (index - 69);
        return "Oceania location " + (index - 89);
    }

    /**
     * Creates a list of sample coordinates for testing and demonstration purposes.
     * All locations are at least 1km apart from each other.
     * 
     * @return A list of Location objects with sample coordinates
     */
    public static List<Location> createSampleCoordinates() {
        List<Location> coordinates = new ArrayList<>();
        
        // North America (20 locations)
        coordinates.add(createLocation(40.7128, -74.0060));  // New York City
        coordinates.add(createLocation(34.0522, -118.2437)); // Los Angeles
        coordinates.add(createLocation(41.8781, -87.6298));  // Chicago
        coordinates.add(createLocation(29.7604, -95.3698));  // Houston
        coordinates.add(createLocation(39.7392, -104.9903)); // Denver
        coordinates.add(createLocation(25.7617, -80.1918));  // Miami
        coordinates.add(createLocation(45.4215, -75.6972));  // Ottawa
        coordinates.add(createLocation(43.6532, -79.3832));  // Toronto
        coordinates.add(createLocation(49.2827, -123.1207)); // Vancouver
        coordinates.add(createLocation(19.4326, -99.1332));  // Mexico City
        coordinates.add(createLocation(20.6843, -88.5678));  // Chichen Itza
        coordinates.add(createLocation(21.1619, -86.8515));  // Cancun
        coordinates.add(createLocation(14.6349, -90.5069));  // Guatemala City
        coordinates.add(createLocation(9.9281, -84.0907));   // San Jose, Costa Rica
        coordinates.add(createLocation(8.9824, -79.5199));   // Panama City
        coordinates.add(createLocation(18.4655, -66.1057));  // San Juan, Puerto Rico
        coordinates.add(createLocation(18.2208, -63.0686));  // The Valley, Anguilla
        coordinates.add(createLocation(17.9714, -76.7922));  // Kingston, Jamaica
        coordinates.add(createLocation(13.1132, -59.5988));  // Bridgetown, Barbados
        coordinates.add(createLocation(12.1165, -68.9320));  // Willemstad, Curaçao

        // South America (15 locations)
        coordinates.add(createLocation(-12.0464, -77.0428)); // Lima, Peru
        coordinates.add(createLocation(-33.4489, -70.6693)); // Santiago, Chile
        coordinates.add(createLocation(-34.6037, -58.3816)); // Buenos Aires, Argentina
        coordinates.add(createLocation(-23.5505, -46.6333)); // São Paulo, Brazil
        coordinates.add(createLocation(-22.9068, -43.1729)); // Rio de Janeiro, Brazil
        coordinates.add(createLocation(-15.7801, -47.9292)); // Brasília, Brazil
        coordinates.add(createLocation(-13.1631, -72.5450)); // Machu Picchu, Peru
        coordinates.add(createLocation(-0.1807, -78.4678));  // Quito, Ecuador
        coordinates.add(createLocation(4.7110, -74.0721));   // Bogotá, Colombia
        coordinates.add(createLocation(10.4806, -66.9036));  // Caracas, Venezuela
        coordinates.add(createLocation(-8.7832, -55.4915));  // Amazon Rainforest, Brazil
        coordinates.add(createLocation(-13.2583, -72.2643)); // Sacred Valley, Peru (updated coordinates)
        coordinates.add(createLocation(-16.5000, -68.1500)); // La Paz, Bolivia
        coordinates.add(createLocation(-25.2637, -57.5759)); // Asunción, Paraguay
        coordinates.add(createLocation(-34.9011, -56.1645)); // Montevideo, Uruguay

        // Europe (20 locations)
        coordinates.add(createLocation(51.5074, -0.1278));   // London, UK
        coordinates.add(createLocation(48.8566, 2.3522));    // Paris, France
        coordinates.add(createLocation(52.5200, 13.4050));   // Berlin, Germany
        coordinates.add(createLocation(41.9028, 12.4964));   // Rome, Italy
        coordinates.add(createLocation(40.4168, -3.7038));   // Madrid, Spain
        coordinates.add(createLocation(59.3293, 18.0686));   // Stockholm, Sweden
        coordinates.add(createLocation(55.6761, 12.5683));   // Copenhagen, Denmark
        coordinates.add(createLocation(52.3676, 4.9041));    // Amsterdam, Netherlands
        coordinates.add(createLocation(50.8503, 4.3517));    // Brussels, Belgium
        coordinates.add(createLocation(47.4979, 19.0402));   // Budapest, Hungary
        coordinates.add(createLocation(48.2082, 16.3738));   // Vienna, Austria
        coordinates.add(createLocation(45.8150, 15.9819));   // Zagreb, Croatia
        coordinates.add(createLocation(41.0082, 28.9784));   // Istanbul, Turkey
        coordinates.add(createLocation(60.1699, 24.9384));   // Helsinki, Finland
        coordinates.add(createLocation(59.9139, 10.7522));   // Oslo, Norway
        coordinates.add(createLocation(53.3498, -6.2603));   // Dublin, Ireland
        coordinates.add(createLocation(38.7223, -9.1393));   // Lisbon, Portugal
        coordinates.add(createLocation(37.9838, 23.7275));   // Athens, Greece
        coordinates.add(createLocation(50.0755, 14.4378));   // Prague, Czech Republic
        coordinates.add(createLocation(55.7558, 37.6173));   // Moscow, Russia
        coordinates.add(createLocation(64.1466, -21.9426));  // Reykjavik, Iceland

        // Africa (15 locations)
        coordinates.add(createLocation(-33.9249, 18.4241));  // Cape Town, South Africa
        coordinates.add(createLocation(-26.2041, 28.0473));  // Johannesburg, South Africa
        coordinates.add(createLocation(-1.2921, 36.8219));   // Nairobi, Kenya
        coordinates.add(createLocation(30.0444, 31.2357));   // Cairo, Egypt
        coordinates.add(createLocation(6.5244, 3.3792));     // Lagos, Nigeria
        coordinates.add(createLocation(9.0579, 7.4951));     // Abuja, Nigeria
        coordinates.add(createLocation(-4.0435, 39.6682));   // Mombasa, Kenya
        coordinates.add(createLocation(-15.3875, 28.3228));  // Lusaka, Zambia
        coordinates.add(createLocation(-17.8252, 31.0335));  // Harare, Zimbabwe
        coordinates.add(createLocation(14.7175, -17.4677));  // Dakar, Senegal
        coordinates.add(createLocation(6.1304, 1.2158));     // Lomé, Togo
        coordinates.add(createLocation(5.5560, -0.1969));    // Accra, Ghana
        coordinates.add(createLocation(12.3714, -1.5197));   // Ouagadougou, Burkina Faso
        coordinates.add(createLocation(13.4549, -16.5790));  // Banjul, Gambia

        // Asia (20 locations)
        coordinates.add(createLocation(35.6762, 139.6503));  // Tokyo, Japan
        coordinates.add(createLocation(31.2304, 121.4737));  // Shanghai, China
        coordinates.add(createLocation(22.3193, 114.1694));  // Hong Kong
        coordinates.add(createLocation(1.3521, 103.8198));   // Singapore
        coordinates.add(createLocation(13.7563, 100.5018));  // Bangkok, Thailand
        coordinates.add(createLocation(3.1390, 101.6869));   // Kuala Lumpur, Malaysia
        coordinates.add(createLocation(14.5995, 120.9842));  // Manila, Philippines
        coordinates.add(createLocation(37.5665, 126.9780));  // Seoul, South Korea
        coordinates.add(createLocation(25.0330, 121.5654));  // Taipei, Taiwan
        coordinates.add(createLocation(22.3964, 114.1095));  // Shenzhen, China
        coordinates.add(createLocation(19.0760, 72.8777));   // Mumbai, India
        coordinates.add(createLocation(28.6139, 77.2090));   // New Delhi, India
        coordinates.add(createLocation(23.8103, 90.4125));   // Dhaka, Bangladesh
        coordinates.add(createLocation(27.7172, 85.3240));   // Kathmandu, Nepal
        coordinates.add(createLocation(6.9271, 79.8612));    // Colombo, Sri Lanka
        coordinates.add(createLocation(21.0278, 105.8342));  // Hanoi, Vietnam
        coordinates.add(createLocation(10.8231, 106.6297));  // Ho Chi Minh City, Vietnam
        coordinates.add(createLocation(11.5564, 104.9282));  // Phnom Penh, Cambodia
        coordinates.add(createLocation(16.8409, 96.1735));   // Yangon, Myanmar
        coordinates.add(createLocation(25.2048, 55.2708));   // Dubai, UAE

        // Oceania (10 locations)
        coordinates.add(createLocation(-33.8688, 151.2093)); // Sydney, Australia
        coordinates.add(createLocation(-37.8136, 144.9631)); // Melbourne, Australia
        coordinates.add(createLocation(-36.8485, 174.7633)); // Auckland, New Zealand
        coordinates.add(createLocation(-41.2866, 174.7756)); // Wellington, New Zealand
        coordinates.add(createLocation(-8.8742, 125.7275));  // Dili, East Timor
        coordinates.add(createLocation(-9.4438, 147.1803));  // Port Moresby, Papua New Guinea
        coordinates.add(createLocation(-18.1248, 178.4501)); // Suva, Fiji
        coordinates.add(createLocation(-13.8333, -171.7667)); // Apia, Samoa
        coordinates.add(createLocation(13.4443, 144.7937));  // Hagåtña, Guam
        coordinates.add(createLocation(7.5149, 134.5825));   // Koror, Palau

        return coordinates;
    }
} 