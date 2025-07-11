package com.itservices.gpxanalyzer.core.ui.components.chart.settings.axis;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.core.utils.common.FormatNumberUtil;

import java.util.Calendar;

import javax.inject.Inject;

/**
 * Formatter for chart axis values (likely X-axis) and data point values that represent time.
 * It implements both {@link IAxisValueFormatter} and {@link IValueFormatter}.
 * The primary function is to convert a float value, representing a fraction of a full hour
 * (e.g., 1.5f means 1 hour and 30 minutes), into a standard "HH:mm:ss" time format string.
 * It provides utility methods for time conversions and constants related to time representation on the chart axis.
 */
public class HourMinutesAxisValueFormatter implements IAxisValueFormatter, IValueFormatter {
	/**
	 * Granularity for X-axis labels, represents 1 second in hour fraction format.
	 * Used to control the minimum interval between axis labels.
	 */
	public static final float GRANULARITY = HourMinutesAxisValueFormatter.getFractionOfFullHourFromSeconds(1);

	/** Default rotation angle for X-axis labels to prevent overlap. */
	public static final float LABEL_ROTATION_ANGLE = -45.0f;
	/** Default number of labels to display on the X-axis. */
	public static final int LABEL_COUNT = 12;

	/** Maximum possible value for the X-axis, representing 23:59:59 in fractional hour format. */
	public static final float MAX_X_SCALED_TIME = 23f + getFractionOfFullHourFromMinutes(59) + getFractionOfFullHourFromSeconds(59);
	/** Minimum possible value for the X-axis, representing 00:00:00. */
	public static final float MIN_X_SCALED_TIME = 0.0f;

	/**
	 * Constructor used by Hilt for dependency injection.
	 */
	@Inject
	HourMinutesAxisValueFormatter() {
	}

	/**
	 * Converts seconds into a fraction of a full minute.
	 *
	 * @param seconds The number of seconds.
	 * @return The equivalent fraction of a minute.
	 */
	public static float getFractionOfFullMinutesFromSeconds(float seconds) {
		return seconds / 60.0f;
	}

	/**
	 * Converts minutes into a fraction of a full hour.
	 *
	 * @param minutes The number of minutes.
	 * @return The equivalent fraction of an hour.
	 */
	public static float getFractionOfFullHourFromMinutes(float minutes) {
		return minutes / 60.0f;
	}

	/**
	 * Converts seconds into a fraction of a full hour.
	 *
	 * @param seconds The number of seconds.
	 * @return The equivalent fraction of an hour.
	 */
	public static float getFractionOfFullHourFromSeconds(float seconds) {
		return seconds / 3600.0f;
	}

	/**
	 * Converts a double value representing fractional hours into a {@link Calendar} object.
	 *
	 * @param doubleHours The time value in fractional hours (e.g., 1.75 for 1 hour 45 minutes).
	 * @return A {@link Calendar} object set to the corresponding hour, minute, and second.
	 */
	public static Calendar combineIntoCalendarTime(double doubleHours) {
		Calendar calendar = Calendar.getInstance();

		double doubleMinutes = ( doubleHours - Math.floor(doubleHours) ) * 60.0;
		double doubleSeconds = ( doubleMinutes - Math.floor(doubleMinutes) ) * 60.0;

		int hour = (int) Math.floor(doubleHours);
		int minutes = (int) Math.floor(doubleMinutes);
		int seconds = (int) Math.floor(doubleSeconds);

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);

		return calendar;
	}

	/**
	 * Converts a {@link Calendar} object into a float value representing fractional hours.
	 *
	 * @param calendar The {@link Calendar} object containing the time.
	 * @return The equivalent time value in fractional hours.
	 */
	public static float combineIntoFloatTime(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);

		float time = getFloatTime(hour, minutes, sec);

		return time;
	}

	/**
	 * Calculates the float representation of time (fractional hours) from hour, minute, and second components.
	 *
	 * @param hour The hour component.
	 * @param minutes The minute component.
	 * @param sec The second component.
	 * @return The time value in fractional hours.
	 */
	private static float getFloatTime(int hour, int minutes, int sec) {
		return hour
				+ getFractionOfFullHourFromMinutes(minutes)
				+ getFractionOfFullHourFromSeconds(sec);
	}

	/**
	 * Formats a float value (fractional hours) into a "HH:mm:ss" string.
	 *
	 * @param value The time value in fractional hours.
	 * @return The formatted time string (e.g., "01:30:00").
	 */
	public static String getFormattedValue(float value) {
		Calendar calendar = combineIntoCalendarTime((double) value);

		return FormatNumberUtil.getFormattedTime(calendar);
	}

	/**
	 * Implementation of {@link IValueFormatter}. Delegates to {@link #getFormattedValue(float)}.
	 */
	@Override
	public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
		return getFormattedValue(value);
	}

	/**
	 * Implementation of {@link IAxisValueFormatter}. Delegates to {@link #getFormattedValue(float)}.
	 */
	@Override
	public String getFormattedValue(float value, AxisBase axis) {
		return getFormattedValue(value);
	}
}
