package com.itservices.gpxanalyzer.logbook.chart.settings.axis;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.utils.common.FormatNumberUtil;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class HourMinutesAxisValueFormatter implements IAxisValueFormatter, IValueFormatter {
	public static final float MAX_X_SCALED_TIME = 23f + getFractionOfFullHourFromMinutes(59) + getFractionOfFullHourFromSeconds(59);
	public static final float MIN_X_SCALED_TIME = 0.0f;

	private final Context context;

	@Inject
	HourMinutesAxisValueFormatter(@ApplicationContext Context context) {
		this.context = context;
	}

	public static float getFractionOfFullMinutesFromSeconds(float seconds) {
		return seconds / 60.0f;
	}

	public static float getFractionOfFullHourFromMinutes(float minutes) {
		return minutes / 60.0f;
	}

	public static float getFractionOfFullHourFromSeconds(float minutes) {
		return minutes / 3600.0f;
	}

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

	public static float combineIntoFloatTime(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);

		float time = getFloatTime(hour, minutes, sec);

		return time;
	}

	private static float getFloatTime(int hour, int minutes, int sec) {
		return hour
				+ getFractionOfFullHourFromMinutes(minutes)
				+ getFractionOfFullHourFromSeconds(sec);
	}

	public String getFormattedValue(float value) {
		Calendar calendar = combineIntoCalendarTime((double) value);

		return FormatNumberUtil.getFormattedTime(calendar);
	}

	@Override
	public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
		return getFormattedValue(value);
	}

	@Override
	public String getFormattedValue(float value, AxisBase axis) {
		return getFormattedValue(value);
	}
}
