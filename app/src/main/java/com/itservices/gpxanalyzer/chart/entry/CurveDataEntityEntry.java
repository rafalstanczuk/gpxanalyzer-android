package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;

import java.util.Calendar;

public class CurveDataEntityEntry extends BaseDataEntityEntry {
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
}
