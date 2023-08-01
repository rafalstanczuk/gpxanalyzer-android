package com.itservices.gpxanalyzer.logbook.chart.entry;

import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.getRangeOfMeasurement;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.itservices.gpxanalyzer.logbook.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

public class CurveMeasurementEntry extends BaseEntry {
	public static final String CURVE_MEASUREMENT = "CURVE_MEASUREMENT";
	public static final int FILL_COLOR_UNDER_CURVE = ColorUtil.rgb(0.96f, 0.96f, 0.96f);
	public static final int FILL_COLOR_ALPHA_UNDER_CURVE = (int) (0.3f * 255.0f);
	public static boolean SHOW_COLOR_CURVE_MEASUREMENT_RANGE_CIRCLES_ICONS = false;
	public static boolean SHOW_COLOR_CURVE_MEASUREMENT_RANGE_CIRCLES_ONLY_AS_DEFAULT = false;

	CurveMeasurementEntry(
		Calendar calendar, float x, float y, Drawable icon, StatisticResults statisticResults
	) {
		super(x, y, icon, statisticResults, calendar);
	}

	public static CurveMeasurementEntry create(
		Context context, final List<Drawable> drawableIconList, StatisticResults statisticResults,
		float x, float y
	) {

		int areaColorId = getRangeOfMeasurement((int) y, context);

		if (SHOW_COLOR_CURVE_MEASUREMENT_RANGE_CIRCLES_ONLY_AS_DEFAULT) {
			areaColorId = drawableIconList.size() - 1;
		}

		Drawable drawableIcon = null;

		try {
			drawableIcon = drawableIconList.get(areaColorId);
		} catch (Exception ex) {
			Log.e("MeasurementCurveEntry", "create: ", ex);
		}

		Calendar calendar = statisticResults.getMeasurements().elementAt((int) x).timestamp;

		float timeConcat = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);

		return new CurveMeasurementEntry(
			calendar, timeConcat, y, SHOW_COLOR_CURVE_MEASUREMENT_RANGE_CIRCLES_ICONS ? drawableIcon : null,
			statisticResults
		);
	}

	@NonNull
	public static LineDataSet createCurveMeasurementLineDataSet(ArrayList<Entry> entries) {
		LineDataSet dataSet = new LineDataSet(entries, CURVE_MEASUREMENT);
		dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
		dataSet.setHighlightEnabled(true);

		dataSet.setDrawCircles(false);
		dataSet.setLineWidth(1.0f);

		//For static line feature marker purpose!
		dataSet.setDrawHorizontalHighlightIndicator(false);
/*
		dataSet.setCircleRadius(5f);
		dataSet.setDrawCircleHole(false);*/

		dataSet.setColor(Color.BLACK);
		//dataSet.setCircleColor(Color.BLUE);

		dataSet.setDrawFilled(true);
		dataSet.setFillColor(FILL_COLOR_UNDER_CURVE);
		dataSet.setFillAlpha(FILL_COLOR_ALPHA_UNDER_CURVE);

		//dataSet.setDrawFilled(true);

		dataSet.setHighLightColor(Color.BLACK);

		dataSet.setDrawIcons(SHOW_COLOR_CURVE_MEASUREMENT_RANGE_CIRCLES_ICONS);
		dataSet.setDrawValues(false);

		return dataSet;
	}
}
