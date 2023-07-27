package com.itservices.gpxanalyzer.logbook.chart.settings;

import android.content.Context;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;

import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class HourMinutesAxisValueFormatter implements IAxisValueFormatter, IValueFormatter {
	public static final float MAX_X_SCALED_TIME = 23f + getFractionOfFullHourFromMinutes(59) ;
	public static final float MIN_X_SCALED_TIME = 0.0f;

	private final Context context;

	@Inject
	HourMinutesAxisValueFormatter(@ApplicationContext Context context) {
		this.context = context;
	}

	public static float getFractionOfFullMinutesFromSeconds(int seconds) {
		return (float)seconds / 60.0f;
	}

	public static float getFractionOfFullHourFromMinutes(int minutes) {
		return (float)minutes / 60.0f;
	}

	public static float getMinutesFromFraction(float fractionOfHour) {
		return fractionOfHour * 60.0f;
	}

	public static float getSecondsFromMinutesFraction(float fractionOfMinutes) {
		return fractionOfMinutes * 60.0f;
	}

	public static float combineIntoFloatTime(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);

		float time = hour + getFractionOfFullHourFromMinutes(minutes);

		return time;
	}

	public String getFormattedValue(float value) {
		BigDecimal bigDecimal = new BigDecimal(String.valueOf(value));

		int intValue = bigDecimal.intValue();
		float floatPart = Float.parseFloat( bigDecimal.subtract(
			new BigDecimal(intValue)).toPlainString()
		);


		int fixedMinute = (int)getMinutesFromFraction(floatPart);
		int hour = intValue;

		int newValue = IconsUtil.getTimeAsIntFromHour(hour, fixedMinute);

		return IconsUtil.getFormattedTime(newValue, context);
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
