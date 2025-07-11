package com.itservices.gpxanalyzer.core.ui.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class providing helper methods for String manipulation and formatting, primarily for UI display.
 */
public final class StringUtils {
    // Date format for displaying timestamps in UI elements.
    // TODO: Determine Locale based on longitude/latitude for more accurate local time representation.
    private static final SimpleDateFormat itemDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat itemDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat itemTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    /**
     * Formats a timestamp (in milliseconds since the epoch) into a human-readable date and time string
     * using the pattern "yyyy-MM-dd HH:mm" and the default locale.
     *
     * @param timeMillis The timestamp to format, in milliseconds since the epoch.
     * @return A formatted string (e.g., "2023-10-27 15:30") if the timestamp is positive and formatting is successful,
     *         otherwise returns "N/A".
     */
    @NonNull
    public static String getFormattedDateTimeMillisDate(long timeMillis) {
        String formattedDate = "N/A";
        if (timeMillis > 0) {
            try {
                Date date = new Date(timeMillis);
                formattedDate = itemDateTimeFormat.format(date);
            } catch (Exception ignored) {
            }
        }
        return formattedDate;
    }

    @NonNull
    public static String getFormattedDateMillisDate(long timeMillis) {
        String formattedDate = "N/A";
        if (timeMillis > 0) {
            try {
                Date date = new Date(timeMillis);
                formattedDate = itemDateFormat.format(date);
            } catch (Exception ignored) {
            }
        }
        return formattedDate;
    }

    @NonNull
    public static String getFormattedTimeMillisDate(long timeMillis) {
        String formattedDate = "N/A";
        if (timeMillis > 0) {
            try {
                Date date = new Date(timeMillis);
                formattedDate = itemTimeFormat.format(date);
            } catch (Exception ignored) {
            }
        }
        return formattedDate;
    }
}
