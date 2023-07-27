package com.itservices.gpxanalyzer.logbook.chart.entry;

import static com.itservices.gpxanalyzer.logbook.chart.entry.ColorUtil.setAlphaInIntColor;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.MEASUREMENT_5RANGES_COLOR_LIST;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IconsUtil {

	public static List<Drawable> generateDrawableIconForAreaList(int size, int alpha) {
		List<Drawable> measurementDrawableIconList = new ArrayList<>();

		for (int color : MEASUREMENT_5RANGES_COLOR_LIST) {

			int colorWithAlpha = setAlphaInIntColor(color, alpha);

			measurementDrawableIconList.add(
				getDrawableIconForAreaColorId(colorWithAlpha, size)
			);
		}

		addDefaultIcon(size, alpha, measurementDrawableIconList);

		return measurementDrawableIconList;
	}

	private static void addDefaultIcon(int size, int alpha, List<Drawable> measurementDrawableIconList) {
		int defaultWithAlpha = setAlphaInIntColor(Color.BLACK, alpha);

		measurementDrawableIconList.add(
			getDrawableIconForAreaColorId(defaultWithAlpha, size)
		);
	}

	private static Drawable getDrawableIconForAreaColorId(int color, int size) {

		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.OVAL);
		shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
		shape.setColor(color);
		shape.setStroke(1, Color.BLACK);
		shape.setSize(size, size);

		return shape;
	}

	public static final long YEAR_MULTIPLIKATOR = 10000000000L;
	public static final int MONTH_MULTIPLIKATOR = 100000000;
	public static final int DATE_MULTIPLIKATOR = 1000000;
	public static final int HOUR_MULTIPLIKATOR = 10000;
	public static final int MINUTE_MULTIPLIKATOR = 100;

	public static int getTimeAsIntFromHour(int hour, int min) {
		return (MINUTE_MULTIPLIKATOR * hour) + min;
	}

	@NonNull
	public static String getFormattedTime(int time, Context context) {
		return getFormattedTimeAmPm(time, false);
	}

	public static String getFormattedTimeAmPm(int time, boolean isAmPm) {
		int hour = (time / MINUTE_MULTIPLIKATOR);
		int minute = (time % MINUTE_MULTIPLIKATOR);

		if (isAmPm) {
			String amPm;

			if (hour > 11) {
				amPm = "pm";

				if (hour > 12) {
					hour -= 12;
				}
			} else {
				if (hour == 0) {
					hour = 12;
				}

				amPm = "am";
			}

			if (minute == 0) {
				return hour + ":00 " + amPm;
				// return [NSString stringWithFormat:@"%i:00 %@", hour, amPm];
			} else if (minute < 10) {
				return hour + ":0" + minute + " " + amPm;
				// return [NSString stringWithFormat:@"%i:0%i %@", hour, minute, amPm];
			} else {
				return hour + ":" + minute + " " + amPm;
				// return [NSString stringWithFormat:@"%i:%i %@", hour, minute, amPm];
			}
		} else {
			if (minute == 0) {
				return hour + ":00";
				// return [NSString stringWithFormat:@"%i:00", hour];
			} else if (minute < 10) {
				return hour + ":0" + minute;
				// return [NSString stringWithFormat:@"%i:0%i", hour, minute];
			} else {
				return hour + ":" + minute;
				// return [NSString stringWithFormat:@"%i:%i", hour, minute];
			}
		}
	}

	@NonNull
	public static Calendar getDateFromIntTimestamp(long timestamp) {
		int timestampInt = (int) (timestamp / (long) MONTH_MULTIPLIKATOR);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, timestampInt);

		timestampInt = (int) (timestamp % (long) MONTH_MULTIPLIKATOR);

		cal.set(Calendar.MONTH, (timestampInt / DATE_MULTIPLIKATOR) - 1);

		timestampInt %= DATE_MULTIPLIKATOR;

		cal.set(Calendar.DATE, (timestampInt / HOUR_MULTIPLIKATOR));

		timestampInt %= HOUR_MULTIPLIKATOR;

		cal.set(Calendar.HOUR_OF_DAY, (timestampInt / MINUTE_MULTIPLIKATOR));
		cal.set(Calendar.MINUTE, (timestampInt % MINUTE_MULTIPLIKATOR));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}

	public static int getTimeAsIntFromDate(@NonNull Calendar cal) {
		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int sec = cal.get(Calendar.SECOND);
		int ms = cal.get(Calendar.MILLISECOND);

		return getTimeAsIntFromHour(hour, min);
	}
	public static final int NDIG_PREC_COMP = 10;

	public static boolean isValueBetweenIncluded(long testVal, long start, long end) {
		return testVal >= start && testVal <= end;
	}

	public static boolean isValueBetweenIncluded(float testVal, float start, float end) {
		return testVal >= start && testVal <= end;
	}

	public static boolean isValueBetweenIncluded(float testVal, float start, float end, int nDigits) {
		return isGreaterEqual(testVal, start, nDigits) && isLessEqual(testVal, end, nDigits);
	}

	public static boolean isEqualOneDigitPrecision(float first, float second) {
		return isEqualNDigitsPrecision(first, second, 1);
	}

	public static boolean isEqualNDigitsPrecision(float first, float second, int nDigits) {
		return Math.abs(first - second) <  Math.pow(10, -nDigits);
	}

	public static boolean isGreaterEqual(float first, float second, int nDigits) {
		return first > second
				||
				isEqualNDigitsPrecision(first, second, nDigits);
	}

	public static boolean isLessEqual(float first, float second, int nDigits) {
		return first < second
				||
				isEqualNDigitsPrecision(first, second, nDigits);
	}

	public static boolean greaterEqualOneDigitPrecision(float first, float second) {
		return isGreaterEqual(first, second, 1);
	}

	public static boolean lessEqualOneDigitPrecision(float first, float second) {
		return isLessEqual(first, second, 1);
	}

	public static float roundToOneDigit(float value) {
		return roundToNDigits(value, 1);
	}

	public static float roundToNDigits(float value, int nDigits) {
		int scale = (int) Math.pow(10, nDigits);
		return (float) Math.round(value * scale) / scale;
	}

}
