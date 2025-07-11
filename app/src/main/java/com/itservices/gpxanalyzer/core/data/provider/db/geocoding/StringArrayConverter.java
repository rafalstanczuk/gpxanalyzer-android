package com.itservices.gpxanalyzer.core.data.provider.db.geocoding;

import androidx.room.TypeConverter;

/**
 * Type converter for handling String arrays in Room database.
 * Converts between String[] and String for storage.
 */
public class StringArrayConverter {
    private static final String SEPARATOR = ",";

    @TypeConverter
    public static String[] fromString(String value) {
        if (value == null || value.isEmpty()) {
            return new String[0];
        }
        return value.split(SEPARATOR);
    }

    @TypeConverter
    public static String fromStringArray(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(SEPARATOR, array);
    }
} 