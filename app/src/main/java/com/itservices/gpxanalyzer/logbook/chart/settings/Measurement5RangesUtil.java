package com.itservices.gpxanalyzer.logbook.chart.settings;

import android.content.Context;

import com.itservices.gpxanalyzer.logbook.chart.entry.ColorUtil;

import java.util.Arrays;
import java.util.List;

public class Measurement5RangesUtil {

	public static final int BEREICH_YELLOW = 0;
	public static final int BEREICH_GREEN = 1;
	public static final int BEREICH_ORANGE = 2;
	public static final int BEREICH_RED = 3;

	public static final int RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW = BEREICH_YELLOW;
	public static final int RANGE_ID_IN_TARGET_MIN_MAX_GREEN = BEREICH_GREEN;
	public static final int RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE = BEREICH_ORANGE;
	public static final int RANGE_ID_BELOW_HYPO_LIMIT_RED = BEREICH_RED;
	public static final int RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK = BEREICH_RED + 1;
/*	public static final int COLOR_DARK_GREEN = 0xFF33CE7D;
	public static final int COLOR_DARK_YELLOW = 0xFFFFFF00;
	public static final int COLOR_DARK_RED = 0xFF990000;
	public static final int COLOR_ORANGE = 0xFFD96647;*/
	public static final int RANGE_ABOVE_HIGH_LIMIT_HYPERGLYCEMIA_RANGE_COLOR_ORANGE = 0xD96647;
	public static final int RANGE_ABOVE_TARGET_MAX_COLOR_DARK_YELLOW = 0xFFFF00;
	public static final int RANGE_IN_TARGET_COLOR_DARK_GREEN = 0x33CE7D;
	public static final int RANGE_BELOW_TARGET_MIN_LIMIT_COLOR_PINK = 0xFB435D;
	public static final int RANGE_BELOW_LOW_LIMIT_HYPOGLYCEMIA_COLOR_DARK_RED = 0x990000;

	public static final int BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA = 55;

	public static final List<Integer> MEASUREMENT_5RANGES_COLOR_LIST =
		Arrays.asList(
			ColorUtil.setAlphaInIntColor(RANGE_ABOVE_TARGET_MAX_COLOR_DARK_YELLOW, BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA),
			ColorUtil.setAlphaInIntColor(RANGE_IN_TARGET_COLOR_DARK_GREEN, BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA),
			ColorUtil.setAlphaInIntColor(RANGE_ABOVE_HIGH_LIMIT_HYPERGLYCEMIA_RANGE_COLOR_ORANGE, BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA),
			ColorUtil.setAlphaInIntColor(RANGE_BELOW_LOW_LIMIT_HYPOGLYCEMIA_COLOR_DARK_RED, BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA),
			ColorUtil.setAlphaInIntColor(RANGE_BELOW_TARGET_MIN_LIMIT_COLOR_PINK, BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA)
		);

	public static int getColorForAreaId(int areaId) {
		return MEASUREMENT_5RANGES_COLOR_LIST.get(areaId);
	}

	public static int getRangeOfMeasurement(int value, Context context) {

		int hypo = (int) MeasurementBoundariesPreferences.LOW_VALUE_MEASUREMENT_DEFAULT;

		if (value < hypo) {
			return RANGE_ID_BELOW_HYPO_LIMIT_RED;
		} else if (value < MeasurementBoundariesPreferences.MEASUREMENT_TARGET_DEFAULT_MIN) {
			return RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK;
		} else if (value <= MeasurementBoundariesPreferences.MEASUREMENT_TARGET_DEFAULT_MAX) {
			return RANGE_ID_IN_TARGET_MIN_MAX_GREEN;
		} else if (value <= MeasurementBoundariesPreferences.MEASUREMENT_UPPER_MAX) {
			return RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW;
		}

		return RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE;
	}
}
