package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.Calendar;

public class CurveDataEntityEntry extends BaseDataEntityEntry {
    public static final int FILL_COLOR_UNDER_CURVE = ColorUtil.rgb(0.96f, 0.96f, 0.96f);
    public static final int FILL_COLOR_ALPHA_UNDER_CURVE = (int) (0.3f * 255.0f);
    public static boolean SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;

    CurveDataEntityEntry(
            DataEntity dataEntity, int dataSetIndex, float x, float y, Drawable icon, StatisticResults statisticResults
    ) {
        super(dataEntity, dataSetIndex, x, y, icon, statisticResults);
    }

    public static CurveDataEntityEntry create(
            DataEntity dataEntity,
            int dataSetIndex,
            PaletteColorDeterminer paletteColorDeterminer,
            StatisticResults statisticResults
    ) {
        Drawable drawableIcon = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dataEntity.getTimestampMillis());
        float floatTime = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);
        float value = dataEntity.getValueList().get(statisticResults.getPrimaryDataIndex());

        try {
            drawableIcon = paletteColorDeterminer.getDrawableIconFrom(value);
        } catch (Exception ex) {
            Log.e("DataEntityCurveEntry", "create: ", ex);
        }

        return new CurveDataEntityEntry(
                dataEntity,
                dataSetIndex,
                floatTime, value, SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
                statisticResults
        );
    }

/*	@NonNull
	public static LineDataSet 	createCurveDataEntityLineDataSet(ArrayList<Entry> entries, LineChartSettings settings) {
		LineDataSet dataSet = new LineDataSet(entries, CURVE_DATA_ENTITY);
		dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
		dataSet.setHighlightEnabled(true);

		dataSet.setDrawCircles(false);
		dataSet.setLineWidth(1.0f);

		//For static line feature marker purpose!
		dataSet.setDrawHorizontalHighlightIndicator(false);
*//*
		dataSet.setCircleRadius(5f);
		dataSet.setDrawCircleHole(false);*//*

		dataSet.setColor(Color.BLACK);
		//dataSet.setCircleColor(Color.BLUE);

		dataSet.setDrawFilled(true);
		dataSet.setFillColor(FILL_COLOR_UNDER_CURVE); aaaaaaa
		dataSet.setFillAlpha(FILL_COLOR_ALPHA_UNDER_CURVE); aaaaaaaa

		//dataSet.setDrawFilled(true);

		dataSet.setHighLightColor(Color.BLACK);

		dataSet.setDrawIcons(settings.isDrawIconsEnabled());
		dataSet.setDrawValues(false);

		return dataSet;
	}*/
}
