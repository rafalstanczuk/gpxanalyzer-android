package com.itservices.gpxanalyzer.utils.common;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Utility class for formatting numerical values, specifically time representations.
 */
public class FormatNumberUtil {


    /**
     * Formats a given time in milliseconds into a string representation (HH:mm:ss) using the default locale.
     *
     * @param timeInMillis The time to format, expressed in milliseconds since the epoch.
     * @return A formatted time string (e.g., "14:35:02").
     */
    @NonNull
    public static String getFormattedTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( timeInMillis );

        return getFormattedTime(calendar);
    }

    /**
     * Formats the time represented by a {@link Calendar} object into a string representation (HH:mm:ss)
     * using the default locale.
     *
     * @param calendar The {@link Calendar} object containing the time to format.
     * @return A formatted time string (e.g., "09:15:30").
     */
    @NonNull
    public static String getFormattedTime(Calendar calendar) {
        return getFormattedTimeAmPm(calendar, false);
    }

    /**
     * Formats the time represented by a {@link Calendar} object into a string representation,
     * optionally including AM/PM marker, using the default locale.
     *
     * @param calendar The {@link Calendar} object containing the time to format.
     * @param isAmPm   If true, includes the AM/PM marker (e.g., "02:35:02 pm").
     *                 If false, uses 24-hour format (e.g., "14:35:02").
     * @return A formatted time string according to the specified format.
     */
    public static String getFormattedTimeAmPm(Calendar calendar, boolean isAmPm) {
        String timeFormat = "HH:mm:ss" + (isAmPm? " a" : "");

        String time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(calendar.getTime()).toLowerCase();

        return time;
    }
}
