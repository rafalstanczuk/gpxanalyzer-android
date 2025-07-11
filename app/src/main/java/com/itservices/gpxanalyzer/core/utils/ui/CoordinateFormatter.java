package com.itservices.gpxanalyzer.core.utils.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.R;

import java.util.Locale;

/**
 * Utility class for formatting geographic coordinates in various formats.
 */
public class CoordinateFormatter {

    /**
     * Formats a latitude coordinate string into Degrees, Minutes, Seconds format with N/S indicator.
     * The cardinal direction (N/S) is formatted in bold.
     * 
     * Example: "50.123" becomes "50° 7' 22.8" N" where "N" is bold.
     * 
     * @param latitudeStr The latitude as a string, which can be parsed as a double.
     * @param context Context for accessing string resources.
     * @return A SpannableStringBuilder with the formatted DMS coordinates.
     */
    @NonNull
    public static SpannableStringBuilder formatLatitudeDMS(String latitudeStr, Context context) {
        try {
            double latitude = Double.parseDouble(latitudeStr);
            return formatLatitudeDMS(latitude, context);
        } catch (NumberFormatException e) {
            // Return the original string if parsing fails
            return new SpannableStringBuilder(latitudeStr);
        }
    }
    
    /**
     * Formats a longitude coordinate string into Degrees, Minutes, Seconds format with E/W indicator.
     * The cardinal direction (E/W) is formatted in bold.
     * 
     * Example: "19.456" becomes "19° 27' 21.6" E" where "E" is bold.
     * 
     * @param longitudeStr The longitude as a string, which can be parsed as a double.
     * @param context Context for accessing string resources.
     * @return A SpannableStringBuilder with the formatted DMS coordinates.
     */
    @NonNull
    public static SpannableStringBuilder formatLongitudeDMS(String longitudeStr, Context context) {
        try {
            double longitude = Double.parseDouble(longitudeStr);
            return formatLongitudeDMS(longitude, context);
        } catch (NumberFormatException e) {
            // Return the original string if parsing fails
            return new SpannableStringBuilder(longitudeStr);
        }
    }
    
    /**
     * Formats a latitude value into Degrees, Minutes, Seconds format with N/S indicator.
     * The cardinal direction (N/S) is formatted in bold.
     * 
     * @param latitude The latitude value in decimal degrees.
     * @param context Context for accessing string resources.
     * @return A SpannableStringBuilder with the formatted DMS coordinates.
     */
    @NonNull
    public static SpannableStringBuilder formatLatitudeDMS(double latitude, Context context) {
        boolean isNorth = latitude >= 0;
        double absLatitude = Math.abs(latitude);
        String dmsStr = convertToDMS(absLatitude, context);
        
        String cardinal = " " + context.getString(isNorth ? R.string.coordinate_north : R.string.coordinate_south);
        SpannableStringBuilder builder = new SpannableStringBuilder(dmsStr);
        SpannableString cardinalSpannable = new SpannableString(cardinal);
        cardinalSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, cardinal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(cardinalSpannable);
        return builder;
    }
    
    /**
     * Formats a longitude value into Degrees, Minutes, Seconds format with E/W indicator.
     * The cardinal direction (E/W) is formatted in bold.
     * 
     * @param longitude The longitude value in decimal degrees.
     * @param context Context for accessing string resources.
     * @return A SpannableStringBuilder with the formatted DMS coordinates.
     */
    @NonNull
    public static SpannableStringBuilder formatLongitudeDMS(double longitude, Context context) {
        boolean isEast = longitude >= 0;
        double absLongitude = Math.abs(longitude);
        String dmsStr = convertToDMS(absLongitude, context);
        
        String cardinal = " " + context.getString(isEast ? R.string.coordinate_east : R.string.coordinate_west);
        SpannableStringBuilder builder = new SpannableStringBuilder(dmsStr);
        SpannableString cardinalSpannable = new SpannableString(cardinal);
        cardinalSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, cardinal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(cardinalSpannable);
        return builder;
    }
    
    /**
     * Converts a decimal degree value to degrees, minutes, seconds format string.
     * 
     * @param decimal The decimal degree value to convert.
     * @param context Context for accessing string resources.
     * @return A string in the format "DD° MM' SS.S""
     */
    private static String convertToDMS(double decimal, Context context) {
        int degrees = (int) decimal;
        double minutesDecimal = (decimal - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;
        
        return String.format(Locale.getDefault(), 
                context.getString(R.string.coordinate_dms_format), 
                degrees, minutes, seconds);
    }
} 