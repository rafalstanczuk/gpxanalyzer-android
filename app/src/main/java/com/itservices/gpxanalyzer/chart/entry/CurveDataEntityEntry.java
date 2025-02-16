package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class CurveDataEntityEntry extends BaseDataEntityEntry {
	public static final String CURVE_DATA_ENTITY = "CURVE_DATA_ENTITY";
	public static final int FILL_COLOR_UNDER_CURVE = ColorUtil.rgb(0.96f, 0.96f, 0.96f);
	public static final int FILL_COLOR_ALPHA_UNDER_CURVE = (int) (0.3f * 255.0f);
	public static boolean SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;

	CurveDataEntityEntry(
			DataEntity dataEntity, float x, float y, Drawable icon, StatisticResults statisticResults
	) {
		super(dataEntity, x, y, icon, statisticResults);
	}

	public static CurveDataEntityEntry create(
		PaletteColorDeterminer paletteColorDeterminer,
		StatisticResults statisticResults,
		float x, float y
	) {
		Drawable drawableIcon = null;

		try {
			drawableIcon = paletteColorDeterminer.getDrawableIconFrom(y);
		} catch (Exception ex) {
			Log.e("DataEntityCurveEntry", "create: ", ex);
		}

		DataEntity dataEntity = statisticResults.getDataEntityVector().elementAt((int) x);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis( dataEntity.getTimestampMillis() );

		float timeConcat = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);

		return new CurveDataEntityEntry(
			dataEntity,
			timeConcat, y, SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
			statisticResults
		);
	}

	@NonNull
	public static LineDataSet 	createCurveDataEntityLineDataSet(ArrayList<Entry> entries, LineChartSettings settings) {
		LineDataSet dataSet = new LineDataSet(entries, CURVE_DATA_ENTITY);
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

		dataSet.setDrawIcons(settings.isDrawIconsEnabled());
		dataSet.setDrawValues(false);

		return dataSet;
	}
}
