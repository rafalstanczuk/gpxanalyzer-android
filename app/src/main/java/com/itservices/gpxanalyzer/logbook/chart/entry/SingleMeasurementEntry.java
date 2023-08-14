package com.itservices.gpxanalyzer.logbook.chart.entry;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.logbook.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class SingleMeasurementEntry extends BaseEntry {
	public static final String MEASUREMENT = "MEASUREMENT";
	public static boolean SHOW_COLOR_SINGLE_MEASUREMENT_RANGE_CIRCLES_ICONS = true;

	SingleMeasurementEntry(
		Calendar calendar, float x, float y, Drawable icon, StatisticResults statisticResults
	) {
		super(x, y, icon, statisticResults, calendar);
	}

	public static SingleMeasurementEntry create(
			PaletteColorDeterminer paletteColorDeterminer,
			StatisticResults statisticResults,
			float x, float y
	) {
		Drawable drawableIcon = null;

		try {
			int colorInt = paletteColorDeterminer.determineDiscreteColorFromScaledValue(
					y,
					(float) 0,
					(float) statisticResults.getMaxValue(),
					10,
					PaletteColorDeterminer.PaletteDirection.MAX_IS_ZERO_INDEX_Y_PIXEL);
			drawableIcon = IconsUtil.getDrawableIconForAreaColorId(colorInt, 10, false);
		} catch (Exception ex) {
			Log.e("MeasurementCurveEntry", "create: ", ex);
		}

		Calendar calendar = statisticResults.getMeasurements().elementAt((int) x).timestamp;

		float timeConcat = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);

		return new SingleMeasurementEntry(
				calendar, timeConcat, y, SHOW_COLOR_SINGLE_MEASUREMENT_RANGE_CIRCLES_ICONS ? drawableIcon : null,
				statisticResults
		);
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
