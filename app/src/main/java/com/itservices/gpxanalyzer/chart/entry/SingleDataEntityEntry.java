package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class SingleDataEntityEntry extends BaseDataEntityEntry {
	public static final String SINGLE_DATA_ENTITY = "SINGLE_DATA_ENTITY";
	public static boolean SHOW_COLOR_SINGLE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;

	SingleDataEntityEntry(
			DataEntity dataEntity, float x, float y, Drawable icon, StatisticResults statisticResults
	) {
		super(dataEntity, x, y, icon, statisticResults);
	}

	public static SingleDataEntityEntry create(
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

		return new SingleDataEntityEntry(
				dataEntity, timeConcat, y, SHOW_COLOR_SINGLE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
				statisticResults
		);
	}

	@NonNull
	public static LineDataSet createSingleDataEntityLineDataSet(ArrayList<Entry> entries) {
		LineDataSet updatedDataSet = new LineDataSet(entries, SINGLE_DATA_ENTITY);

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
