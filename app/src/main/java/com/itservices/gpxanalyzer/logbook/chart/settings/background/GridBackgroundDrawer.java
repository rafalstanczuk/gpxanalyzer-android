package com.itservices.gpxanalyzer.logbook.chart.settings.background;

import static com.itservices.gpxanalyzer.logbook.chart.settings.axis.HourMinutesAxisValueFormatter.MIN_X_SCALED_TIME;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.logbook.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GridBackgroundDrawer {
    public static final int BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA = 70;

    @Inject
    public LimitLinesBoundaries measurement;

    @Inject
    public PaletteColorDeterminer palette;

    @Inject
    public GridBackgroundDrawer(@ApplicationContext Context context) {

    }

    public void drawGridBackground(LineChart lineChart, Canvas canvas) {
        for(Map.Entry<Integer, BoundaryColorSpan> entry: palette.getPalette().entrySet()) {
            drawRectArea(canvas, entry.getValue(), lineChart);
        }
    }

    public void drawRectArea(Canvas canvas, BoundaryColorSpan boundaryColorSpan, LineChart lineChart) {
        int colorForAreaId = ColorUtil.setAlphaInIntColor(boundaryColorSpan.getColor(), BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA);

        MPPointF leftMin = null;
        MPPointF leftMax = null;

        leftMin = lineChart.getPosition(
                new Entry(MIN_X_SCALED_TIME, boundaryColorSpan.getMin()), YAxis.AxisDependency.LEFT);
        leftMax = lineChart.getPosition(
                new Entry(MIN_X_SCALED_TIME, boundaryColorSpan.getMax()),
                YAxis.AxisDependency.LEFT
        );

        Paint paintMinMax = new Paint();
        paintMinMax.setColor(colorForAreaId);

        if (leftMax != null) {
            RectF rectMinMax = new RectF(lineChart.getViewPortHandler().contentLeft(), leftMax.y,
                    lineChart.getViewPortHandler().contentRight(), leftMin.y
            );

            canvas.drawRect(rectMinMax, paintMinMax);
        }
    }
}
