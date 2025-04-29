package com.itservices.gpxanalyzer.data.model.geocoding;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Common model class for both forward and reverse geocoding results.
 * Maps.co returns different JSON structures for forward and reverse geocoding,
 * this class provides a unified format for app usage.
 */
public class GeocodingResult {
    @SerializedName("place_id")
    public String placeId;

    @SerializedName("licence")
    public String license;

    @SerializedName("osm_type")
    public String osmType;

    @SerializedName("osm_id")
    public String osmId;

    @SerializedName("lat")
    public double latitude;

    @SerializedName("lon")
    public double longitude;

    @SerializedName("display_name")
    public String displayName;

    // Structured address components
    @SerializedName("house_number")
    public String houseNumber;

    @SerializedName("road")
    public String street;

    @SerializedName("suburb")
    public String suburb;

    @SerializedName("city")
    public String city;

    @SerializedName("state")
    public String state;

    @SerializedName("country")
    public String country;

    @SerializedName("postcode")
    public String postalCode;
    
    @SerializedName("importance")
    public double importance;
    
    @SerializedName("boundingbox")
    public String[] boundingBox;

    /**
     * Returns this location as a GeoPoint for use with mapping libraries.
     * 
     * @return GeoPoint representation of this location
     */
    public Location toLocation() {
        Location location = new Location("GeocodingResult");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        return location;
    }
    
    /**
     * Gets a formatted address string from the individual components.
     * If displayName is available, returns that; otherwise builds an address
     * from the components.
     * 
     * @return Formatted address string
     */
    public String getFormattedAddress() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        
        StringBuilder sb = new StringBuilder();
        if (houseNumber != null && !houseNumber.isEmpty()) {
            sb.append(houseNumber).append(" ");
        }
        
        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }
        
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(city);
        }
        
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(state);
        }
        
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(postalCode);
        }
        
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(country);
        }
        
        return sb.toString();
    }

    public String getFormattedAddressShort() {

        StringBuilder sb = new StringBuilder();

        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(city);
        }

        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(state);
        }

        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(country);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "\nGeocodingResult{" +
                "\nplaceId='" + placeId + '\'' +
                ",\n license='" + license + '\'' +
                ",\n osmType='" + osmType + '\'' +
                ",\n osmId='" + osmId + '\'' +
                ",\n latitude=" + latitude +
                ",\n longitude=" + longitude +
                ",\n displayName='" + displayName + '\'' +
                ",\n houseNumber='" + houseNumber + '\'' +
                ",\n street='" + street + '\'' +
                ",\n suburb='" + suburb + '\'' +
                ",\n city='" + city + '\'' +
                ",\n state='" + state + '\'' +
                ",\n country='" + country + '\'' +
                ",\n postalCode='" + postalCode + '\'' +
                ",\n importance=" + importance +
                ",\n boundingBox=" + Arrays.toString(boundingBox) +
                "}\n";
    }
}