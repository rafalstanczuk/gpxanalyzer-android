package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.TrendBoundaryDataEntity;

import java.util.Calendar;

public class CurveDataEntityEntry extends BaseDataEntityEntry {
    public static boolean SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;
    private final TrendBoundaryDataEntity trendBoundaryDataEntity;

    CurveDataEntityEntry(
            DataEntity dataEntity, TrendBoundaryDataEntity trendBoundaryDataEntity, float x, float y, Drawable icon, StatisticResults statisticResults
    ) {
        super(dataEntity, trendBoundaryDataEntity.id(), x, y, icon, statisticResults);

        this.trendBoundaryDataEntity = trendBoundaryDataEntity;
    }

    public static CurveDataEntityEntry create(
            DataEntity dataEntity,
            TrendBoundaryDataEntity trendBoundaryDataEntity, PaletteColorDeterminer paletteColorDeterminer,
            StatisticResults statisticResults
    ) {
        Drawable drawableIcon = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dataEntity.timestampMillis());
        float floatTime = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);
        float value = dataEntity.valueList().get(statisticResults.getPrimaryDataIndex());

        try {
            drawableIcon = paletteColorDeterminer.getDrawableIconFrom(value);
        } catch (Exception ex) {
            Log.e("DataEntityCurveEntry", "create: ", ex);
        }

        return new CurveDataEntityEntry(
                dataEntity,
                trendBoundaryDataEntity,
                floatTime, value, SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
                statisticResults
        );
    }

    public TrendBoundaryDataEntity getTrendBoundaryDataEntity() {
        return trendBoundaryDataEntity;
    }
}
