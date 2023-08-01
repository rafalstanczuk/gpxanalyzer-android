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

public class SingleMeasurementEntry extends BaseEntry {
	public static final String MEASUREMENT = "MEASUREMENT";

	SingleMeasurementEntry(
		Calendar calendar, float x, float y, Drawable icon, StatisticResults statisticResults
	) {
		super(x, y, icon, statisticResults, calendar);
	}

	public static SingleMeasurementEntry create(
		Context context, List<Drawable> drawableIconList, StatisticResults statisticResults,
		float x, float y
	) {

		int areaColorId = getRangeOfMeasurement((int) y, context);

		Drawable drawableIcon = null;

		try {
			drawableIcon = drawableIconList.get(areaColorId);
		} catch (Exception ex) {
			Log.e("MeasurementEntry", "create: ", ex);
		}


		Calendar calendar = statisticResults.getMeasurements().elementAt((int) x).timestamp;

		float timeConcat = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);

		return new SingleMeasurementEntry(calendar, timeConcat, y, drawableIcon, statisticResults);
	}

	@NonNull
	public static LineDataSet createSingleMeasurementLineDataSet(ArrayList<Entry> entries) {
		LineDataSet updatedDataSet = new LineDataSet(entries, MEASUREMENT);

		updatedDataSet.setLineWidth(0.0f);
		updatedDataSet.setDrawIcons(true);

		updatedDataSet.setDrawCircles(false);
		updatedDataSet.setHighlightEnabled(true);
		updatedDataSet.setHighLightColor(Color.BLACK);
		updatedDataSet.setDrawValues(false);
		updatedDataSet.setColor(0x000000FF);
		return updatedDataSet;
	}
}
