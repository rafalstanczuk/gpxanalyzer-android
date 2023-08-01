package com.itservices.gpxanalyzer.utils.common;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FormatNumberUtil {

    @NonNull
    public static String getFormattedTime(Calendar calendar) {
        return getFormattedTimeAmPm(calendar, false);
    }

    public static String getFormattedTimeAmPm(Calendar calendar, boolean isAmPm) {
        String timeFormat = "HH:mm:ss" + (isAmPm? " a" : "");

        String time = new SimpleDateFormat(timeFormat, Locale.getDefault()).format(calendar.getTime()).toLowerCase();

        return time;
    }
}
