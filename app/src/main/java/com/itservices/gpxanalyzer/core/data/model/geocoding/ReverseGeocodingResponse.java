package com.itservices.gpxanalyzer.core.data.model.geocoding;

import com.google.gson.annotations.SerializedName;

/**
 * Model class representing the response from Maps.co Reverse Geocoding API.
 * The API returns a single location result with address details.
 */
public class ReverseGeocodingResponse {
    @SerializedName("place_id")
    public long placeId;

    @SerializedName("licence")
    public String license;

    @SerializedName("osm_type")
    public String osmType;

    @SerializedName("osm_id")
    public long osmId;

    @SerializedName("lat")
    public String latitude;

    @SerializedName("lon")
    public String longitude;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("address")
    public AddressComponent address;

    /**
     * Model class for the address component in the reverse geocoding response.
     */
    public static class AddressComponent {
        @SerializedName("house_number")
        public String houseNumber;

        @SerializedName("road")
        public String road;

        @SerializedName("suburb")
        public String suburb;

        @SerializedName("city")
        public String city;

        @SerializedName("state")
        public String state;

        @SerializedName("postcode")
        public String postcode;

        @SerializedName("country")
        public String country;

        @SerializedName("country_code")
        public String countryCode;
    }

    /**
     * Converts the response to a common GeocodingResult format for consistency.
     * 
     * @return A GeocodingResult containing the essential location data
     */
    public GeocodingResult toGeocodingResult() {
        GeocodingResult result = new GeocodingResult();
        result.placeId = String.valueOf(this.placeId);
        result.displayName = this.displayName;
        
        try {
            result.latitude = Double.parseDouble(this.latitude);
            result.longitude = Double.parseDouble(this.longitude);
        } catch (NumberFormatException e) {
            // Handle parsing error
        }
        
        if (this.address != null) {
            result.houseNumber = this.address.houseNumber;
            result.street = this.address.road;
            result.city = this.address.city;
            result.state = this.address.state;
            result.country = this.address.country;
            result.postalCode = this.address.postcode;
        }
        
        return result;
    }
} 