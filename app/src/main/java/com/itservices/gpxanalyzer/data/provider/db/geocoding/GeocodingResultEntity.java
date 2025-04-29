package com.itservices.gpxanalyzer.data.provider.db.geocoding;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;

/**
 * Entity class representing a geocoding result in the Room database.
 * Maps to the GeocodingResult model class.
 */
@Entity(tableName = "geocoding_results")
@TypeConverters({StringArrayConverter.class})
public class GeocodingResultEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String placeId;
    public String license;
    public String osmType;
    public String osmId;
    public double latitude;
    public double longitude;
    public String displayName;
    public String houseNumber;
    public String street;
    public String suburb;
    public String city;
    public String state;
    public String country;
    public String postalCode;
    public double importance;
    public String[] boundingBox;

    public GeocodingResultEntity() {
        // Default constructor required by Room
    }

    /**
     * Copy constructor for creating a new entity with the same values as another.
     *
     * @param other The entity to copy
     */
    public GeocodingResultEntity(GeocodingResultEntity other) {
        this.id = other.id;
        this.placeId = other.placeId;
        this.license = other.license;
        this.osmType = other.osmType;
        this.osmId = other.osmId;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.displayName = other.displayName;
        this.houseNumber = other.houseNumber;
        this.street = other.street;
        this.suburb = other.suburb;
        this.city = other.city;
        this.state = other.state;
        this.country = other.country;
        this.postalCode = other.postalCode;
        this.importance = other.importance;
        this.boundingBox = other.boundingBox != null ? other.boundingBox.clone() : null;
    }

    /**
     * Converts this entity to a GeocodingResult model.
     */
    public GeocodingResult toGeocodingResult() {
        GeocodingResult result = new GeocodingResult();
        result.placeId = this.placeId;
        result.license = this.license;
        result.osmType = this.osmType;
        result.osmId = this.osmId;
        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.displayName = this.displayName;
        result.houseNumber = this.houseNumber;
        result.street = this.street;
        result.suburb = this.suburb;
        result.city = this.city;
        result.state = this.state;
        result.country = this.country;
        result.postalCode = this.postalCode;
        result.importance = this.importance;
        result.boundingBox = this.boundingBox;
        return result;
    }

    /**
     * Creates a GeocodingResultEntity from a GeocodingResult model.
     */
    public static GeocodingResultEntity fromGeocodingResult(GeocodingResult result) {
        GeocodingResultEntity entity = new GeocodingResultEntity();
        entity.placeId = result.placeId;
        entity.license = result.license;
        entity.osmType = result.osmType;
        entity.osmId = result.osmId;
        entity.latitude = result.latitude;
        entity.longitude = result.longitude;
        entity.displayName = result.displayName;
        entity.houseNumber = result.houseNumber;
        entity.street = result.street;
        entity.suburb = result.suburb;
        entity.city = result.city;
        entity.state = result.state;
        entity.country = result.country;
        entity.postalCode = result.postalCode;
        entity.importance = result.importance;
        entity.boundingBox = result.boundingBox;
        return entity;
    }
} 