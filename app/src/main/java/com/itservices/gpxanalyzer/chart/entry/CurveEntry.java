package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;

import java.util.Calendar;

public class CurveEntry extends BaseEntry {
    public static boolean SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;
    private final TrendBoundaryDataEntity trendBoundaryDataEntity;

    CurveEntry(
            DataEntity dataEntity, TrendBoundaryDataEntity trendBoundaryDataEntity, float x, float y, Drawable icon, DataEntityWrapper dataEntityWrapper
    ) {
        super(dataEntity, trendBoundaryDataEntity.id(), x, y, icon, dataEntityWrapper);

        this.trendBoundaryDataEntity = trendBoundaryDataEntity;
    }

    public static CurveEntry create(
            DataEntity dataEntity,
            TrendBoundaryDataEntity trendBoundaryDataEntity, PaletteColorDeterminer paletteColorDeterminer,
            DataEntityWrapper dataEntityWrapper
    ) {
        Drawable drawableIcon = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dataEntity.timestampMillis());
        float floatTime = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);
        float value = dataEntityWrapper.getValue( dataEntity );

        try {
            drawableIcon = paletteColorDeterminer.getDrawableIconFrom(value);
        } catch (Exception ex) {
            Log.e("DataEntityCurveEntry", "create: ", ex);
        }

        return new CurveEntry(
                dataEntity,
                trendBoundaryDataEntity,
                floatTime, value, SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
                dataEntityWrapper
        );
    }

    public TrendBoundaryDataEntity getTrendBoundaryDataEntity() {
        return trendBoundaryDataEntity;
    }
}
